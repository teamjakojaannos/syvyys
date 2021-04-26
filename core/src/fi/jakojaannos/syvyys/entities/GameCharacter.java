package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.systems.CharacterTickSystem;

public class GameCharacter extends TracksPlayerContact.Simple implements CharacterTickSystem.InputEntity, HasHealth {
    private final float attackDuration;
    private final float attackInitialDelay;
    private final float jumpForce;
    private final float width;
    private final float height;
    public float maxHealth;
    private final float deathAnimationDuration;
    private final Body body;
    private final int soulReward;
    public float maxSpeed;
    public float distanceTravelled = 0.0f;
    public float previousDistanceTravelled;
    public boolean grounded = false;
    public boolean facingRight = true;
    public boolean attacking;
    public TimerHandle attackTimer;
    public TimerHandle shotTimer;
    private int shotsPerAttack;
    private TimerHandle deathTimer;
    private TimerHandle attackDelayTimer;
    private CharacterInput input = new CharacterInput(0.0f, false, false);
    public float health;
    private boolean deathSequenceHasFinished;
    private int nShots;
    private boolean inputDisabled;

    public GameCharacter(
            final Body body,
            final float width,
            final float height,
            final float jumpForce,
            final float health,
            final float attackDuration,
            final int shotsPerAttack,
            final float attackInitialDelay,
            final float deathAnimationDuration,
            final float maxSpeed,
            final int soulReward
    ) {
        this.attackDuration = attackDuration;
        this.shotsPerAttack = shotsPerAttack;
        this.jumpForce = jumpForce;
        this.width = width;
        this.height = height;
        this.body = body;
        this.health = this.maxHealth = health;
        this.attackInitialDelay = attackInitialDelay;
        this.deathAnimationDuration = deathAnimationDuration;
        this.maxSpeed = maxSpeed;
        this.soulReward = soulReward;
    }

    public float width() { return this.width; }

    public float height() { return this.height; }

    public Body body() { return this.body; }

    @Override
    public CharacterInput input() {
        return this.input;
    }

    @Override
    public void input(final CharacterInput input) {
        this.input = input;
    }

    @Override
    public boolean inputDisabled(final GameState gameState) {
        return this.inputDisabled;
    }

    @Override
    public void disableInput() {
        this.inputDisabled = true;
    }

    @Override
    public void enableInput() {
        this.inputDisabled = false;
    }

    @Override
    public float distanceTravelled() {
        return this.distanceTravelled;
    }

    @Override
    public void distanceTravelled(final float value) {
        this.previousDistanceTravelled = this.distanceTravelled;
        this.distanceTravelled = value;
    }

    @Override
    public boolean attacking() {
        return this.attacking;
    }

    @Override
    public void attacking(final boolean value) {
        this.attacking = value;
    }

    @Override
    public boolean grounded() {
        return this.grounded;
    }

    @Override
    public void grounded(final boolean value) {
        this.grounded = value;
    }

    @Override
    public boolean facingRight() {
        return this.facingRight;
    }

    @Override
    public void facingRight(final boolean value) {
        this.facingRight = value;
    }

    @Override
    public float jumpForce() {
        return this.jumpForce;
    }

    @Override
    public float attackDuration() {
        return this.attackDuration;
    }

    @Override
    public int shotsPerAttack() {
        return this.shotsPerAttack;
    }

    @Override
    public TimerHandle shotTimer() {
        return this.shotTimer;
    }

    @Override
    public void shotTimer(final TimerHandle timer) {
        this.shotTimer = timer;
    }

    @Override
    public TimerHandle attackTimer() {
        return this.attackTimer;
    }

    @Override
    public void attackTimer(final TimerHandle timer) {
        this.attackTimer = timer;
    }

    @Override
    public float attackDelay() {
        return this.attackInitialDelay;
    }

    @Override
    public TimerHandle attackDelayTimer() {
        return this.attackDelayTimer;
    }

    @Override
    public void attackDelayTimer(final TimerHandle timer) {
        this.attackDelayTimer = timer;
    }

    @Override
    public void dealDamage(final float amount, GameState gameState) {
        //noinspection InstanceofThis
        if (this instanceof Player && SyvyysGame.Constants.SATANMODE) {
            return;
        }

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
    public void health(final float health) {
        this.health = health;
    }

    @Override
    public boolean hasMoved() {
        return this.distanceTravelled - this.previousDistanceTravelled > 0.05f;
    }

    @Override
    public boolean dead() {
        return this.health <= 0.00001f;
    }

    @Override
    public TimerHandle deathTimer() {
        return this.deathTimer;
    }

    @Override
    public void deathTimer(final TimerHandle timer) {
        this.deathTimer = timer;
    }

    @Override
    public float deathAnimationDuration() {
        return this.deathAnimationDuration;
    }

    @Override
    public boolean deathSequenceHasFinished() {
        return this.deathSequenceHasFinished;
    }

    @Override
    public void deathSequenceHasFinished(final boolean value) {
        this.deathSequenceHasFinished = value;
    }

    @Override
    public void onDeadCallback(GameState gameState) {
        gameState.souls += this.soulReward;
    }

    @Override
    public void onStartDeathSequenceCallback(final GameState gameState) {
        body().getFixtureList()
              .forEach(fixture -> {
                  final var filter = new Filter();
                  filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_CORPSE;
                  filter.maskBits = SyvyysGame.Constants.Collision.MASK_CORPSE;
                  fixture.setFilterData(filter);
              });

        final var timers = gameState.getTimers();
        timers.clear(this.attackDelayTimer);
        timers.clear(this.attackTimer);
        timers.clear(this.shotTimer);
        timers.clear(this.deathTimer);
    }

    @Override
    public float maxSpeed() {
        return this.maxSpeed;
    }

    @Override
    public boolean checkShouldContinueShootingAfterShot() {
        ++this.nShots;
        if (this.nShots >= this.shotsPerAttack()) {
            this.nShots = 0;
            return true;
        }

        return false;
    }

    @Override
    public void shotsPerAttack(final int value) {
        this.shotsPerAttack = value;
    }
}
