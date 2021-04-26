package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.SpikeNode;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class SpikeRenderer implements EntityRenderer<SpikeNode> {
    private final Texture texture;

    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> rumbling;
    private final Animation<TextureRegion> spawn;
    private final Animation<TextureRegion> breaking;

    public SpikeRenderer() {
        this.texture = new Texture("spike.png");
        final var frames = Arrays.stream(TextureRegion.split(this.texture, 8, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);

        final var rumblingFrames = new int[]{0, 1, 2};
        final var spawnFrames = new int[]{2, 3, 4, 5, 6};
        final var idleFrames = new int[]{6};
        final var breakingFrames = new int[]{7, 8, 9, 10, 11};
        this.rumbling = Animations.animationFromFrames(frames, rumblingFrames);
        this.spawn = Animations.animationFromFrames(frames, spawnFrames);
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.breaking = Animations.animationFromFrames(frames, breakingFrames);
    }

    @Override
    public boolean rendersLayer(final RenderLayer layer) {
        return layer == RenderLayer.FOREGROUND;
    }

    @Override
    public <I extends Iterable<SpikeNode>> void render(
            final I spikes,
            final RenderContext context
    ) {
        spikes.forEach(yikes -> {
            final var animation = switch (yikes.state) {
                case INITIAL, HIDDEN -> null;
                case RUMBLING -> this.rumbling;
                case IDLE -> this.idle;
                case UP_IN_THE_ASS_OF_TIMO -> this.spawn;
                case DED -> this.breaking;
            };

            final var timers = context.gameState().getTimers();
            if (animation == null || yikes.stageTimer == null || !timers.isActiveAndValid(yikes.stageTimer)) {
                return;
            }

            final var progress = timers.getTimeElapsed(yikes.stageTimer) / yikes.stageTimer.duration();

            final var loops = yikes.state == SpikeNode.State.RUMBLING ? 4 * yikes.stageTimer.duration() : 1;
            final var frame = animation.getKeyFrame(progress * loops);

            final var position = yikes.body().getPosition();
            final float x = position.x - yikes.width() / 2.0f;
            final float y = position.y - yikes.height() / 2.0f;
            final float originX = yikes.width() / 2.0f;
            final float originY = 0.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            final var scaleX = yikes.facingRight ? -1.0f : 1.0f;
            context.batch()
                   .draw(frame,
                         x, y,
                         originX, originY,
                         yikes.width(), yikes.height(),
                         scaleX, 1.0f,
                         0.0f
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
