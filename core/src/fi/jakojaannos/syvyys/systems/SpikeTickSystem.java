package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.Timers;
import fi.jakojaannos.syvyys.entities.SpikeNode;

import java.util.stream.Stream;

public class SpikeTickSystem implements EcsSystem<SpikeNode> {
    @Override
    public void tick(final Stream<SpikeNode> entities, final GameState gameState) {
        entities.forEach(spike -> {
            // TODO: deal damage + knockback on erupt, use quick (0.1s) idle?
            if (spike.state == SpikeNode.State.IDLE && spike.isInContactWithPlayer) {
                gameState.getPlayer().ifPresent(player -> player.dealDamage(spike.damage, gameState));
                spike.isInContactWithPlayer = false;
            }

            if (spike.state == SpikeNode.State.INITIAL) {
                final var timers = gameState.getTimers();
                final var duration = spike.duration(SpikeNode.State.HIDDEN);

                spike.state = SpikeNode.State.HIDDEN;
                spike.stageTimer = timers.set(duration, false, () -> nextSpikeState(spike, timers));
            }
        });
    }

    private void nextSpikeState(final SpikeNode spike, final Timers timers) {
        spike.nextState()
             .ifPresent(state -> {
                 spike.state = state;
                 final float duration = spike.duration(state);
                 spike.stageTimer = timers.set(duration, false, () -> nextSpikeState(spike, timers));
             });
    }
}
