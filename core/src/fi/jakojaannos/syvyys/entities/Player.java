package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;

public final class Player extends GameCharacter {
    public Player(final Body body) {
        super(body,
              1.0f, 1.0f,
              10.0f,
              100.0f,
              0.6f, 3,
              3.0f);
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
        hbFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_PLAYER;
        hbFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_PLAYER;
        hbFixture.shape = hitBox;
        hbFixture.density = 80.0f;
        hbFixture.friction = 0.15f;
        hbFixture.restitution = 0.0f;
        body.createFixture(hbFixture);
        hitBox.dispose();

        final var player = new Player(body);
        player.body().setUserData(player);
        return player;
    }

    public static void tickAttack(final GameState gameState, final Player entity) {
        final var barrelOffsetX = 0.33f * (entity.facingRight ? 1 : -1);
        final var position = new Vector2(entity.body().getPosition())
                .add(barrelOffsetX, -0.15f);

        final var facingVec = new Vector2(entity.facingRight ? 1.0f : -1.0f, 0.0f);

        final var gunRange = 10.0f;
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
            if (SyvyysGame.Constants.DEBUG_ATTACK_RAYCAST) {
                System.out.println("Hit:\t" + fixture.toString());
                System.out.println("Point:\t" + point.toString());
                System.out.println("Player:\t" + position.toString());
                System.out.println("Fraction:\t" + fraction);
                System.out.println("isSensor:\t" + fixture.isSensor());
            }

            // Ignore any sensors
            if (fixture.isSensor()) {
                return -1;
            }

            // Ignore corpses
            if (fixture.getBody().getUserData() instanceof HasHealth killable && killable.dead()) {
                return -1;
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
                demon.dealDamage(99999.0f);
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
    }

    private static class HitInfo {
        public final Vector2 closestPoint = new Vector2();
        public final Vector2 normal = new Vector2();
        public Body body;
        public float closestFraction = 1.0f;
        public boolean thereWasAHit = false;
    }
}
