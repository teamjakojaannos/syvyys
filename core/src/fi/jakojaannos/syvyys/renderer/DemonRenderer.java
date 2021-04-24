package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.Demon;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class DemonRenderer implements EntityRenderer<Demon> {
    private final Texture texture;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> idle;

    private float currentTime;

    public DemonRenderer() {
        this.texture = new Texture("demon_01.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0};
        final var attackFrames = new int[]{0, 1, 2, 3, 4, 5, 6};
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.attack = Animations.animationFromFrames(frames, attackFrames);
    }

    @Override
    public <I extends Iterable<Demon>> void render(
            final I demons,
            final RenderContext context
    ) {
        demons.forEach(demon -> {
            final var animation = this.attack;

            final var animationProgress = context.gameState().getCurrentTime();
            final var currentFrame = animation.getKeyFrame(animationProgress, true);

            final var position = demon.body().getPosition();

            final float x = position.x - demon.width() / 2.0f;
            final float y = position.y - demon.height() / 2.0f;
            final float originX = demon.width() / 2.0f;
            final float originY = 0.0f;
            final float scaleX = 1.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(currentFrame,
                         x, y,
                         originX, originY,
                         demon.width(), demon.height(),
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
