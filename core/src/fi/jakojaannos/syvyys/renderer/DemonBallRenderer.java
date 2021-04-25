package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.DemonBall;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class DemonBallRenderer implements EntityRenderer<DemonBall> {
    private final Texture texture;
    private final Animation<TextureRegion> animation;

    public DemonBallRenderer() {
        this.texture = new Texture("ball.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 8, 8))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        this.animation = Animations.animationFromFrames(frames, new int[]{0, 1, 2});
    }

    @Override
    public <I extends Iterable<DemonBall>> void render(
            final I projectiles,
            final RenderContext context
    ) {
        projectiles.forEach(projectile -> {
            final var animSpeed = 5.0f;

            final var currentTime = context.gameState().getCurrentTime();
            final var frame = this.animation.getKeyFrame(currentTime * animSpeed, true);

            final var position = projectile.body().getPosition();
            final float x = position.x - projectile.width() / 2.0f;
            final float y = position.y - projectile.height() / 2.0f;
            final float originX = projectile.width() / 2.0f;
            final float originY = 0.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(frame,
                         x, y,
                         originX, originY,
                         projectile.width(), projectile.height(),
                         1.0f, 1.0f,
                         0.0f
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
