package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Entity;

public interface EcsSystem<TEntity extends Entity> {
    void tick(Iterable<TEntity> entities, GameState gameState);
}
