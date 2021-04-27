package fi.jakojaannos.syvyys.stages;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.level.BossLevelGenerator;
import fi.jakojaannos.syvyys.level.TileLevelGenerator;

public class BossStage extends RegularCircleStage {
    private boolean barrierClosed;
    private boolean bossSequenceStarted;
    private BossLevelGenerator levgen;
    private Boss boss;
    private boolean bossKilled;

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

                spawnBossShockwave(gameState, 4, 2.0f);

                var ccircleN = 1;
                if (gameState.getCurrentStage() instanceof RegularCircleStage circleStage) {
                    ccircleN = circleStage.circleN + 1;
                }
                final var circleN = ccircleN;

                gameState.getTimers().set(5.0f, false, () -> {
                    this.player.enableInput();

                    gameState.getTimers().set(10.6f, true, () -> {
                        for (int i = 0; i < 4; i++) {
                            gameState.getTimers().set(i * 1.0f, false, () -> {
                                gameState.spawn(DemonBall.create(
                                        gameState.getPhysicsWorld(),
                                        this.boss.handL.getPosition(),
                                        dirToPlayer(gameState, this.boss.handL.getPosition()),
                                        0.1250f,
                                        gameState.getCurrentTime(),
                                        10.0f,
                                        (float) (Math.ceil(circleN / 10.0f) * 1.0f)
                                ));
                            });
                        }
                    });

                    gameState.getTimers().set(10.0f, true, () -> {
                        spawnBossShockwave(gameState, 2, 0.0f);
                    });

                    gameState.getTimers().set(13.7f, true, () -> {
                        for (int i = 0; i < 2; i++) {
                            gameState.getTimers().set(i * 0.2f, false, () -> {
                                gameState.spawn(DemonBall.create(
                                        gameState.getPhysicsWorld(),
                                        this.boss.handR.getPosition(),
                                        dirToPlayer(gameState, this.boss.handL.getPosition()),
                                        0.125f,
                                        gameState.getCurrentTime(),
                                        10.0f,
                                        (float) Math.ceil(circleN / 10.0f)
                                ));
                            });
                        }
                    });

                    gameState.getTimers().set(25.5f, true, () -> {
                        for (int i = 0; i < 8; i++) {
                            gameState.getTimers().set(i * 0.025f, false, () -> {
                                gameState.spawn(DemonBall.create(
                                        gameState.getPhysicsWorld(),
                                        this.boss.handR.getPosition(),
                                        dirToPlayer(gameState, this.boss.handR.getPosition()),
                                        0.125f,
                                        gameState.getCurrentTime(),
                                        10.0f,
                                        (float) Math.ceil(circleN / 10.0f)
                                ));
                            });

                            gameState.getTimers().set(i * 0.2f, false, () -> {
                                gameState.spawn(DemonBall.create(
                                        gameState.getPhysicsWorld(),
                                        this.boss.handL.getPosition(),
                                        dirToPlayer(gameState, this.boss.handL.getPosition()),
                                        0.0125f,
                                        gameState.getCurrentTime(),
                                        10.0f,
                                        (float) Math.ceil(circleN / 10.0f)
                                ));
                            });
                        }
                    });
                });
                gameState.getTimers().set(3.0f, false, () -> {

                    gameState.spawn(this.boss = Boss.create(gameState.getPhysicsWorld(), gameState, circleN));
                });
                gameState.getTimers().set(3.0f, false, () -> {
                    final var level = this.levgen.generateBossPlatforms(gameState.getPhysicsWorld(), gameState);
                    level.getAllTiles().forEach(gameState::spawn);
                    level.getAllEntities().forEach(gameState::spawn);
                });
            }
        }

        super.tick(deltaSeconds, gameState);
    }

    @Override
    public void systemTick(GameState gameState) {
        super.systemTick(gameState);

        if (this.boss != null && this.boss.health() < 0 && !this.bossKilled) {
            Boss.INSTANCE = null;
            this.bossKilled = true;

            gameState.forceKillTimers();

            gameState.deletThis(this.boss);
            this.player.disableInput();
            this.player.body().setLinearVelocity(0.0f, 0.0f);
            this.ui.messageText = "Devil:";

            gameState.getTimers().set(4.0f, false, () -> {
                this.ui.messageText = "I do not have time for this...";
                gameState.getTimers().set(4.0f, false, () -> {
                    this.ui.messageText = "I have other things to attend to...";

                    gameState.getTimers().set(4.0f, false, () -> {
                        var circleN = 1;
                        if (gameState.getCurrentStage() instanceof RegularCircleStage circleStage) {
                            circleN = circleStage.circleN + 1;
                        }

                        gameState.changeStage(circleN % 10 != 0 ? new RegularCircleStage(circleN) : new BossStage(circleN), false);
                    });
                });
            });
        }
    }

    private void startBossSequence(final GameState gameState, final Player player) {
        this.barrierClosed = true;
        gameState.spawn(SpikeBarrier.create(gameState.getPhysicsWorld(),
                                            new Vector2(20.0f * BossLevelGenerator.TILE_WIDTH - 0.25f, 1.5f),
                                            gameState.getTimers()));
    }

    private void spawnBossShockwave(final GameState gameState, int nEchoes1, float dist1) {
        final var a = new boolean[]{true, false};
        for (final var right : a) {
            final float echoDelay = 0.75f;
            final float size = 1.0f;
            final var factorStep = 1.5f;

            float factor = 1.0f;
            for (int i = 0; i < nEchoes1; i++) {
                final float finalFactor = factor; // HACK: make factor effectively final
                gameState.getTimers().set(i * echoDelay, false, () -> {
                    final var dist = dist1;
                    final var entities = SpikeNode.spawnSpikeStrip(
                            gameState.getPhysicsWorld(),
                            new Vector2(this.player.body().getPosition()).add(right ? dist : -dist, 0.0f),
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

    public static Vector2 dirToPlayer(GameState gameState, Vector2 position) {
        return gameState
                .getPlayer()
                .map(GameCharacter::body)
                .map(Body::getPosition)
                .map(playerPos -> new Vector2(playerPos).sub(position))
                .orElseGet(() -> new Vector2().setToRandomDirection());
    }
}
