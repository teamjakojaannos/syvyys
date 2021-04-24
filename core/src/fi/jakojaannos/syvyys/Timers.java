package fi.jakojaannos.syvyys;

import com.badlogic.gdx.utils.LongMap;

import java.util.ArrayList;
import java.util.List;

public class Timers {
    private final List<TimerHandle> timers = new ArrayList<>();
    private final LongMap<TimerStatus> states = new LongMap<>();

    private long idCounter = 0;

    public TimerHandle set(final float duration, final boolean looping, final Action action) {
        return new TimerHandle(++this.idCounter, duration, action, looping);
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
        expired.forEach(timer -> this.states.remove(timer.id()));
        this.timers.removeAll(expired);
    }

    public interface Action {
        void execute();
    }

    private static class TimerStatus {
        float progress;
        boolean paused;
    }
}
