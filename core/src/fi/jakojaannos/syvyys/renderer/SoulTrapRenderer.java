package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import fi.jakojaannos.syvyys.entities.SoulTrap;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoulTrapRenderer implements EntityRenderer<SoulTrap> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> bubbling;
    private final Animation<TextureRegion> iWantOut;

    private final Sound goToHell;

    public SoulTrapRenderer() {
        this.goToHell = Gdx.audio.newSound(Gdx.files.internal("helvettiin_siita.wav"));
        this.texture = new Texture("souls_of_the_damned.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 32, 32))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0, 1};
        final var bubblingFrames = new int[]{0, 1, 2, 3};
        final var iWantOutFrames = new int[]{4, 5, 6};

        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.bubbling = Animations.animationFromFrames(frames, bubblingFrames, Animation.PlayMode.LOOP_RANDOM);
        this.iWantOut = Animations.animationFromFrames(frames, iWantOutFrames, Animation.PlayMode.LOOP_RANDOM);
    }

    @Override
    public <I extends Iterable<SoulTrap>> void render(
            final I traps,
            final RenderContext context
    ) {
        final var shouldStartScreaming = new AtomicBoolean(false);
        traps.forEach(trap -> {
            final var animation = switch (trap.state) {
                case IDLE -> this.idle;
                case BUBBLING -> this.bubbling;
                case I_WANT_OUT, I_WANT_OUT_START -> this.iWantOut;
            };

            if (trap.state == SoulTrap.State.I_WANT_OUT_START) {
                shouldStartScreaming.set(true);
                trap.state = SoulTrap.State.I_WANT_OUT;
            }

            final var animSpeed = trap.state == SoulTrap.State.IDLE ? 1.0f : 5.0f;

            final var currentTime = context.gameState().getCurrentTime();
            final var frame = animation.getKeyFrame(currentTime * animSpeed, true);

            final var position = trap.body().getPosition();
            final float x = position.x - trap.width() / 2.0f;
            final float y = position.y - trap.height() / 2.0f + 1.25f; // HACK: hitbox has odd size by design
            final float originX = trap.width() / 2.0f;
            final float originY = 0.0f;
            context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(frame,
                         x, y,
                         originX, originY,
                         trap.width(), trap.height(),
                         1.0f, 1.0f,
                         0.0f
                   );
        });

        if (shouldStartScreaming.get()) {
            this.goToHell.play(
                    0.125f,
                    MathUtils.random(0.5f, 2.0f),
                    MathUtils.random(-0.2f, 0.2f)
            );
        }
    }

    @Override
    public void close() {
        this.texture.dispose();
        this.goToHell.dispose();
    }
}
