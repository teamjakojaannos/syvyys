package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.Gdx;
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

import java.util.List;

public class IntroStage implements GameStage {
    private Sound ukkoKuoriutuuPisteWav;

    @Override
    public GameState createState(final GameStage gameStage, final Camera camera) {
        final var physicsWorld = new World(new Vector2(0.0f, 0.0f), true);
        final var player = Player.create(physicsWorld, new Vector2(1.2f, 1.0f));
        player.grounded(true);
        player.facingRight(false);

        final var demonicSpawn = new IntroDemonicSpawn(new Vector2(-1.2f, 0.5f));

        final var ui = new UI();
        ui.showPlayerHp = false;
        ui.messageText = "Hello world!";
        final var state = new GameState(gameStage, physicsWorld, List.of(
                demonicSpawn,
                player,
                ui
        ), player, camera);
        camera.setLocation(new Vector2(0, 2));

        this.ukkoKuoriutuuPisteWav = Gdx.audio.newSound(Gdx.files.internal("ukko_kuoriutuu.wav"));

        final var timers = state.getTimers();

        state.setBackgroundColor(new Color(0.3f, 0.3f, 0.3f, 1.0f));
        demonicSpawn.stageTimer = timers.set(15f, false, () -> {
            demonicSpawn.stage = IntroDemonicSpawn.Stage.TREMBLING;

            demonicSpawn.flashTimer = timers.set(1.0f, false, () -> tremblingFlash(demonicSpawn, state));

            demonicSpawn.stageTimer = timers.set(3.0f, false, () -> {
                timers.set(2.75f, false, this.ukkoKuoriutuuPisteWav::play);
                demonicSpawn.stage = IntroDemonicSpawn.Stage.HATS_OFF;
                timers.clear(demonicSpawn.flashTimer);
                timers.clear(demonicSpawn.flashTimer);

                state.setBackgroundColor(new Color(0.3f, 0.3f, 0.3f, 1.0f));

                demonicSpawn.stageTimer = timers.set(4.0f, false, () -> {

                    demonicSpawn.stage = IntroDemonicSpawn.Stage.SPLIT;

                    demonicSpawn.stageTimer = timers.set(5.0f, false, () -> {
                        state.setBackgroundColor(new Color(0.1f, 0.1f, 0.1f, 1.0f));

                        demonicSpawn.stage = IntroDemonicSpawn.Stage.SPLURT;


                        demonicSpawn.stageTimer = timers.set(1.5f, false, () -> {
                            demonicSpawn.stage = IntroDemonicSpawn.Stage.HATCHING;
                            final var soulTrap = SoulTrap.create(
                                    state.getPhysicsWorld(),
                                    new Vector2(player.body().getPosition())
                                            .add(-1.0f, -0.5f)
                            );
                            timers.set(1.5f, false, () -> {
                                state.spawn(soulTrap);
                            });

                            timers.set(2.5f, false, () -> {
                                soulTrap.state = SoulTrap.State.BUBBLING;
                            });

                            demonicSpawn.stageTimer = timers.set(5.0f, false, () -> {
                                demonicSpawn.stage = IntroDemonicSpawn.Stage.IDLE_HATCHED;
                                soulTrap.state = SoulTrap.State.I_WANT_OUT;

                                player.dealDamage(999999.0f);
                                player.deathTimer(timers.set(3.0f, false, () -> {
                                    player.deathSequenceHasFinished(true);
                                }));

                                timers.set(7.5f, false, () -> state.changeStage(new RegularCircleStage(1)));
                            });
                        });
                    });
                });
            });
        });

        return state;
    }

    private void tremblingFlash(
            final IntroDemonicSpawn demonicSpawn,
            final GameState state
    ) {
        final var timers = state.getTimers();
        state.setBackgroundColor(new Color(0.8f, 0.8f, 0.8f, 1.0f));

        demonicSpawn.flashTimer = timers.set(0.15f, false, () -> {
            state.setBackgroundColor(new Color(0.8f, 0.1f, 0.1f, 1.0f));

            demonicSpawn.flashTimer = timers.set(1.0f, false, () -> tremblingFlash(demonicSpawn, state));
        });
    }

    @Override
    public void tick(final float deltaSeconds, final GameState gameState) {

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
