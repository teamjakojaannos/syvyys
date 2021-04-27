package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.Boss;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class BossRenderer implements EntityRenderer<Boss> {
    private final Texture texture;
    private final Animation<TextureRegion> intro;

    private final Animation<TextureRegion> head;
    private final Animation<TextureRegion> handL;
    private final Animation<TextureRegion> handR;

    private final Animation<TextureRegion> shooty;

    private final Sound smash;

    public BossRenderer() {
        this.texture = new Texture("satan.png");
        this.smash = Gdx.audio.newSound(Gdx.files.internal("helvettiin_siita.wav"));

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 64, 64))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);

        this.handL = Animations.animationFromFrames(frames, new int[]{8});
        this.handR = Animations.animationFromFrames(frames, new int[]{9});
        this.head = Animations.animationFromFrames(frames, new int[]{10});

        this.intro = Animations.animationFromFrames(frames, new int[]{0, 1, 2, 3, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5});
        this.shooty = Animations.animationFromFrames(frames, new int[]{6, 7});
    }

    @Override
    public <I extends Iterable<Boss>> void render(
            final I bosses,
            final RenderContext context
    ) {
        bosses.forEach(boss -> {
            final var timers = context.gameState().getTimers();

            final var headPos = boss.handL.getPosition();
            if (boss.introProgress(context.gameState()) < 1.0f) {
                context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
                context.batch()
                       .draw(this.intro.getKeyFrame(boss.introProgress(context.gameState()), true),
                             headPos.x - 3.5f, headPos.y - 1.0f,
                             2.0f, 2.0f,
                             4.0f, 4.0f,
                             1.0f, 1.0f,
                             0.0f
                       );
                return;
            }

            final var handLPos = boss.handL.getPosition();
            final var handRPos = boss.handR.getPosition();

            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(this.head.getKeyFrame(0.0f),
                         headPos.x - 3.5f, headPos.y - 1.0f,
                         2.0f, 2.0f,
                         4.0f, 4.0f,
                         1.0f, 1.0f,
                         0.0f
                   );

            context.batch()
                   .draw(this.handL.getKeyFrame(0.0f),
                         handLPos.x - 2.0f, handLPos.y - 2.0f,
                         2.0f, 2.0f,
                         4.0f, 4.0f,
                         1.0f, 1.0f,
                         (float) (Math.sin(context.gameState().getCurrentTime()) * 45.0f)
                   );

            context.batch()
                   .draw(this.handR.getKeyFrame(0.0f),
                         handRPos.x - 2.0f, handRPos.y - 2.0f,
                         2.0f, 2.0f,
                         4.0f, 4.0f,
                         1.0f, 1.0f,
                         (float) (Math.sin(context.gameState().getCurrentTime() + 10.0f) * 45.0f)
                   );
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
        this.smash.dispose();
    }
}
