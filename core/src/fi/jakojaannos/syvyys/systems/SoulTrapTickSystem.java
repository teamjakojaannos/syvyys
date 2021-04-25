package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.SoulTrap;

import java.util.stream.Stream;

public class SoulTrapTickSystem implements EcsSystem<SoulTrap> {
    @Override
    public void tick(final Stream<SoulTrap> soulTraps, final GameState gameState) {
        final var timers = gameState.getTimers();
        soulTraps.forEach(trap -> {
            final boolean isDealingDamage = timers.isActiveAndValid(trap.damageTimer);
            final var canDealDamage = trap.isInContactWithPlayer && trap.state == SoulTrap.State.I_WANT_OUT;
            if (canDealDamage && !isDealingDamage) {
                trap.damageTimer = timers.set(trap.damageTickInterval, true, () -> {
                    gameState.getPlayer()
                             .ifPresent(player -> player.dealDamage(5.0f));
                });
            }

            if (!canDealDamage) {
                timers.clear(trap.damageTimer);
            }

            final var isTicking = timers.isActiveAndValid(trap.stateTimer);
            if (!isTicking && trap.isBubbling()) {
                trap.stateTimer = timers.set(trap.bubblingDuration, false, () -> {
                    trap.state = SoulTrap.State.I_WANT_OUT_START;

                    trap.stateTimer = timers.set(trap.attackDuration, false, () -> {
                        trap.state = SoulTrap.State.IDLE;
                    });
                });
            }
        });
    }
}
