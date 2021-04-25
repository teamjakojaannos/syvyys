package fi.jakojaannos.syvyys.entities;

import fi.jakojaannos.syvyys.TimerHandle;

public interface HasHealth extends Entity {
    void dealDamage(float amount);

    float maxHealth();

    float health();

    boolean dead();

    TimerHandle deathTimer();

    void deathTimer(TimerHandle timer);

    float deathAnimationDuration();

    default void onDeadCallback() {}

    boolean deathSequenceHasFinished();

    void deathSequenceHasFinished(boolean value);
}
