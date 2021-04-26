package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;

public class Golem extends GameCharacter {

    public Golem(
            final Body body
    ) {
        super(body,
              1.0f, 2.0f,
              0.0f,
              50.0f,
              3.0f, 1,
              0.5f,
              1.0f,
              1.5f, 50);
    }

    public static Golem create(final World physicsWorld, final Vector2 position) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(0.5f);

        final var hbFixture = new FixtureDef();
        hbFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_ENEMY;
        hbFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_ENEMY;
        hbFixture.shape = hitBox;
        hbFixture.density = 400.0f;
        hbFixture.friction = 0.15f;
        hbFixture.restitution = 0.0f;
        body.createFixture(hbFixture);
        hitBox.dispose();

        final var golem = new Golem(body);
        golem.body().setUserData(golem);
        return golem;
    }

    public static void tickAttack(final GameState state, final Golem golem) {
        final var a = new boolean[]{true, false};
        for (final var right : a) {
            final var entities = SpikeNode.spawnSpikeStrip(
                    state.getPhysicsWorld(),
                    new Vector2(golem.body().getPosition()),
                    0.5f, 1.0f,
                    8,
                    0.75f,
                    2.5f,
                    0.7f,
                    right
            );
            entities.forEach(state::spawn);
        }
    }

}
