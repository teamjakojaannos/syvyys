package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;

public class CharacterTickSystem implements EcsSystem<Player> {
    private static final float EPSILON = 0.0001f;

    @Override
    public void tick(final Iterable<Player> entities, final GameState gameState) {
        entities.forEach(entity -> {
            applyInputForceAndFriction(entity);
            limitMaxHorizontalSpeed(entity);

            final var velocity = entity.body().getLinearVelocity();
            entity.distanceTravelled += Math.abs(velocity.x);

            final boolean isMoving = Math.abs(velocity.x) > 0.0f;
            if (Math.abs(entity.input.horizontalInput()) > 0.0f && isMoving && !entity.attacking) {
                entity.facingRight = velocity.x > 0.0f;
            }

            // Attack
            if (entity.input.attack() && !entity.attacking) {
                entity.attacking = true;

                final var timers = gameState.getTimers();
                entity.shotTimer = timers.set(entity.attackDuration / entity.shotsPerAttack, true, () -> {
                    final var barrelOffsetX = 0.33f * (entity.facingRight ? 1 : -1);
                    final var position = new Vector2(entity.body().getPosition())
                            .add(barrelOffsetX, -0.15f);
                    gameState.obtainParticleEmitter()
                             .spawnBurst(gameState.getCurrentTime(),
                                         25,
                                         position,
                                         0.01f,
                                         Vector2.Y,
                                         0.025f,
                                         0.25f, 0.5f,
                                         1f, 1f,
                                         2.5f, 5.0f,
                                         0.0f, 0.01f,
                                         0.5f, 0.75f,
                                         new Color(1.0f, 1.0f, 1.0f, 1.0f),
                                         new Color(1.0f, 1.0f, 1.0f, 1.0f),
                                         new Vector2(0.25f, 0.0f)
                             );

                    gameState.obtainParticleEmitter()
                             .spawnBurst(gameState.getCurrentTime(),
                                         10,
                                         position,
                                         0.0f,
                                         new Vector2(entity.facingRight ? 1.0f : -1.0f, 0.0f),
                                         0.125f,
                                         0.5f, 2.5f,
                                         1f, 1f,
                                         0.1f, 0.2f,
                                         1.0f, 0.9f,
                                         0.0f, 0.0f,
                                         new Color(1.0f, 0.0f, 0.0f, 1.0f),
                                         new Color(1.0f, 1.0f, 0.0f, 1.0f),
                                         Vector2.Zero
                             );
                });

                entity.attackTimer = timers.set(entity.attackDuration, false, () -> {
                    entity.attacking = false;
                    timers.clear(entity.shotTimer);
                });
            }

            // Jump
            if (entity.input.jump() && entity.grounded) {
                entity.body().setLinearVelocity(entity.body().getLinearVelocity().x, entity.jumpForce);
                entity.grounded = false;
            }
        });
    }

    private static void limitMaxHorizontalSpeed(final Player entity) {
        final var velocity = entity.body().getLinearVelocity();

        final float maxSpeed = 5.0f;
        if (Math.abs(velocity.x) > maxSpeed) {
            entity.body()
                  .setLinearVelocity(maxSpeed * Math.signum(velocity.x),
                                     velocity.y);
        }
    }

    private static void applyInputForceAndFriction(
            final Player entity
    ) {
        final float movementForce = 100.0f;
        final float accelerationForce = movementForce * (entity.grounded
                ? 1.0f
                : 0.5f);
        final float deceleration = 0.05f * (entity.attacking ? 2.5f : 0.0f);
        final float baseFriction = 0.05f;

        final var position = entity.body().getPosition();

        final var velocity = entity.body().getLinearVelocity();
        final var slowAmount = Math.max(baseFriction, Math.abs(velocity.x) * deceleration) * entity.body().getMass();

        final var noHorizontalInput = entity.attacking || Math.abs(entity.input.horizontalInput()) < EPSILON;
        final var isAlmostStill = Math.abs(velocity.x) * entity.body().getMass() < slowAmount;
        if (noHorizontalInput && isAlmostStill) {
            entity.body().setLinearVelocity(0.0f, velocity.y);
        } else {
            final var inputImpulseX = noHorizontalInput
                    ? slowAmount * Math.signum(velocity.x) * -1.0f
                    : entity.input.horizontalInput() * accelerationForce;

            entity.body()
                  .applyLinearImpulse(inputImpulseX, 0.0f,
                                      position.x, position.y,
                                      true);
        }
    }
}
