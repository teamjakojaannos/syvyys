package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.SpikeBarrier;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class SpikeBarrierRenderer implements EntityRenderer<SpikeBarrier> {
    private final Texture texture;
    private final Animation<TextureRegion> animation;

    public SpikeBarrierRenderer() {
        this.texture = new Texture("spike.png");
        final var frames = Arrays.stream(TextureRegion.split(this.texture, 8, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);

        this.animation = Animations.animationFromFrames(frames, new int[]{2, 3, 4, 5, 6});
    }

    @Override
    public boolean rendersLayer(final RenderLayer layer) {
        return layer == RenderLayer.FOREGROUND;
    }

    @Override
    public <I extends Iterable<SpikeBarrier>> void render(
            final I spikes,
            final RenderContext context
    ) {
        spikes.forEach(yikes -> {
            final var timers = context.gameState().getTimers();
            final var progress = timers.isActiveAndValid(yikes.closeProgress)
                    ? timers.getTimeElapsed(yikes.closeProgress) / yikes.closeProgress.duration()
                    : 0.999f;

            final var frame = this.animation.getKeyFrame(progress);

            final var position = yikes.body.getPosition();
            final float x = position.x;
            final float y = position.y;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(frame,
                         x - 0.25f, y - 1.0f,
                         0.0f, 0.0f,
                         0.5f, 1.0f,
                         1.0f, 1.0f,
                         0.0f
                   );
            context.batch()
                   .draw(frame,
                         x - 0.25f, y + 1.0f,
                         0.0f, 0.0f,
                         0.5f, 1.0f,
                         1.0f, -1.0f,
                         0.0f
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
