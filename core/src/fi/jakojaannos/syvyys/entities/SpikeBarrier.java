package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.Timers;

public class SpikeBarrier implements Entity {
    public final Body body;
    public final TimerHandle closeProgress;

    public SpikeBarrier(final Body body, final TimerHandle closeProgress) {
        this.body = body;
        this.closeProgress = closeProgress;
    }

    public static SpikeBarrier create(
            final World physicsWorld,
            final Vector2 position,
            final Timers timers
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new PolygonShape();
        hitBox.setAsBox(0.25f, 1.0f);

        final var hbFixture = new FixtureDef();
        hbFixture.shape = hitBox;
        hbFixture.density = 0.0f;
        hbFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_TERRAIN;
        hbFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_TERRAIN;

        body.createFixture(hbFixture);
        hitBox.dispose();

        final var barrier = new SpikeBarrier(body, timers.set(0.5f, false, () -> {}));
        barrier.body.setUserData(barrier);
        return barrier;
    }
}
