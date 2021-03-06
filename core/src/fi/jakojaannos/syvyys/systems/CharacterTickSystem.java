package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.*;

import java.util.stream.Stream;


public class CharacterTickSystem implements EcsSystem<CharacterTickSystem.InputEntity> {
    private static final float EPSILON = 0.0001f;

    @Override
    public void tick(final Stream<InputEntity> entities, final GameState gameState) {
        entities.filter(inputEntity -> !inputEntity.inputDisabled(gameState))
                .forEach(entity -> {
                    entity.distanceTravelled(entity.distanceTravelled() + Math.abs(entity.body().getLinearVelocity().x));

                    applyInputForceAndFriction(entity);
                    if (!(entity instanceof Player player) || !player.isDashing(gameState)) {
                        limitMaxHorizontalSpeed(entity);
                    }

                    final var velocity = entity.body().getLinearVelocity();

                    final boolean isMoving = Math.abs(velocity.x) > 0.0f;
                    if (Math.abs(entity.input().horizontalInput()) > 0.0f && isMoving && !entity.attacking()) {
                        entity.facingRight(entity.input().horizontalInput() > 0.0f);
                    }

                    // Attack
                    tickAttack(gameState, entity);

                    // Jump
                    if (entity.input().jump() && entity.grounded()) {
                        entity.body().setLinearVelocity(entity.body().getLinearVelocity().x, entity.jumpForce());
                        entity.grounded(false);
                    }
                });
    }

    private void tickAttack(final GameState gameState, final InputEntity entity) {
        if (!entity.input().attack() || entity.attacking()) {
            return;
        }

        entity.attacking(true);

        final var timers = gameState.getTimers();
        entity.attackDelayTimer(timers.set(entity.attackDelay(), false, () -> {
            final var shotDuration = entity.attackDuration() / entity.shotsPerAttack();

            entity.shotTimer(timers.set(shotDuration, true, () -> {
                if (entity instanceof Player player) {
                    Player.tickAttack(gameState, player);
                } else if (entity instanceof Demon demon) {
                    Demon.tickAttack(gameState, demon);
                } else if (entity instanceof Hellspider hellspider) {
                    Hellspider.tickAttack(gameState, hellspider);
                } else if (entity instanceof Golem golem) {
                    Golem.tickAttack(gameState, golem);
                } else {
                    throw new IllegalStateException("Not implemented for " + entity.getClass().getSimpleName());
                }

                if (entity.checkShouldContinueShootingAfterShot()) {
                    entity.attacking(false);
                    timers.clear(entity.shotTimer());
                }
            }, true));
        }));
    }

    private static void limitMaxHorizontalSpeed(final InputEntity entity) {
        final var velocity = entity.body().getLinearVelocity();

        final float maxSpeed = entity.maxSpeed();
        if (Math.abs(velocity.x) > maxSpeed) {
            entity.body()
                  .setLinearVelocity(maxSpeed * Math.signum(velocity.x),
                                     velocity.y);
        }
    }

    private static void applyInputForceAndFriction(
            final InputEntity entity
    ) {
        final float movementForce = 100.0f;
        final float accelerationForce = movementForce * (entity.grounded()
                ? 1.0f
                : 0.5f);
        final float deceleration = 0.05f * (entity.attacking() ? 2.5f : 1.0f);
        final float baseFriction = 0.05f;

        final var position = entity.body().getPosition();

        final var velocity = entity.body().getLinearVelocity();
        final var slowAmount = Math.max(baseFriction, Math.abs(velocity.x) * deceleration) * entity.body().getMass();

        final var noHorizontalInput = entity.attacking() || Math.abs(entity.input().horizontalInput()) < EPSILON;
        final var isAlmostStill = Math.abs(velocity.x) * entity.body().getMass() < slowAmount;
        if (noHorizontalInput && isAlmostStill) {
            entity.body().setLinearVelocity(0.0f, velocity.y);
        } else {
            final var inputImpulseX = noHorizontalInput
                    ? slowAmount * Math.signum(velocity.x) * -1.0f
                    : entity.input().horizontalInput() * accelerationForce;

            entity.body()
                  .applyLinearImpulse(inputImpulseX, 0.0f,
                                      position.x, position.y,
                                      true);
        }
    }

    public interface InputEntity extends Entity, HasBody, HasCharacterState, HasCharacterInput {}
}
