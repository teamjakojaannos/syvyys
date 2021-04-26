package fi.jakojaannos.syvyys.entities;

import fi.jakojaannos.syvyys.systems.HasCharacterState;

public interface HasEnemyAI extends HasBody, HasCharacterState, HasCharacterInput, Entity {
    float maxChaseDistance();

    float attackDistance();

    boolean canAttackFromDifferentY();

    default boolean shouldTickAi() {
        return true;
    }
}
