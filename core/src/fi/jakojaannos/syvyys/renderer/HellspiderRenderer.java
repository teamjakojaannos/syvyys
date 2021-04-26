package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import fi.jakojaannos.syvyys.entities.Hellspider;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class HellspiderRenderer implements EntityRenderer<Hellspider> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> run;
    private final Animation<TextureRegion> leap;
    private final Animation<TextureRegion> dash;
    private final Animation<TextureRegion> death;

    private final Sound[] jump;

    public HellspiderRenderer() {
        this.texture = new Texture("hellspider.png");
        this.jump = new Sound[]{
                Gdx.audio.newSound(Gdx.files.internal("demoni_sylkee_3-2.wav")),
        };

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0, 0, 0, 3};
        final var runFrames = new int[]{0, 1, 2};
        final var leapFrames = new int[]{
                // HACK: make the last frame very short
                3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                5
        };
        final var dashFrames = new int[]{6};
        final var deathFrames = new int[]{3, 4, 6, 7, 8, 9};
        this.idle = Animations.animationFromFrames(frames, idleFrames, Animation.PlayMode.LOOP_RANDOM);
        this.run = Animations.animationFromFrames(frames, runFrames);
        this.leap = Animations.animationFromFrames(frames, leapFrames);
        this.dash = Animations.animationFromFrames(frames, dashFrames);
        this.death = Animations.animationFromFrames(frames, deathFrames);
    }

    @Override
    public <I extends Iterable<Hellspider>> void render(
            final I spiders,
            final RenderContext context
    ) {
        spiders.forEach(spoder -> {
            if (spoder.justAttacked) {
                final var pew = this.jump[MathUtils.random(this.jump.length - 1)];
                pew.play(0.5f, MathUtils.random(0.8f, 1.2f), 0.0f);
                spoder.justAttacked = false;
            }

            final var timers = context.gameState().getTimers();
            final var animation = spoder.dead()
                    ? this.death
                    : timers.isActiveAndValid(spoder.attackDelayTimer()) || spoder.state == Hellspider.State.LEAPING
                    ? this.leap
                    : spoder.state == Hellspider.State.DASHING
                    ? this.dash
                    : spoder.hasMoved()
                    ? this.run
                    : this.idle;

            final var currentTime = context.gameState().getCurrentTime();
            final float stepLength = 8.0f * this.run.getKeyFrames().length;

            final var animationProgress = spoder.dead()
                    ? Animations.deathProgress(spoder, timers)
                    : timers.isActiveAndValid(spoder.attackDelayTimer()) || spoder.state == Hellspider.State.LEAPING
                    ? (timers.isActiveAndValid(spoder.attackDelayTimer()) ? timers.getTimeElapsed(spoder.attackDelayTimer()) : 0.99f)
                    : spoder.state == Hellspider.State.DASHING
                    ? currentTime
                    : spoder.hasMoved()
                    ? spoder.distanceTravelled() / stepLength
                    : currentTime;

            final var currentFrame = animation.getKeyFrame(animationProgress, true);
            final var position = spoder.body().getPosition();

            final float x = position.x - spoder.width() / 2.0f;
            final float y = position.y - spoder.height() / 2.0f;
            final float originX = spoder.width() / 2.0f;
            final float originY = 0.0f;
            final float scaleX = 1.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(currentFrame,
                         x, y,
                         originX, originY,
                         spoder.width(), spoder.height(),
                         scaleX, 1.0f,
                         0.0f
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
        for (final var sound : this.jump) {
            sound.dispose();
        }
    }
}
