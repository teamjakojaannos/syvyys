package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import fi.jakojaannos.syvyys.Timers;
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
    private final Animation<TextureRegion> dash;
    private final Sound pew;
    private final Sound splat;

    private float currentTime;

    public PlayerRenderer() {
        this.texture = new Texture("miner.png");
        this.pew = Gdx.audio.newSound(Gdx.files.internal("Blast4.ogg"));
        this.splat = Gdx.audio.newSound(Gdx.files.internal("demoni_sylkee_3-2.wav"));

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0};
        final var runFrames = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        final var attackFrames = new int[]{9, 10, 11};
        final var deathFrames = new int[]{12, 12, 12, 12, 13, 14, 15, 15, 15, 15, 16};
        final var dashFrames = new int[]{17};
        this.run = Animations.animationFromFrames(frames, runFrames);
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.shoot = Animations.animationFromFrames(frames, attackFrames);
        this.death = Animations.animationFromFrames(frames, deathFrames);
        this.falling = Animations.animationFromFrames(frames, runFrames);
        this.dash = Animations.animationFromFrames(frames, dashFrames);
    }

    @Override
    public <I extends Iterable<Player>> void render(
            final I entities,
            final RenderContext context
    ) {
        this.currentTime += Gdx.graphics.getDeltaTime();

        entities.forEach(player -> {
            if (player.justAttacked) {
                this.pew.play(0.25f, 1.75f + MathUtils.random(0.0f, 0.25f), 0.0f);
                player.justAttacked = false;
            }

            if (player.justHitSomething) {
                this.splat.play(0.5f, 1.75f + MathUtils.random(0.0f, 0.25f), 0.0f);
                player.justHitSomething = false;
            }

            final var velocity = player.body().getLinearVelocity();

            final var animation = player.dead()
                    ? this.death
                    : (player.isDashing(context.gameState()) || player.body().getLinearVelocity().x > player.maxSpeed())
                    ? this.dash
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
                    ? Animations.deathProgress(player, timers)
                    : player.attacking()
                    ? attackProgress(player, timers)
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
            var rgb = distance < 0
                    ? 1.0f
                    : (1.0f - ((distance - fadeStart) / (fadeEnd - fadeStart)));

            if (player.isInvulnerable(context.gameState())) {
                rgb = 0.4f;
            }
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

    private float attackProgress(final Player player, final Timers timers) {
        return timers.isActiveAndValid(player.attackTimer())
                ? timers.getTimeElapsed(player.attackTimer()) / player.attackDuration()
                : 0.0f;
    }

    @Override
    public void close() {
        this.texture.dispose();
        this.pew.dispose();
        this.splat.dispose();
    }
}
