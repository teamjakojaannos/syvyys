package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.HasHealth;

import java.util.stream.Stream;

public class EntityReaperSystem implements EcsSystem<HasHealth> {
    @Override
    public void tick(final Stream<HasHealth> entities, final GameState gameState) {
        final var timers = gameState.getTimers();
        entities.forEach(entity -> {
            if (!entity.deathSequenceHasFinished() && entity.dead() && !timers.isActiveAndValid(entity.deathTimer())) {
                entity.deathTimer(timers.set(entity.deathAnimationDuration(), false, () -> {
                    entity.deathSequenceHasFinished(true);
                    entity.onDeadCallback();
                }));
            }
        });
    }
}
