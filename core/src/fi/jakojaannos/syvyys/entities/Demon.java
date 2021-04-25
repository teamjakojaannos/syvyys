package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.GameState;

public class Demon extends GameCharacter {
    public final float maxChaseDistance = 50f;
    public final float attackDistance = 5f;


    public Demon(final Body body) {
        super(body);
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
        demon.body().setUserData(demon);
        return demon;
    }

    public static void tickAttack(final GameState gameState, final Demon demon) {
        final var position = demon.body().getPosition();

        gameState.obtainParticleEmitter()
                 .spawnBurst(gameState.getCurrentTime(),
                             5,
                             position,
                             0.01f,
                             new Vector2(0, 1.0f),
                             0.5f,
                             0.02f, 0.2f,
                             0.5f, 10.0f,
                             0.5f, 0.75f,
                             0.0f, 0.125f,
                             new Color(0.75f, 0.75f, 0.75f, 1.0f),
                             new Color(0.75f, 0.75f, 0.75f, 1.0f),
                             new Vector2(0, 0.05f)
                 );
    }
}