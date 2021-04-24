package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import fi.jakojaannos.syvyys.entities.Player;

import java.util.Arrays;

public class PlayerRenderer implements EntityRenderer<Player> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> run;
    private final Animation<TextureRegion> shoot;

    private float currentTime;

    public PlayerRenderer() {
        this.texture = new Texture("miner.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0};
        final var runFrames = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        final var attackFrames = new int[]{9, 10, 11};
        this.run = new Animation<>(
                1.0f / runFrames.length,
                framesForAnimation(frames, runFrames),
                Animation.PlayMode.LOOP
        );

        this.idle = new Animation<>(
                1.0f / idleFrames.length,
                framesForAnimation(frames, idleFrames),
                Animation.PlayMode.LOOP
        );

        this.shoot = new Animation<>(
                1.0f / attackFrames.length,
                framesForAnimation(frames, attackFrames),
                Animation.PlayMode.LOOP
        );
    }

    private static Array<TextureRegion> framesForAnimation(final TextureRegion[] frames, final int[] frameIndices) {
        return Array.with(Arrays.stream(frameIndices)
                                .sequential()
                                .mapToObj(index -> frames[index])
                                .toArray(TextureRegion[]::new));
    }

    @Override
    public <I extends Iterable<Player>> void render(
            final I entities,
            final RenderContext context
    ) {
        this.currentTime += Gdx.graphics.getDeltaTime();

        entities.forEach(player -> {
            final var velocity = player.body().getLinearVelocity();

            final var animation = player.attacking
                    ? this.shoot
                    : Math.abs(velocity.x) > 0.0f
                    ? this.run
                    : this.idle;

            final float stepLength = 16.0f * this.run.getKeyFrames().length;
            final var animationProgress = player.attacking
                    ? (this.currentTime % player.attackDuration) / player.attackDuration
                    : Math.abs(velocity.x) > 0.0f
                    ? player.distanceTravelled / stepLength
                    : this.currentTime;
            final var currentFrame = animation.getKeyFrame(animationProgress, true);

            final var position = player.body().getPosition();

            final float x = position.x - player.width() / 2.0f;
            final float y = position.y - player.height() / 2.0f;
            final float originX = player.width() / 2.0f;
            final float originY = 0.0f;
            final float scaleX = player.facingRight ? 1.0f : -1.0f;
            context.batch()
                   .draw(currentFrame,
                         x, y,
                         originX, originY,
                         player.width(), player.height(),
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
