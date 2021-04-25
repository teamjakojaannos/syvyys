package fi.jakojaannos.syvyys.entities;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.TimerHandle;

public interface HasHealth extends Entity {
    void dealDamage(float amount);

    float maxHealth();

    float health();

    void health(float health);

    boolean dead();

    TimerHandle deathTimer();

    void deathTimer(TimerHandle timer);

    float deathAnimationDuration();

    boolean deathSequenceHasFinished();

    void deathSequenceHasFinished(boolean value);

    default void onDeadCallback(final GameState gameState) {}

    default void onStartDeathSequenceCallback(final GameState gameState) {}
}
