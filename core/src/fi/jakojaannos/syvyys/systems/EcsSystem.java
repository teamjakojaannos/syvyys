package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Entity;

import java.util.stream.Stream;

public interface EcsSystem<TEntity extends Entity> {
    void tick(Stream<TEntity> entities, GameState gameState);
}
