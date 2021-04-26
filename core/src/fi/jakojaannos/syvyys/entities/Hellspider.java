package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;

public class Hellspider extends GameCharacter implements HasEnemyAI {
    public final float maxChaseDistance = 15f;
    public final float attackDistance = 3.5f;
    public final float attackDamage = 10.0f;
    private final float attackJumpStrength = 12.5f;
    private final float attackLeapStrength = 30.0f;
    public boolean justAttacked;
    public boolean justLeaped;
    public boolean justDashed;

    public State state = State.RUNNING;
    public boolean isInContactWithPlayer;

    public Hellspider(final Body body) {
        super(body,
              1.0f, 1.0f,
              5.0f,
              10.0f,
              3.0f, 2,
              1.0f,
              1.5f);
    }

    @Override
    public float maxChaseDistance() {
        return this.maxChaseDistance;
    }

    @Override
    public float attackDistance() {
        return this.attackDistance;
    }

    @Override
    public boolean canAttackFromDifferentY() {
        return true;
    }

    @Override
    public boolean shouldTickAi() {
        return this.state == State.RUNNING;
    }

    @Override
    public float maxSpeed() {
        if (this.state == State.RUNNING) {
            return super.maxSpeed();
        }

        return Float.MAX_VALUE;
    }

    public static Hellspider create(final World physicsWorld, final Vector2 position) {
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
        hbFixture.density = 40.0f;
        hbFixture.friction = 0.15f;
        hbFixture.restitution = 0.0f;
        body.createFixture(hbFixture);
        hitBox.dispose();

        final var hellspider = new Hellspider(body);
        hellspider.body().setUserData(hellspider);
        return hellspider;
    }

    public static void tickAttack(final GameState gameState, final Hellspider hellspider) {
        final var timers = gameState.getTimers();
        final var position = hellspider.body().getPosition();

        hellspider.input(new CharacterInput(0.0f, false, false));
        if (hellspider.state == State.RUNNING) {
            hellspider.justLeaped = true;

            hellspider.state = State.LEAPING;

            gameState.obtainParticleEmitter()
                     .spawnBurst(gameState.getCurrentTime(),
                                 10,
                                 position,
                                 0.01f,
                                 new Vector2(0, 1.0f),
                                 2.0f,
                                 0.1f, 0.25f,
                                 0.5f, 2.0f,
                                 0.5f, 0.75f,
                                 0.0f, 0.125f,
                                 new Color(0.75f, 0.1f, 0.1f, 1.0f),
                                 new Color(0.25f, 0.1f, 0.1f, 1.0f),
                                 new Vector2(0, 0.05f)
                     );

            final var dirToPlayer = gameState
                    .getPlayer()
                    .map(GameCharacter::body)
                    .map(Body::getPosition)
                    .map(playerPos -> new Vector2(playerPos).sub(position))
                    .orElseGet(() -> new Vector2().setToRandomDirection());

            final var h = dirToPlayer.x < 0 ? -1.0f : 1.0f;
            hellspider.body().applyLinearImpulse(new Vector2(h * 0.25f, 1.0f).scl(hellspider.attackJumpStrength * hellspider.body().getMass()),
                                                 position, true);
            hellspider.grounded(false);

            final var pauseTime = hellspider.attackDuration() / 8.0f;
            timers.set(pauseTime, false, () -> {
                hellspider.body().setLinearVelocity(0.0f, 0.0f);
                hellspider.body().setGravityScale(0.0f);
            });
        } else if (hellspider.state == State.LEAPING) {
            hellspider.justDashed = true;
            hellspider.body().setGravityScale(0.25f);

            final var dirToPlayer = gameState
                    .getPlayer()
                    .map(GameCharacter::body)
                    .map(Body::getPosition)
                    .map(playerPos -> new Vector2(playerPos).sub(position).nor())
                    .orElseGet(() -> new Vector2().setToRandomDirection());

            hellspider.body().setLinearVelocity(0.0f, 0.0f);
            final var impulse = new Vector2(dirToPlayer).scl(hellspider.attackLeapStrength * hellspider.body().getMass());
            hellspider.body().applyLinearImpulse(impulse, position, true);

            hellspider.state = State.DASHING;

            timers.set(0.5f, false, () -> {
                hellspider.state = State.RUNNING;
                hellspider.body().setGravityScale(1.0f);
            });
        }
    }

    public enum State {
        RUNNING,
        LEAPING,
        DASHING
    }
}
