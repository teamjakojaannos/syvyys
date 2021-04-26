package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.TimerHandle;

public interface HasCharacterState {
    float distanceTravelled();

    void distanceTravelled(final float value);

    boolean attacking();

    void attacking(final boolean value);

    boolean grounded();

    void grounded(final boolean value);

    boolean facingRight();

    void facingRight(boolean value);

    float jumpForce();

    float attackDuration();

    float shotsPerAttack();

    TimerHandle shotTimer();

    void shotTimer(TimerHandle timer);

    TimerHandle attackTimer();

    void attackTimer(TimerHandle timer);

    boolean hasMoved();

    float attackDelay();

    TimerHandle attackDelayTimer();

    void attackDelayTimer(TimerHandle timer);

    float maxSpeed();

    boolean checkShouldContinueShootingAfterShot();
}
