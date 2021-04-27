package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.util.RayCast;

public final class Player extends GameCharacter {
    public final float dashCoolDown = 3.0f;
    public float weaponSelfKnockback = 75.0f;
    private final float damageKnockbackStrength = 100.0f;
    private final float damageStaggerDuration = 0.1f;
    public float dashStrength = 75.0f;
    public float meNoDieTime = 0.5f;
    public float dashDuration = 0.25f;
    public TimerHandle dashTimer;
    public TimerHandle dashCooldownTimer;
    public TimerHandle meNoDieTimer;
    public boolean justAttacked;
    public boolean isHoldingAttack;
    public float damage = 5.0f;
    public boolean dashUnlocked;
    public boolean justHitSomething;
    private AbilityInput abilityInput = new AbilityInput(false);

    public Player(final Body body) {
        super(body,
              1.0f, 1.0f,
              10.0f,
              100.0f,
              0.4f, 3,
              0.1f,
              3.0f,
              3.0f,
              0);
    }

    public AbilityInput abilityInput() {
        return this.abilityInput;
    }

    public void abilityInput(final AbilityInput abilityInput) {
        this.abilityInput = abilityInput;
    }

    public boolean isDashing(final GameState gameState) {
        return gameState.getTimers().isActiveAndValid(this.dashTimer);
    }

    public boolean canDash(final GameState gameState) {
        return !gameState.getTimers().isActiveAndValid(this.dashCooldownTimer);
    }

    public boolean isInvulnerable(final GameState gameState) {
        return gameState.getTimers().isActiveAndValid(this.meNoDieTimer);
    }

    @Override
    public boolean inputDisabled(final GameState gameState) {
        return isDashing(gameState) || super.inputDisabled(gameState);
    }

    @Override
    public void dealDamage(final float amount, final GameState gameState) {
        if (isInvulnerable(gameState)) {
            return;
        }

        super.dealDamage(amount, gameState);
    }

    public static Player copyFrom(final World physicsWorld, final Vector2 position, final Player other) {
        final var player = Player.create(physicsWorld, position);

        player.health(other.health());
        player.maxHealth = other.maxHealth;

        player.maxSpeed = other.maxSpeed;
        player.jumpForce = other.jumpForce;

        player.dashUnlocked = other.dashUnlocked;
        player.meNoDieTime = other.meNoDieTime;
        player.dashStrength = other.dashStrength;

        player.damage = other.damage;
        player.shotsPerAttack(other.shotsPerAttack());

        return player;
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
        entity.justAttacked = true;

        final var barrelOffsetX = 0.33f * (entity.facingRight ? 1 : -1);
        final var position = new Vector2(entity.body().getPosition())
                .add(barrelOffsetX, -0.15f);

        final var facingVec = new Vector2(entity.facingRight ? 1.0f : -1.0f, 0.0f);

        final var gunRange = 10.0f;
        final var rayEnd = new Vector2(facingVec)
                .scl(gunRange)
                .add(position);
        final var temp = new Vector2(facingVec).scl(-1);

        entity.body().applyLinearImpulse(new Vector2(temp).scl(entity.weaponSelfKnockback), entity.body().getPosition(), true);

        final var hitInfo = RayCast.nearestHit(gameState.getPhysicsWorld(), position, rayEnd, RayCast.Filter.everything());
        hitInfo.normal.set(facingVec).scl(-1.0f); // Hackety hack :shrug:

        if (hitInfo.thereWasAHit && hitInfo.body != null) {
            final var target = hitInfo.body.getUserData();
            if (target instanceof HasHealth killable) {
                entity.justHitSomething = true;
                killable.dealDamage(entity.damage, gameState);
            }
            if (target instanceof HasCharacterInput characterInput) {
                characterInput.disableInput();
                gameState.getTimers().set(entity.damageStaggerDuration, false, characterInput::enableInput);
            }

            if (hitInfo.body.getType() == BodyType.DynamicBody) {
                hitInfo.body.applyLinearImpulse(new Vector2(facingVec).scl(entity.damageKnockbackStrength),
                                                hitInfo.closestPoint, true);
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

}
