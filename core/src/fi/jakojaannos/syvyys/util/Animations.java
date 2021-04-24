package fi.jakojaannos.syvyys.util;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

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
        return new Animation<>(
                1.0f / frameIndices.length,
                Animations.framesForAnimation(frames, frameIndices),
                Animation.PlayMode.LOOP
        );
    }
}
