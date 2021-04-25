package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class PlayerRenderer implements EntityRenderer<Player> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> run;
    private final Animation<TextureRegion> shoot;
    private final Animation<TextureRegion> death;
    private final Animation<TextureRegion> falling;

    private float currentTime;

    public PlayerRenderer() {
        this.texture = new Texture("miner.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0};
        final var runFrames = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        final var attackFrames = new int[]{9, 10, 11};
        final var deathFrames = new int[]{12, 12, 12, 12, 13, 14, 15, 15, 15, 15, 16};
        this.run = Animations.animationFromFrames(frames, runFrames);
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.shoot = Animations.animationFromFrames(frames, attackFrames);
        this.death = Animations.animationFromFrames(frames, deathFrames);
        this.falling = Animations.animationFromFrames(frames, runFrames);
    }

    @Override
    public <I extends Iterable<Player>> void render(
            final I entities,
            final RenderContext context
    ) {
        this.currentTime += Gdx.graphics.getDeltaTime();

        entities.forEach(player -> {
            final var velocity = player.body().getLinearVelocity();

            final var animation = player.dead()
                    ? this.death
                    : player.attacking()
                    ? this.shoot
                    : !player.grounded()
                    ? this.falling
                    : Math.abs(velocity.x) > 0.0f
                    ? this.run
                    : this.idle;

            final var timers = context.gameState().getTimers();
            final float stepLength = 16.0f * this.run.getKeyFrames().length;
            final var animationProgress = player.dead()
                    ? deathProgress(player, timers)
                    : player.attacking()
                    ? timers.getTimeElapsed(player.attackTimer()) / player.attackDuration()
                    : player.grounded() && Math.abs(velocity.x) > 0.0f
                    ? player.distanceTravelled() / stepLength
                    : this.currentTime * (player.grounded() ? 1.0f : 0.25f);

            final var currentFrame = animation.getKeyFrame(animationProgress, true);

            final var position = player.body().getPosition();

            final float x = position.x - player.width() / 2.0f;
            final float y = position.y - player.height() / 2.0f;
            final float originX = player.width() / 2.0f;
            final float originY = 0.0f;
            final float scaleX = player.facingRight ? 1.0f : -1.0f;

            final var fadeStart = 10.0f;
            final var fadeEnd = 50.0f;

            final var distance = Math.abs(position.y) - fadeStart;
            final var rgb = distance < 0
                    ? 1.0f
                    : (1.0f - ((distance - fadeStart) / (fadeEnd - fadeStart)));
            context.batch().setColor(rgb, rgb, rgb, 1.0f);
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

    private float deathProgress(final Player player, final fi.jakojaannos.syvyys.Timers timers) {
        return !player.deathSequenceHasFinished() && timers.isActiveAndValid(player.deathTimer())
                ? timers.getTimeElapsed(player.deathTimer()) / player.deathAnimationDuration()
                : 0.999f;
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
