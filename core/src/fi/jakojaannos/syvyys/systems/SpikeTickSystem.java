package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.Timers;
import fi.jakojaannos.syvyys.entities.SpikeNode;

import java.util.stream.Stream;

public class SpikeTickSystem implements EcsSystem<SpikeNode> {
    @Override
    public void tick(final Stream<SpikeNode> entities, final GameState gameState) {
        entities.forEach(spike -> {
            if (spike.state == SpikeNode.State.UP_IN_THE_ASS_OF_TIMO && spike.isInContactWithPlayer) {
                gameState.getPlayer().ifPresent(player -> {
                    player.dealDamage(spike.damage, gameState);
                    player.body().applyLinearImpulse(new Vector2(Vector2.Y).scl(10.0f * player.body().getMass()), player.body().getPosition(), true);
                });

                spike.isInContactWithPlayer = false;
            }

            if (spike.state == SpikeNode.State.INITIAL) {
                final var timers = gameState.getTimers();
                final var duration = spike.duration(SpikeNode.State.HIDDEN);

                spike.state = SpikeNode.State.HIDDEN;
                spike.stageTimer = timers.set(duration, false, () -> nextSpikeState(spike, timers, gameState));
            }
        });
    }

    private void nextSpikeState(final SpikeNode spike, final Timers timers, final GameState gameState) {
        spike.nextState()
             .ifPresentOrElse(state -> {
                                  spike.state = state;
                                  final float duration = spike.duration(state);
                                  spike.stageTimer = timers.set(duration, false, () -> nextSpikeState(spike, timers, gameState));
                              },
                              () -> gameState.deletThis(spike));
    }
}
