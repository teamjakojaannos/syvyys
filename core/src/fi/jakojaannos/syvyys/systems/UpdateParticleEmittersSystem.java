package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.ParticleEmitter;

import java.util.stream.Stream;

public class UpdateParticleEmittersSystem implements EcsSystem<ParticleEmitter> {
    @Override
    public void tick(final Stream<ParticleEmitter> emitters, final GameState gameState) {
        final var currentTime = gameState.getCurrentTime();

        emitters.filter(emitter -> emitter.hasExpired(currentTime))
                .forEach(gameState::returnToPool);

    }
}
