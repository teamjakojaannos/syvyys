package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Golem;

import java.util.stream.Stream;

public class GolemAiTickSystem implements EcsSystem<Golem> {

    @Override
    public void tick(final Stream<Golem> entities, final GameState gameState) {
        entities.forEach(golem -> {

        });
    }
}
