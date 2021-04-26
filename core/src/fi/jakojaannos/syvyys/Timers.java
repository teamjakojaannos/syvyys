package fi.jakojaannos.syvyys;

import com.badlogic.gdx.utils.LongMap;

import java.util.ArrayList;
import java.util.List;

public class Timers {
    private final List<TimerHandle> timers = new ArrayList<>();
    private final LongMap<TimerState> states = new LongMap<>();

    private final List<TimerHandle> timersPendingRemoval = new ArrayList<>();

    private long idCounter = 0;

    public TimerHandle set(final float duration, final boolean looping, final Action action) {
        return set(duration, looping, action, false);
    }

    public TimerHandle set(
            final float duration,
            final boolean looping,
            final Action action,
            final boolean tickFirstImmediately
    ) {
        final var timer = new TimerHandle(++this.idCounter, duration, action, looping);
        this.timers.add(timer);
        final var state = new TimerState();
        state.forceTick = tickFirstImmediately;

        this.states.put(timer.id(), state);

        return timer;
    }

    public void tick(final float delta) {
        // Copy to avoid ConcurrentModificationException
        final var oldTimers = List.copyOf(this.timers);
        oldTimers.forEach(timer -> {
            final var state = this.states.get(timer.id());
            if (state == null) {
                System.err.println("Encountered invalidated/stateless timer!");
                this.timers.remove(timer);
                return;
            }

            if (!state.paused) {
                state.progress += delta;

                while (state.progress >= timer.duration() || state.forceTick) {
                    if (!state.forceTick) {
                        state.progress -= timer.duration();
                    }
                    state.forceTick = false;

                    timer.action().execute();

                    if (!timer.looping()) {
                        this.states.remove(timer.id());
                        this.timers.remove(timer);
                        break;
                    }
                }
            }
        });

        // Clean up any pending removals
        this.timersPendingRemoval.forEach(timer -> this.states.remove(timer.id()));
        this.timers.removeAll(this.timersPendingRemoval);
        this.timersPendingRemoval.clear();
    }

    public void clear(final TimerHandle timer) {
        if (timer == null || !this.states.containsKey(timer.id())) {
            return;
        }

        this.timersPendingRemoval.add(timer);
    }

    public float getTimeElapsed(final TimerHandle timer) {
        if (!this.states.containsKey(timer.id())) {
            throw new IllegalStateException("Tried getting elapsed time of an invalid timer handle!");
        }

        return this.states.get(timer.id()).progress;
    }

    public boolean isActiveAndValid(final TimerHandle timer) {
        return timer != null && this.states.containsKey(timer.id()) && !this.states.get(timer.id()).paused;
    }

    public interface Action {
        void execute();
    }

    private static class TimerState {
        boolean forceTick = false;
        float progress = 0.0f;
        boolean paused = false;
    }
}
