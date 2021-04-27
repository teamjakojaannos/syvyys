package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.TimerHandle;

public class Boss implements Entity, HasHealth {
    public static Boss INSTANCE;

    public final Body head;
    public final Body handL;
    public final Body handR;

    public TimerHandle introTimer;
    private boolean introFinished;
    private float health;
    private float maxHealth;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public Boss(final Body head, final Body handL, final Body handR, GameState gameState, int circleN) {
        this.head = head;
        this.handL = handL;
        this.handR = handR;

        this.introTimer = gameState.getTimers().set(3.0f, false, () -> this.introFinished = true);

        this.health = this.maxHealth = circleN * 1000.0f;

        INSTANCE = this;
    }


    public float introProgress(final GameState gameState) {
        if (this.introFinished) {
            return 1.0f;
        }

        return gameState.getTimers().isActiveAndValid(this.introTimer)
                ? gameState.getTimers().getTimeElapsed(this.introTimer) / this.introTimer.duration()
                : 0.0f;
    }

    @Override
    public void dealDamage(float amount, GameState gameState) {
        this.health -= amount;
    }

    @Override
    public float maxHealth() {
        return this.maxHealth;
    }

    @Override
    public float health() {
        return this.health;
    }

    @Override
    public void health(float health) {

    }

    @Override
    public boolean dead() {
        return false;
    }

    @Override
    public TimerHandle deathTimer() {
        return null;
    }

    @Override
    public void deathTimer(TimerHandle timer) {

    }

    @Override
    public float deathAnimationDuration() {
        return 0;
    }

    @Override
    public boolean deathSequenceHasFinished() {
        return false;
    }

    @Override
    public void deathSequenceHasFinished(boolean value) {

    }

    public static Boss create(
            final World physicsWorld,
            final GameState gameState,
            int circleN
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(20.0f, 5.0f));//.sub(width / 2.0f, height / 2.0f);
        bodyDef.fixedRotation = true;

        final var head = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(2.0f);

        final var hbFixture = new FixtureDef();
        hbFixture.shape = hitBox;
        hbFixture.density = 0.0f;
        hbFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_ENEMY;
        hbFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_ENEMY;

        head.createFixture(hbFixture);

        final var handFixture = new FixtureDef();
        handFixture.shape = hitBox;
        handFixture.isSensor = true;
        handFixture.density = 0.0f;
        handFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_ENEMY;
        handFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_ENEMY;

        hitBox.setRadius(1.0f);

        bodyDef.position.add(1.5f, -1.5f);
        final var handL = physicsWorld.createBody(bodyDef);
        handL.createFixture(handFixture);

        bodyDef.position.add(-3.0f, 0.0f);
        final var handR = physicsWorld.createBody(bodyDef);
        handR.createFixture(handFixture);

        hitBox.dispose();

        final var boss = new Boss(head, handL, handR, gameState, circleN);
        boss.head.setUserData(boss);
        return boss;
    }
}
