package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.physics.box2d.Body;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.systems.CharacterTickSystem;

public class GameCharacter implements CharacterTickSystem.InputEntity, HasHealth {
    private final float attackDuration = 0.6f;
    private final int shotsPerAttack = 3;
    private final float jumpForce = 10.0f;
    private final float width;
    private final float height;
    private final Body body;
    public float distanceTravelled;
    public float previousDistanceTravelled;
    public boolean grounded;
    public boolean facingRight;
    public boolean attacking;
    public TimerHandle attackTimer;
    public TimerHandle shotTimer;
    private CharacterInput input;
    private float health;

    public GameCharacter(
            final Body body
    ) {
        this.width = 1.0f;
        this.height = 1.0f;
        this.body = body;
        this.distanceTravelled = 0.0f;
        this.grounded = true;
        this.input = new CharacterInput(0.0f, false, false);
        this.facingRight = true;
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
    public float shotsPerAttack() {
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
    public void dealDamage(float amount) {
        this.health -= amount;
    }

    @Override
    public boolean hasMoved() {
        return this.distanceTravelled - this.previousDistanceTravelled > 0.05f;
    }
}
