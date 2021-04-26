package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.SyvyysGame;

public class DemonBall implements Entity, HasBody {
    public final Vector2 direction;
    public final float timeStamp;
    public final float maxLifeTime;
    private final Body body;
    public float acceleration;
    public boolean collidedWithWall;
    public final float damage;
    public boolean isInContactWithPlayer;

    public DemonBall(
            final Body body,
            final Vector2 direction,
            final float acceleration,
            final float timeStamp,
            final float maxLifeTime,
            final float damage
    ) {
        this.body = body;
        this.direction = direction;
        this.acceleration = acceleration;
        this.timeStamp = timeStamp;
        this.maxLifeTime = maxLifeTime;
        this.damage = damage;
    }

    @Override
    public Body body() {
        return this.body;
    }

    public float width() {
        return 0.5f;
    }

    public float height() {
        return 0.5f;
    }

    public static DemonBall create(
            final World physicsWorld,
            final Vector2 position,
            final Vector2 direction,
            final float speed,
            final float timeStamp,
            final float maxLifeTime,
            final float damage
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.gravityScale = 0.0f;
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(0.125f);
        final var fixture = new FixtureDef();
        fixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_PROJECTILE_ENEMY;
        fixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_PROJECTILE_ENEMY;
        fixture.isSensor = true;
        fixture.shape = hitBox;
        fixture.density = 100.0f;
        fixture.friction = 0.0f;
        fixture.restitution = 0.0f;
        body.createFixture(fixture);
        hitBox.dispose();

        final var projectile = new DemonBall(body, direction, speed, timeStamp, maxLifeTime, damage);
        projectile.body.setUserData(projectile);
        return projectile;
    }
}
