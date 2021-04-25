package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Projectile implements Entity {
    public final Vector2 direction;
    public final float timeStamp;
    public final float maxLifeTime;
    private final Body body;
    public float speed;

    public Projectile(
            final Body body,
            final Vector2 direction,
            final float speed,
            final float timeStamp,
            final float maxLifeTime
    ) {
        this.body = body;
        this.direction = direction;
        this.speed = speed;
        this.timeStamp = timeStamp;
        this.maxLifeTime = maxLifeTime;
    }

    public static Projectile create(
            final World physicsWorld,
            final Vector2 position,
            final Vector2 direction,
            final float speed,
            final float timeStamp,
            final float maxLifeTime
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.gravityScale = 0.0f;
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(0.25f);
        final var fixture = new FixtureDef();
        fixture.shape = hitBox;
        fixture.density = 0.0f;
        fixture.friction = 0.0f;
        fixture.restitution = 0.0f;
        body.createFixture(fixture);
        hitBox.dispose();

        final var projectile = new Projectile(body, direction, speed, timeStamp, maxLifeTime);
        projectile.body.setUserData(projectile);
        return projectile;
    }
}
