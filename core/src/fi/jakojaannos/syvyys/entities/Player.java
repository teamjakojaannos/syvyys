package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.TimerHandle;

public final class Player implements Entity {
    private static final float EPSILON = 0.0001f;

    public final float attackDuration = 0.6f;
    private final int shotsPerAttack = 3;

    private final float jumpForce = 5.0f;
    private final float width;
    private final float height;
    private final Body body;

    public float distanceTravelled;
    public boolean grounded;
    public Input input;
    public boolean facingRight;
    public boolean attacking;

    private TimerHandle attackTimer;

    public Player(
            final float width,
            final float height,
            final Body body
    ) {
        this.width = width;
        this.height = height;
        this.body = body;
        this.distanceTravelled = 0.0f;
        this.facingRight = true;
        this.grounded = true;
        this.input = new Input(0.0f, false, false);
    }

    public static Player create(final World physicsWorld, final Vector2 position) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(0.5f);

        final var hbFixture = new FixtureDef();
        hbFixture.shape = hitBox;
        hbFixture.density = 80.0f;
        hbFixture.friction = 0.15f;
        hbFixture.restitution = 0.0f;
        body.createFixture(hbFixture);
        hitBox.dispose();

        final var player = new Player(1.0f, 1.0f, body);
        player.body.setUserData(player);
        return player;
    }

    public static void tick(final Iterable<Player> entities, final GameState gameState) {
        entities.forEach(entity -> {
            applyInputForceAndFriction(entity);
            limitMaxHorizontalSpeed(entity);

            final var velocity = entity.body().getLinearVelocity();
            entity.distanceTravelled += Math.abs(velocity.x);

            final boolean isMoving = Math.abs(velocity.x) > 0.0f;
            if (Math.abs(entity.input.horizontalInput) > 0.0f && isMoving) {
                entity.facingRight = velocity.x > 0.0f;
            }

            // Attack
            if (entity.input.attack && !entity.attacking) {
                entity.attacking = true;

                final var timers = gameState.getTimers();
                entity.attackTimer = timers.set(entity.attackDuration / entity.shotsPerAttack, true, () -> {
                    System.out.println("BANG!");
                });

                timers.set(entity.attackDuration, false, () -> {
                    entity.attacking = false;
                    timers.clear(entity.attackTimer);
                });
            }

            // Jump
            if (entity.input.jump && entity.grounded) {
                entity.body.setLinearVelocity(entity.body().getLinearVelocity().x, entity.jumpForce);
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
        final float deceleration = 0.05f;
        final float baseFriction = 0.05f;

        final var position = entity.body().getPosition();

        final var velocity = entity.body().getLinearVelocity();
        final var slowAmount = Math.max(baseFriction, Math.abs(velocity.x) * deceleration) * entity.body().getMass();

        final var noHorizontalInput = entity.attacking || Math.abs(entity.input.horizontalInput) < EPSILON;
        final var isAlmostStill = Math.abs(velocity.x) * entity.body().getMass() < slowAmount;
        if (noHorizontalInput && isAlmostStill) {
            entity.body().setLinearVelocity(0.0f, velocity.y);
        } else {
            final var inputImpulseX = noHorizontalInput
                    ? slowAmount * Math.signum(velocity.x) * -1.0f
                    : entity.input.horizontalInput * accelerationForce;

            entity.body()
                  .applyLinearImpulse(inputImpulseX, 0.0f,
                                      position.x, position.y,
                                      true);
        }
    }

    public float width() { return this.width; }

    public float height() { return this.height; }

    public Body body() { return this.body; }

    public static record Input(float horizontalInput, boolean attack, boolean jump) {}
}
