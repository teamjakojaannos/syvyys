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
        final var timer = new TimerHandle(++this.idCounter, duration, action, looping);
        this.timers.add(timer);
        this.states.put(timer.id(), new TimerState());

        return timer;
    }

    public void tick(final float delta) {
        final var expired = new ArrayList<TimerHandle>();
        this.timers.forEach(timer -> {
            final var state = this.states.get(timer.id());
            if (state == null) {
                expired.add(timer);
                return;
            }

            if (!state.paused) {
                state.progress += delta;

                while (state.progress >= timer.duration()) {
                    state.progress -= timer.duration();

                    timer.action().execute();

                    if (!timer.looping()) {
                        expired.add(timer);
                        break;
                    }
                }
            }
        });

        // Clean up
        expired.addAll(this.timersPendingRemoval);
        expired.forEach(timer -> this.states.remove(timer.id()));
        this.timers.removeAll(expired);
        this.timersPendingRemoval.clear();
    }

    public void clear(final TimerHandle timer) {
        if (!this.states.containsKey(timer.id())) {
            throw new IllegalStateException("Tried clearing invalid timer handle!");
        }

        this.timersPendingRemoval.add(timer);
    }

    public interface Action {
        void execute();
    }

    private static class TimerState {
        float progress = 0.0f;
        boolean paused = false;
    }
}
