package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Demon;
import fi.jakojaannos.syvyys.entities.Player;

public class CharacterTickSystem implements EcsSystem<Player> {
    private static final boolean DEBUG_ATTACK_RAYCAST = false;
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
            tickAttack(gameState, entity);

            // Jump
            if (entity.input.jump() && entity.grounded) {
                entity.body().setLinearVelocity(entity.body().getLinearVelocity().x, entity.jumpForce);
                entity.grounded = false;
            }
        });
    }

    private void tickAttack(final GameState gameState, final Player entity) {
        if (!entity.input.attack() || entity.attacking) {
            return;
        }

        entity.attacking = true;

        final var timers = gameState.getTimers();
        entity.shotTimer = timers.set(entity.attackDuration / entity.shotsPerAttack, true, () -> {
            final var barrelOffsetX = 0.33f * (entity.facingRight ? 1 : -1);
            final var position = new Vector2(entity.body().getPosition())
                    .add(barrelOffsetX, -0.15f);

            final var facingVec = new Vector2(entity.facingRight ? 1.0f : -1.0f, 0.0f);

            final var gunRange = 100.0f;
            final var rayEnd = new Vector2(facingVec)
                    .scl(gunRange)
                    .add(position);
            final var temp = new Vector2(facingVec).scl(-1);

            final var hitInfo = new HitInfo();
            hitInfo.closestFraction = 1.0f;
            hitInfo.closestPoint.set(rayEnd);
            hitInfo.normal.set(facingVec).scl(-1.0f);
            hitInfo.thereWasAHit = false;
            gameState.getPhysicsWorld().rayCast((fixture, point, normal, fraction) -> {
                if (DEBUG_ATTACK_RAYCAST) {
                    System.out.println("Hit:\t" + fixture.toString());
                    System.out.println("Point:\t" + point.toString());
                    System.out.println("Player:\t" + position.toString());
                    System.out.println("Fraction:\t" + fraction);
                }

                hitInfo.thereWasAHit = true;
                if (fraction < hitInfo.closestFraction) {
                    hitInfo.closestPoint.set(point);
                    hitInfo.normal.set(normal);
                    hitInfo.closestFraction = fraction;
                    hitInfo.body = fixture.getBody();
                }

                return 1;
            }, position, rayEnd);

            if (hitInfo.thereWasAHit && hitInfo.body != null) {
                if (hitInfo.body.getUserData() instanceof Demon demon) {
                    gameState.deletThis(demon);
                }

                gameState.obtainParticleEmitter()
                         .spawnBurst(gameState.getCurrentTime(),
                                     5,
                                     hitInfo.closestPoint,
                                     0.05f,
                                     temp,
                                     0.125f * 10,
                                     0.5f, 2.5f,
                                     0.05f, 0.3f,
                                     1.0f, 0.9f,
                                     0.0f, 0.0f,
                                     new Color(0.75f, 0.00f, 0.00f, 1.0f),
                                     new Color(0.75f, 0.00f, 0.00f, 1.0f),
                                     new Vector2(0, -0.25f)
                         );
            }

            // Calm smoke
            final var wind = 0.05f;
            gameState.obtainParticleEmitter()
                     .spawnBurst(gameState.getCurrentTime(),
                                 5,
                                 position,
                                 0.01f,
                                 facingVec,
                                 0.025f,
                                 0.0f, 0.025f,
                                 2.5f, 10.0f,
                                 0.5f, 0.75f,
                                 0.0f, 0.125f,
                                 new Color(0.75f, 0.75f, 0.75f, 1.0f),
                                 new Color(0.75f, 0.75f, 0.75f, 1.0f),
                                 new Vector2(wind, 0.25f)
                     );

            // Burst smoke
            gameState.obtainParticleEmitter()
                     .spawnBurst(gameState.getCurrentTime(),
                                 10,
                                 position,
                                 0.01f,
                                 facingVec,
                                 0.25f,
                                 0.1f, 0.75f,
                                 2.5f, 5.0f,
                                 0.5f, 0.75f,
                                 0.0f, 0.125f,
                                 new Color(1.0f, 1.0f, 1.0f, 1.0f),
                                 new Color(1.0f, 1.0f, 1.0f, 1.0f),
                                 new Vector2(wind, 0.25f)
                     );

            // Muzzle flash
            gameState.obtainParticleEmitter()
                     .spawnBurst(gameState.getCurrentTime(),
                                 10,
                                 position,
                                 0.0f,
                                 facingVec,
                                 0.125f,
                                 0.5f, 2.5f,
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

    private static class HitInfo {
        public final Vector2 closestPoint = new Vector2();
        public final Vector2 normal = new Vector2();
        public Body body;
        public float closestFraction = 1.0f;
        public boolean thereWasAHit = false;
    }
}
