package fi.jakojaannos.syvyys;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public final class Player implements Entity {
    public final float attackDuration = 1.0f;
    private final float jumpForce = 5.0f;
    private final float width;
    private final float height;
    private final Body body;

    public float distanceTravelled;
    public boolean grounded;
    public Input input;
    public boolean facingRight;
    public boolean attacking;

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
            final float movementForce = 100.0f;
            final float accelerationForce = movementForce * (entity.grounded
                    ? 1.0f
                    : 0.5f);

            final var position = entity.body().getPosition();

            final var inputImpulseX = entity.input.horizontalInput * accelerationForce;

            // TODO: if h move input == nearly zero, decelerate
            entity.body()
                  .applyLinearImpulse(inputImpulseX, 0.0f,
                                      position.x, position.y,
                                      true);

            final float maxSpeed = 5.0f;
            final var velocity = entity.body().getLinearVelocity();
            if (Math.abs(velocity.x) > maxSpeed) {
                entity.body()
                      .setLinearVelocity(maxSpeed * Math.signum(velocity.x),
                                         velocity.y);
            }

            entity.distanceTravelled += Math.abs(velocity.x);

            if (Math.abs(entity.input.horizontalInput) > 0.0f) {
                entity.facingRight = velocity.x > 0.0f;
            }

            // Attack
            if (entity.input.attack && !entity.attacking) {
                entity.attacking = true;

                final var timers = gameState.getTimers();
                timers.set(entity.attackDuration, false, () -> {
                    entity.attacking = false;
                });
            }

            // Jump
            if (entity.input.jump) {
                entity.body.setLinearVelocity(velocity.x, entity.jumpForce);
            }
        });
    }

    public float width() { return this.width; }

    public float height() { return this.height; }

    public Body body() { return this.body; }

    public static record Input(float horizontalInput, boolean attack, boolean jump) {}
}
