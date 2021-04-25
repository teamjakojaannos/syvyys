package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.SoulTrap;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;

public class SoulTrapRenderer implements EntityRenderer<SoulTrap> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> bubbling;
    private final Animation<TextureRegion> iWantOut;

    public SoulTrapRenderer() {
        this.texture = new Texture("souls_of_the_damned.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 32, 32))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0, 1};
        final var bubblingFrames = new int[]{0, 1, 2, 3};
        final var iWantOutFrames = new int[]{4, 5, 6,};

        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.bubbling = Animations.animationFromFrames(frames, bubblingFrames, Animation.PlayMode.LOOP_RANDOM);
        this.iWantOut = Animations.animationFromFrames(frames, iWantOutFrames, Animation.PlayMode.LOOP_RANDOM);
    }

    @Override
    public <I extends Iterable<SoulTrap>> void render(
            final I traps,
            final RenderContext context
    ) {
        traps.forEach(trap -> {
            final var animation = switch (trap.state) {
                case IDLE -> this.idle;
                case BUBBLING -> this.bubbling;
                case I_WANT_OUT -> this.iWantOut;
            };

            final var animSpeed = trap.state == SoulTrap.State.IDLE ? 1.0f : 5.0f;

            final var currentTime = context.gameState().getCurrentTime();
            final var frame = animation.getKeyFrame(currentTime * animSpeed, true);

            final var position = trap.body().getPosition();
            final float x = position.x - trap.width() / 2.0f;
            final float y = position.y - trap.height() / 2.0f;
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
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
