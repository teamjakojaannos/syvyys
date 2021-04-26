package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.entities.SoulTrap;
import fi.jakojaannos.syvyys.entities.UI;
import fi.jakojaannos.syvyys.entities.intro.IntroDemonicSpawn;
import fi.jakojaannos.syvyys.renderer.Camera;
import fi.jakojaannos.syvyys.renderer.Renderer;

import java.util.Collections;
import java.util.List;

public class IntroStage implements GameStage {
    public static final float TREMBLE_FLASH_DELAY = 0.75f;
    public static final float TREMBLE_FLASH_DURATION = 0.03f;

    private static final String[] INTRO_DIALOGUE = new String[]{
            "Hello world!",
            "More text!",
            "Even more!"
    };

    private Sound ukkoKuoriutuuPisteWav;
    private UI ui;
    private IntroDemonicSpawn demonicSpawn;
    private Player player;
    private int iDialogue = 0;

    @Override
    public GameState createState(
            final GameStage gameStage,
            final GameState previousState,
            final Camera camera
    ) {
        final var physicsWorld = new World(new Vector2(0.0f, 0.0f), true);
        this.player = Player.create(physicsWorld, new Vector2(1.2f, 1.0f));
        this.player.grounded(true);
        this.player.facingRight(false);

        this.demonicSpawn = new IntroDemonicSpawn(new Vector2(-1.2f, 0.5f));

        this.ui = new UI();
        this.ui.showPlayerHp = false;
        this.ui.messageText = "Hello world!";
        final var state = new GameState(gameStage, physicsWorld, List.of(
                this.demonicSpawn,
                this.player,
                this.ui
        ), this.player, camera, Collections.emptyList());
        camera.setLocation(new Vector2(0, 2));

        this.ukkoKuoriutuuPisteWav = Gdx.audio.newSound(Gdx.files.internal("ukko_kuoriutuu.wav"));
        state.setBackgroundColor(new Color(0.3f, 0.3f, 0.3f, 1.0f));

        return state;
    }

    private void introAnimationSequence(
            final Player player,
            final IntroDemonicSpawn demonicSpawn,
            final GameState state
    ) {
        final var timers = state.getTimers();

        demonicSpawn.stageTimer = timers.set(1.5f, false, () -> {
            demonicSpawn.stage = IntroDemonicSpawn.Stage.TREMBLING;

            demonicSpawn.flashTimer = timers.set(TREMBLE_FLASH_DELAY, false,
                                                 () -> tremblingFlash(demonicSpawn, state,
                                                                      new Color(0.3f, 0.2f, 0.2f, 1.0f),
                                                                      new Color(0.3f, 0.3f, 0.3f, 1.0f)));

            demonicSpawn.stageTimer = timers.set(3.0f, false, () -> {
                demonicSpawn.stage = IntroDemonicSpawn.Stage.HATS_OFF;

                final var colorA = new Color(0.3f, 0.3f, 0.3f, 1.0f);
                timers.clear(demonicSpawn.flashTimer);
                demonicSpawn.flashTimer = timers.set(2.25f, false, () -> {
                    tremblingFlash(demonicSpawn, state, colorA, new Color(0.2f, 0f, 0f, 1.0f));
                    this.ukkoKuoriutuuPisteWav.play();
                });

                state.setBackgroundColor(colorA);

                demonicSpawn.stageTimer = timers.set(4.0f, false, () -> {
                    demonicSpawn.stage = IntroDemonicSpawn.Stage.SPLIT;
                    timers.clear(demonicSpawn.flashTimer);

                    state.setBackgroundColor(new Color(0.2f, 0f, 0f, 1.0f));

                    demonicSpawn.stageTimer = timers.set(1.75f, false, () -> {
                        state.setBackgroundColor(new Color(0.1f, 0.0f, 0.0f, 1.0f));

                        demonicSpawn.stage = IntroDemonicSpawn.Stage.SPLURT;


                        demonicSpawn.stageTimer = timers.set(1.75f, false, () -> {
                            demonicSpawn.stage = IntroDemonicSpawn.Stage.HATCHING;
                            final var soulTrap = SoulTrap.create(
                                    state.getPhysicsWorld(),
                                    new Vector2(player.body().getPosition())
                                            .add(-1.0f, -0.5f)
                            );
                            timers.set(1.5f, false, () -> {
                                state.setBackgroundColor(new Color(0.05f, 0.0f, 0.0f, 1.0f));
                                state.spawn(soulTrap);
                            });

                            timers.set(2.5f, false, () -> {
                                soulTrap.state = SoulTrap.State.BUBBLING;
                            });

                            demonicSpawn.stageTimer = timers.set(5.0f, false, () -> {
                                demonicSpawn.stage = IntroDemonicSpawn.Stage.IDLE_HATCHED;
                                soulTrap.state = SoulTrap.State.I_WANT_OUT;
                                state.setBackgroundColor(new Color(0.025f, 0.0f, 0.0f, 1.0f));

                                player.dealDamage(999999.0f, state);
                                player.deathTimer(timers.set(3.0f, false, () -> {
                                    player.deathSequenceHasFinished(true);
                                }));

                                timers.set(7.5f, false, () -> state.changeStage(new RegularCircleStage(1), true));
                            });
                        });
                    });
                });
            });
        });
    }

    private void tremblingFlash(
            final IntroDemonicSpawn demonicSpawn,
            final GameState state,
            final Color colorA,
            final Color colorB
    ) {
        final var timers = state.getTimers();
        state.setBackgroundColor(colorB);

        demonicSpawn.flashTimer = timers.set(TREMBLE_FLASH_DURATION, false, () -> {
            state.setBackgroundColor(colorA);

            demonicSpawn.flashTimer = timers.set(TREMBLE_FLASH_DELAY, false, () -> tremblingFlash(demonicSpawn, state, colorA, colorB));
        });
    }

    @Override
    public void tick(final float deltaSeconds, final GameState gameState) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z) && this.iDialogue < INTRO_DIALOGUE.length) {
            ++this.iDialogue;
            if (this.iDialogue == INTRO_DIALOGUE.length) {
                this.ui.messageText = null;
                introAnimationSequence(this.player, this.demonicSpawn, gameState);
                return;
            }

            this.ui.messageText = INTRO_DIALOGUE[this.iDialogue];
        }
    }

    @Override
    public void systemTick(final GameState gameState) {

    }

    @Override
    public void lateSystemTick(final Renderer renderer, final GameState gameState) {

    }

    @Override
    public void close() {
        this.ukkoKuoriutuuPisteWav.dispose();
    }
}
