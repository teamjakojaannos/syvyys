package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.TimerHandle;

public class SoulTrap extends TracksPlayerContact.Simple implements Entity {
    public static final float SIZE = 2.0f;

    private final Body body;

    public State state;
    public TimerHandle stateTimer;
    public TimerHandle damageTimer;
    public float bubblingDuration;
    public float attackDuration;
    public float damageTickInterval;

    public SoulTrap(final Body body) {
        this.body = body;
        this.state = State.IDLE;
        this.bubblingDuration = 1.0f;
        this.attackDuration = 2.0f;
        this.damageTickInterval = 0.1f;
    }

    public Body body() {
        return this.body;
    }

    public float width() {
        return SIZE;
    }

    public float height() {
        return SIZE;
    }

    public boolean isBubbling() {
        return this.state == State.BUBBLING;
    }

    public static SoulTrap create(final World physicsWorld, final Vector2 position) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position).add(SIZE / 2.0f, -0.25f);
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(SIZE / 2.25f);

        final var hbFixture = new FixtureDef();
        hbFixture.shape = hitBox;
        hbFixture.isSensor = true;
        body.createFixture(hbFixture);
        hitBox.dispose();

        final var trap = new SoulTrap(body);
        trap.body.setUserData(trap);
        return trap;
    }

    public enum State {
        BUBBLING,
        IDLE,
        I_WANT_OUT,
        I_WANT_OUT_START
    }
}
