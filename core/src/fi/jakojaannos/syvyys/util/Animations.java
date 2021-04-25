package fi.jakojaannos.syvyys.util;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import fi.jakojaannos.syvyys.entities.HasHealth;

import java.util.Arrays;

public final class Animations {
    private Animations() {}

    public static Array<TextureRegion> framesForAnimation(final TextureRegion[] frames, final int[] frameIndices) {
        return Array.with(Arrays.stream(frameIndices)
                                .sequential()
                                .mapToObj(index -> frames[index])
                                .toArray(TextureRegion[]::new));
    }

    public static Animation<TextureRegion> animationFromFrames(final TextureRegion[] frames, final int[] frameIndices) {
        return animationFromFrames(frames, frameIndices, Animation.PlayMode.LOOP);
    }

    public static Animation<TextureRegion> animationFromFrames(
            final TextureRegion[] frames,
            final int[] frameIndices,
            final Animation.PlayMode playMode
    ) {
        return new Animation<>(
                1.0f / frameIndices.length,
                Animations.framesForAnimation(frames, frameIndices),
                playMode
        );
    }

    public static float deathProgress(final HasHealth player, final fi.jakojaannos.syvyys.Timers timers) {
        return !player.deathSequenceHasFinished() && timers.isActiveAndValid(player.deathTimer())
                ? timers.getTimeElapsed(player.deathTimer()) / player.deathAnimationDuration()
                : 0.999f;
    }
}
