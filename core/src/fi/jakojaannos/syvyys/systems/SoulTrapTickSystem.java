package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.SoulTrap;

import java.util.stream.StreamSupport;

public class SoulTrapTickSystem implements EcsSystem<SoulTrap> {
    @Override
    public void tick(final Iterable<SoulTrap> soulTraps, final GameState gameState) {
        final var timers = gameState.getTimers();
        StreamSupport.stream(soulTraps.spliterator(), false)
                     .forEach(trap -> {
                         final var isTicking = timers.isActiveAndValid(trap.stateTimer);
                         if (!isTicking && trap.isBubbling()) {
                             trap.stateTimer = timers.set(trap.bubblingDuration, false, () -> {
                                 trap.state = SoulTrap.State.I_WANT_OUT;

                                 trap.stateTimer = timers.set(trap.attackDuration, false, () -> {
                                     trap.state = SoulTrap.State.IDLE;
                                 });

                                 trap.damageTimer = timers.set(trap.damageTickInterval, true, () -> {
                                     gameState.getPlayer()
                                              .ifPresent(player -> player.dealDamage(5.0f));
                                 });
                             });
                         }
                     });
    }
}
