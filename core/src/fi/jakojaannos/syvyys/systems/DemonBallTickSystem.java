package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Boss;
import fi.jakojaannos.syvyys.entities.DemonBall;

import java.util.stream.Stream;

public class DemonBallTickSystem implements EcsSystem<DemonBall> {

    @Override
    public void tick(final Stream<DemonBall> balls, final GameState gameState) {
        balls.forEach(ball -> {
            if (ball.isInContactWithPlayer) {
                gameState.getPlayer().ifPresent(player -> {
                    player.dealDamage(ball.damage, gameState);

                    final var pushForce = 5.0f * player.body().getMass();
                    final var outward = new Vector2(player.body().getPosition())
                            .sub(ball.body().getPosition())
                            .nor()
                            .scl(pushForce);

                    player.body().applyLinearImpulse(
                            outward,
                            player.body().getPosition(),
                            true
                    );


                });
                gameState.deletThis(ball);
                return;
            }

            if (ball.collidedWithWall) {
                gameState.deletThis(ball);
                return;
            }

            // O'Reilly: How Not To Handle Projectile Particle Trail 101
            gameState.obtainParticleEmitter()
                     .spawnBurst(gameState.getCurrentTime(),
                                 2,
                                 ball.body().getPosition(),
                                 0.5f,
                                 new Vector2(0.0f, -1.0f),
                                 1.0f,
                                 0.1f, 0.5f,
                                 0.5f, 1.0f,
                                 0.4f, 0.7f,
                                 0.0f, 0.0f,
                                 new Color(1.0f, 0.4f, 0.4f, 1.0f),
                                 new Color(0.6f, 0.2f, 0.2f, 1.0f),
                                 new Vector2(0.0f, 0.0f));

            if (Boss.INSTANCE == null || ball.body().getLinearVelocity().len2() < 8.0f) {
                ball.body().applyLinearImpulse(
                        ball.direction,
                        ball.body().getPosition(),
                        true
                );
            }
        });
    }
}
