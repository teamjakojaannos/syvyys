package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.entities.SpikeBarrier;
import fi.jakojaannos.syvyys.entities.SpikeNode;
import fi.jakojaannos.syvyys.level.BossLevelGenerator;
import fi.jakojaannos.syvyys.level.TileLevelGenerator;

public class BossStage extends RegularCircleStage {
    private boolean barrierClosed;
    private boolean bossSequenceStarted;
    private BossLevelGenerator levgen;

    public BossStage(final int circleN) {
        super(circleN);
    }

    @Override
    protected TileLevelGenerator createLevelGenerator() {
        return this.levgen = new BossLevelGenerator();
    }

    @Override
    public void tick(final float deltaSeconds, final GameState gameState) {
        if (!this.barrierClosed) {
            gameState.getPlayer()
                     .ifPresent(player -> {
                         if (player.body().getPosition().x > 21.0f * BossLevelGenerator.TILE_WIDTH) {
                             startBossSequence(gameState, player);
                         }
                     });
        }

        if (this.barrierClosed && !this.bossSequenceStarted) {
            if (this.player.body().getPosition().x > 40.0f * BossLevelGenerator.TILE_WIDTH) {
                this.player.body().setLinearVelocity(0.0f, 0.0f);
                this.player.body().setTransform(new Vector2(40.0f * BossLevelGenerator.TILE_WIDTH, this.player.body().getPosition().y), 0.0f);
                this.player.disableInput();
                this.bossSequenceStarted = true;

                spawnBossShockwave(gameState);

                final var level = this.levgen.generateBossPlatforms(gameState.getPhysicsWorld(), gameState);
                level.getAllTiles().forEach(gameState::spawn);
                level.getAllEntities().forEach(gameState::spawn);

                gameState.getTimers().set(5.0f, false, () -> this.player.enableInput());
            }
        }

        super.tick(deltaSeconds, gameState);
    }

    private void startBossSequence(final GameState gameState, final Player player) {
        this.barrierClosed = true;
        gameState.spawn(SpikeBarrier.create(gameState.getPhysicsWorld(),
                                            new Vector2(20.0f * BossLevelGenerator.TILE_WIDTH - 0.25f, 1.5f),
                                            gameState.getTimers()));
    }

    private void spawnBossShockwave(final GameState gameState) {
        final var a = new boolean[]{true, false};
        for (final var right : a) {
            final float echoDelay = 0.75f;
            final float size = 1.0f;
            final var factorStep = 1.5f;

            float factor = 1.0f;
            final int nEchoes = 4;
            for (int i = 0; i < nEchoes; i++) {
                final float finalFactor = factor; // HACK: make factor effectively final
                gameState.getTimers().set(i * echoDelay, false, () -> {
                    final var entities = SpikeNode.spawnSpikeStrip(
                            gameState.getPhysicsWorld(),
                            new Vector2(this.player.body().getPosition()).add(right ? 2.0f : -2.0f, 0.0f),
                            (size / 2.0f) * finalFactor, size * finalFactor,
                            8,
                            0.75f,
                            0.0f,
                            0.25f,
                            right
                    );
                    entities.forEach(gameState::spawn);
                });

                factor *= factorStep;
            }
        }
    }
}
