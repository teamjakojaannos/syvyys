package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.Golem;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class GolemRenderer implements EntityRenderer<Golem> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> death;
    private final Sound smash;

    public GolemRenderer() {
        this.texture = new Texture("golem.png");
        this.smash = Gdx.audio.newSound(Gdx.files.internal("helvettiin_siita.wav"));

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 32, 32))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);

        this.idle = Animations.animationFromFrames(frames, new int[]{0});
        this.attack = Animations.animationFromFrames(frames, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        this.walk = Animations.animationFromFrames(frames, new int[]{0, 13, 14, 15, 16, 17, 18, 19});
        this.death = Animations.animationFromFrames(frames, new int[]{20, 21, 22, 23, 24});
    }

    @Override
    public <I extends Iterable<Golem>> void render(
            final I golems,
            final RenderContext context
    ) {
        golems.forEach(golem -> {
            if (golem.justAttacked) {
                this.smash.play(0.5f, 0.5f, 0.0f);
                golem.justAttacked = false;
            }

            final var timers = context.gameState().getTimers();
            final var animation = golem.dead()
                    ? this.death
                    : timers.isActiveAndValid(golem.attackDelayTimer())
                    ? this.attack
                    : golem.hasMoved()
                    ? this.walk
                    : this.idle;

            final var currentTime = context.gameState().getCurrentTime();
            final float stepLength = 8.0f * this.walk.getKeyFrames().length;

            final var animationProgress = golem.dead()
                    ? Animations.deathProgress(golem, timers)
                    : timers.isActiveAndValid(golem.attackDelayTimer())
                    ? timers.getTimeElapsed(golem.attackDelayTimer()) / golem.attackDelayTimer().duration()
                    : golem.hasMoved()
                    ? golem.distanceTravelled() / stepLength
                    : currentTime;

            final var currentFrame = animation.getKeyFrame(animationProgress, true);
            final var position = golem.body().getPosition();

            final float x = position.x - golem.width() / 2.0f;
            final float y = position.y - 0.5f;
            final float originX = golem.width() / 2.0f;
            final float originY = 0.0f;
            final float scaleX = 1.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(currentFrame,
                         x, y,
                         originX, originY,
                         golem.width(), golem.height(),
                         scaleX, 1.0f,
                         0.0f
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
        this.smash.dispose();
    }
}
