package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.entities.ParticleEmitter;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.StreamSupport;

public class ParticleEmitterRenderer implements EntityRenderer<ParticleEmitter> {
    private static final Random PARTICLE_ROT_RANDOM = new Random();

    private final Texture texture;
    private final TextureRegion[] frames;


    public ParticleEmitterRenderer() {
        this.texture = new Texture("particle_small.png");
        this.frames = Arrays.stream(TextureRegion.split(this.texture, 1, 1))
                            .flatMap(Arrays::stream)
                            .toArray(TextureRegion[]::new);
    }

    @Override
    public boolean rendersLayer(final RenderLayer layer) {
        return layer == RenderLayer.FOREGROUND;
    }

    @Override
    public <I extends Iterable<ParticleEmitter>> void render(
            final I emitters,
            final RenderContext context
    ) {
        final var currentTime = context.gameState().getCurrentTime();

        final Vector2 pos = new Vector2();
        final Vector2 translationFromGravity = new Vector2();
        StreamSupport.stream(emitters.spliterator(), false)
                     .flatMap(ParticleEmitter::getParticles)
                     .filter(particle -> particle.isAliveAt(currentTime))
                     .forEach(particle -> {
                         final var frame = this.frames[particle.frameIndex() % this.frames.length];
                         final var age = particle.ageAt(currentTime);

                         translationFromGravity.set(particle.gravity())
                                               .scl((age * age) * 0.5f);

                         pos.set(particle.startPosition())
                            .mulAdd(particle.velocity(), age)
                            .add(translationFromGravity);

                         final var width = frame.getRegionWidth() / 16.0f;
                         final var height = frame.getRegionHeight() / 16.0f;
                         final var x = pos.x - width / 2.0f;
                         final var y = pos.y - height / 2.0f;
                         final var originX = width / 2.0f;
                         final var originY = height / 2.0f;
                         final var alpha = particle.alphaAt(currentTime);
                         PARTICLE_ROT_RANDOM.setSeed(particle.frameIndex());
                         final var angle = PARTICLE_ROT_RANDOM.nextFloat() * 360.0f;

                         final var color = particle.color();

                         context.batch()
                                .setColor(color.r, color.g, color.b, color.a * alpha);
                         context.batch()
                                .draw(frame,
                                      x, y,
                                      originX, originY,
                                      width, height,
                                      1.0f, 1.0f,
                                      angle);
                     });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
