package fi.jakojaannos.syvyys.renderer.intro;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.intro.IntroDemonicSpawn;
import fi.jakojaannos.syvyys.renderer.EntityRenderer;
import fi.jakojaannos.syvyys.renderer.RenderContext;
import fi.jakojaannos.syvyys.util.Animations;

import java.util.Arrays;


public class IntroDemonicSpawnRenderer implements EntityRenderer<IntroDemonicSpawn> {
    private final Texture texture;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> hatching;
    private final Animation<TextureRegion> splurt;
    private final Animation<TextureRegion> split;
    private final Animation<TextureRegion> hatsOff;
    private final Animation<TextureRegion> trembling;
    private final Animation<TextureRegion> hatched;

    public IntroDemonicSpawnRenderer() {
        this.texture = new Texture("demonic_miner.png");

        final var frames = Arrays.stream(TextureRegion.split(this.texture, 16, 16))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var idleFrames = new int[]{0, 1};
        final var tremblingFrames = new int[]{2, 3, 4};
        final var hatsOffFrames = new int[]{5, 6, 7, 8, 8, 9, 9, 9, 9};
        final var splitFrames = new int[]{10, 11, 12, 13, 14, 15, 16, 16, 16, 16, 16, 17, 18, 19, 19, 19, 19, 19, 19, 19};
        final var splurtFrames = new int[]{20, 21, 22, 23, 24, 25, 25, 25, 25, 25, 25};
        final var hatchingFrames = new int[]{26, 26, 26, 26, 27, 27, 27, 28, 29, 30, 31, 32};
        final var hatchedFrames = new int[]{32};
        this.idle = Animations.animationFromFrames(frames, idleFrames);
        this.trembling = Animations.animationFromFrames(frames, tremblingFrames, Animation.PlayMode.LOOP_RANDOM);
        this.hatsOff = Animations.animationFromFrames(frames, hatsOffFrames, Animation.PlayMode.NORMAL);
        this.split = Animations.animationFromFrames(frames, splitFrames);
        this.splurt = Animations.animationFromFrames(frames, splurtFrames);
        this.hatching = Animations.animationFromFrames(frames, hatchingFrames);

        this.hatched = Animations.animationFromFrames(frames, hatchedFrames);
    }

    @Override
    public <I extends Iterable<IntroDemonicSpawn>> void render(
            final I entities,
            final RenderContext context
    ) {
        entities.forEach(entity -> {
            final var animation = switch (entity.stage) {
                case TIMPPA_HUOLLOSTA -> this.idle;
                case TREMBLING -> this.trembling;
                case HATS_OFF -> this.hatsOff;
                case SPLIT -> this.split;
                case SPLURT -> this.splurt;
                case HATCHING -> this.hatching;
                case IDLE_HATCHED -> this.hatched;
            };

            final var loops = switch (entity.stage) {
                case TIMPPA_HUOLLOSTA -> 1.1f;
                case TREMBLING -> entity.stageTimer.duration() * 2;
                case HATS_OFF, SPLIT, SPLURT, HATCHING, IDLE_HATCHED -> 1;
            };

            final var timers = context.gameState().getTimers();
            final var stageProgress = entity.stage == IntroDemonicSpawn.Stage.IDLE_HATCHED
                    ? 0.0f
                    : entity.stage == IntroDemonicSpawn.Stage.TIMPPA_HUOLLOSTA ?
                    context.gameState().getCurrentTime()
                    : timers.isActiveAndValid(entity.stageTimer) ? timers.getTimeElapsed(entity.stageTimer) / entity.stageTimer.duration() : 0.0f;

            final var frame = animation.getKeyFrame(stageProgress * loops, true);

            final var pos = entity.position;
            context.batch()
                   .draw(frame, pos.x, pos.y, 1.0f, 1.0f);
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
