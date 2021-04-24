package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Demon implements Entity {
    private final Body body;
    private final float width;
    private final float height;

    public Demon(final Body body) {
        this.width = 1.0f;
        this.height = 1.0f;
        this.body = body;
    }

    public Body body() {
        return this.body;
    }

    public float width() {
        return this.width;
    }

    public float height() {
        return this.height;
    }

    public static Demon create(final World physicsWorld, final Vector2 position) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
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

        final var demon = new Demon(body);
        demon.body.setUserData(demon);
        return demon;
    }
}
