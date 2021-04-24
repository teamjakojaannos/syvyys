package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.level.TileLevelGenerator;
import fi.jakojaannos.syvyys.physics.PhysicsContactListener;
import fi.jakojaannos.syvyys.renderer.Renderer;
import fi.jakojaannos.syvyys.systems.CharacterTickSystem;

import java.util.ArrayList;
import java.util.List;

public class FirstCircleStage implements GameStage {
    private CharacterTickSystem characterTick;
    private Player player;

    @Override
    public GameState createState() {
        final var physicsWorld = new World(new Vector2(0.0f, -20.0f), true);
        physicsWorld.setContactListener(new PhysicsContactListener());

        this.player = Player.create(physicsWorld, new Vector2(0.0f, 3.0f));

        final var level = new TileLevelGenerator(666).generateLevel(physicsWorld);

        this.characterTick = new CharacterTickSystem();

        final List<Entity> entities = new ArrayList<>(level.getAllTiles());
        entities.add(this.player);
        final var state = new GameState(physicsWorld, entities);
        state.setBackgroundColor(new Color(0.01f, 0f, 0f, 1.0f));
        return state;
    }

    @Override
    public void tick(final float deltaSeconds, final GameState gameState) {
        final int leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.A)
                ? 1 : 0;

        final int rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.D)
                ? 1 : 0;

        final boolean attackPressed = Gdx.input.isKeyPressed(Input.Keys.Z) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        final boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        this.player.input = new Player.Input(
                rightPressed - leftPressed,
                attackPressed,
                jumpPressed
        );
    }

    @Override
    public void systemTick(final GameState gameState) {
        this.characterTick.tick(List.of(this.player), gameState);
    }

    @Override
    public void lateSystemTick(final Renderer renderer, final GameState gameState) {
        renderer.getCamera().lerpNewPosition(this.player.body().getPosition());
    }
}
