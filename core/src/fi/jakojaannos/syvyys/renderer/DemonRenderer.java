package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import fi.jakojaannos.syvyys.entities.Demon;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class DemonRenderer implements EntityRenderer<Demon> {
    private final Texture texture;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> death;

    private final Sound[] spit;

    public DemonRenderer() {
        this.texture = new Texture("demon_01.png");
        this.spit = new Sound[]{
                Gdx.audio.newSound(Gdx.files.internal("demoni_sylkee_1-2.wav")),
                Gdx.audio.newSound(Gdx.files.internal("demoni_sylkee_2-2.wav")),
                //Gdx.audio.newSound(Gdx.files.internal("demoni_sylkee_3-2.wav")), // does not fit, re-used as hit marker sfx
        };

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0};
        final var attackFrames = new int[]{0, 1, 2, 3, 4, 5, 6};
        final var deathFrames = new int[]{0, 1, 2, 7, 8, 9, 10, 11};
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.attack = Animations.animationFromFrames(frames, attackFrames);
        this.death = Animations.animationFromFrames(frames, deathFrames);
    }

    @Override
    public <I extends Iterable<Demon>> void render(
            final I demons,
            final RenderContext context
    ) {
        demons.forEach(demon -> {
            if (demon.justAttacked) {
                final var pew = this.spit[MathUtils.random(this.spit.length - 1)];
                pew.play(0.125f, MathUtils.random(0.8f, 1.2f), 0.0f);
                demon.justAttacked = false;
            }

            final var animation = demon.dead()
                    ? this.death
                    : this.attack;

            final var timers = context.gameState().getTimers();
            final var animationProgress = demon.dead()
                    ? Animations.deathProgress(demon, timers)
                    : context.gameState().getCurrentTime();
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
        for (final var sound : this.spit) {
            sound.dispose();
        }
    }
}
