package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import fi.jakojaannos.syvyys.TimerHandle;

public final class Player implements Entity {
    public final float attackDuration = 0.6f;
    public final int shotsPerAttack = 3;
    public final float jumpForce = 10.0f;
    public final float width;
    public final float height;

    private final Body body;

    public float distanceTravelled;
    public boolean grounded;
    public Input input;
    public boolean facingRight;
    public boolean attacking;

    public TimerHandle attackTimer;
    public TimerHandle shotTimer;

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

    public float width() { return this.width; }

    public float height() { return this.height; }

    public Body body() { return this.body; }

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

    public static record Input(float horizontalInput, boolean attack, boolean jump) {}
}
