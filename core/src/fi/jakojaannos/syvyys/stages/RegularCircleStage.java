package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.level.TileLevelGenerator;
import fi.jakojaannos.syvyys.physics.PhysicsContactListener;
import fi.jakojaannos.syvyys.renderer.Camera;
import fi.jakojaannos.syvyys.renderer.Renderer;
import fi.jakojaannos.syvyys.systems.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegularCircleStage implements GameStage {
    public final int circleN;

    private CharacterTickSystem characterTick;
    private SoulTrapTickSystem soulTrapTick;
    private DemonAiSystem demonAiTick;
    private EntityReaperSystem reaperTick;
    private TransitionStageSystem transitionTick;
    private UpdateParticleEmittersSystem emitterTick;
    private DemonBallTickSystem projectileTick;
    private PlayerAbilityTickSystem playerAbilityTick;

    private Player player;
    private HellspiderCollideWithPlayerSystem spooderCollisionTick;

    public RegularCircleStage(final int circleN) {
        this.circleN = circleN;
    }

    @Override
    public GameState createState(
            final GameStage gameStage,
            final GameState previousState,
            final Camera camera
    ) {
        final var physicsWorld = new World(new Vector2(0.0f, -20.0f), true);
        physicsWorld.setContactListener(new PhysicsContactListener());

        final var playerStartPos = new Vector2(0.0f, 80.0f);
        this.player = Optional.ofNullable(previousState)
                              .flatMap(GameState::getPlayer)
                              .map(player -> Player.copyFrom(physicsWorld, playerStartPos, player))
                              .orElseGet(() -> Player.create(
                                      physicsWorld,
                                      playerStartPos)
                              );

        camera.setLocation(new Vector2(0, 90.0f));
        camera.lockedToPlayer = true;

        final var level = new TileLevelGenerator(
                666L * this.circleN,
                0.10f + this.circleN * 0.01f,
                0.035f + this.circleN * 0.01f,
                0.035f + this.circleN * 0.0075f,
                200 + 15 * this.circleN
        ).generateLevel(physicsWorld);

        this.characterTick = new CharacterTickSystem();
        this.soulTrapTick = new SoulTrapTickSystem();
        this.demonAiTick = new DemonAiSystem();
        this.reaperTick = new EntityReaperSystem();
        this.transitionTick = new TransitionStageSystem();
        this.emitterTick = new UpdateParticleEmittersSystem();
        this.projectileTick = new DemonBallTickSystem();
        this.playerAbilityTick = new PlayerAbilityTickSystem();
        this.spooderCollisionTick = new HellspiderCollideWithPlayerSystem();

        final List<Entity> entities = new ArrayList<>(level.getAllTiles());
        entities.addAll(level.getAllEntities());
        //entities.add(Hellspider.create(physicsWorld, new Vector2(20.0f, 40.0f)));

        entities.add(this.player);
        final var ui = new UI();
        ui.showPlayerHp = true;
        ui.messageText = null;
        entities.add(ui);

        // Pass null player to disable AI for the "fall sequence". Player is set in TransitionStageSystem once
        // the player touches down.
        final var state = new GameState(gameStage, physicsWorld, entities, null, camera);
        state.setBackgroundColor(new Color(0.01f, 0f, 0f, 1.0f));
        return state;
    }

    @Override
    public void tick(final float deltaSeconds, final GameState gameState) {
        if (gameState.getCamera().lockedToPlayer) {
            this.player.input(new CharacterInput(0.0f, false, false));
            this.player.abilityInput(new AbilityInput(false));
            return;
        }
        final int leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.A)
                ? 1 : 0;

        final int rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.D)
                ? 1 : 0;

        final boolean attackPressed = Gdx.input.isKeyPressed(Input.Keys.Z) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        if (!attackPressed) {
            this.player.isHoldingAttack = false;
        }

        final boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (Gdx.input.isKeyPressed(Input.Keys.K)) {
            this.player.dealDamage(999999.0f, gameState);
        }

        this.player.input(new CharacterInput(
                rightPressed - leftPressed,
                attackPressed,
                jumpPressed
        ));

        final boolean dash = Gdx.input.isKeyPressed(Input.Keys.X);
        this.player.abilityInput(new AbilityInput(dash));
    }

    @Override
    public void systemTick(final GameState gameState) {
        this.player.input(new CharacterInput(
                this.player.input().horizontalInput(),
                this.player.input().attack() && !this.player.isHoldingAttack,
                this.player.input().jump()
        ));

        this.playerAbilityTick.tick(gameState.getEntities(Player.class), gameState);
        this.characterTick.tick(gameState.getEntities(CharacterTickSystem.InputEntity.class), gameState);
        this.soulTrapTick.tick(gameState.getEntities(SoulTrap.class), gameState);
        this.demonAiTick.tick(gameState.getEntities(HasEnemyAI.class), gameState);
        this.projectileTick.tick(gameState.getEntities(DemonBall.class), gameState);
        this.spooderCollisionTick.tick(gameState.getEntities(Hellspider.class), gameState);
        this.reaperTick.tick(gameState.getEntities(HasHealth.class, true), gameState);
        this.emitterTick.tick(gameState.getEntities(ParticleEmitter.class), gameState);
        this.transitionTick.tick(gameState.getEntities(Player.class, true), gameState);

        if (this.player.input().attack()) {
            this.player.isHoldingAttack = true;
        }
    }

    @Override
    public void lateSystemTick(final Renderer renderer, final GameState gameState) {
        renderer.getCamera().lerpNewPosition(this.player.body().getPosition());
    }

    @Override
    public void close() {
    }
}
