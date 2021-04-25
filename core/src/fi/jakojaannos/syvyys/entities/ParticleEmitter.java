package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParticleEmitter implements Entity {
    private final List<Particle> particles = new ArrayList<>();
    private final Vector2 gravity = new Vector2();

    private float burstTimestamp;
    private float lifetime;

    /**
     * FIXME: angleNoise IS NOT IN DEGREES/RADIANS (it is actually max deviation from direction in units after travelling distance of 1.0 units)
     */
    public void spawnBurst(
            final float currentTime,
            final int count,
            final Vector2 position,
            final float spawnRadius,
            final Vector2 direction,
            final float angleNoise,
            final float velocityMin,
            final float velocityMax,
            final float lifetimeMin,
            final float lifetimeMax,
            final float startAlphaMin,
            final float startAlphaMax,
            final float endAlphaMin,
            final float endAlphaMax,
            final Color colorRangeMin,
            final Color colorRangeMax,
            final Vector2 gravity
    ) {
        this.burstTimestamp = currentTime;
        this.gravity.set(gravity);

        final var missing = Math.max(0, count - this.particles.size());
        if (missing > 0) {
            for (int i = 0; i < missing; i++) {
                this.particles.add(new Particle());
            }
        }

        final var dirNormalOrig = new Vector2(direction).rotate90(-1);
        final var tmpDirNormal = new Vector2();

        float maxLifetime = lifetimeMin;
        for (int i = 0; i < this.particles.size(); i++) {
            final var particle = this.particles.get(i);
            if (i < count) {
                tmpDirNormal.set(dirNormalOrig);

                particle.startPosition.setToRandomDirection()
                                      .scl(MathUtils.random(spawnRadius))
                                      .add(position);
                particle.velocity.set(direction)
                                 .scl(MathUtils.random(velocityMin, velocityMax))
                                 .add(tmpDirNormal.scl(MathUtils.random(-angleNoise, angleNoise)));
                particle.lifetime = MathUtils.random(lifetimeMin, lifetimeMax);
                particle.frameIndex = MathUtils.random(Integer.MAX_VALUE - 1);
                particle.startAlpha = MathUtils.random(startAlphaMin, startAlphaMax);
                particle.endAlpha = MathUtils.random(endAlphaMin, endAlphaMax);
                particle.color = new Color(
                        MathUtils.random(colorRangeMin.r, colorRangeMax.r),
                        MathUtils.random(colorRangeMin.g, colorRangeMax.g),
                        MathUtils.random(colorRangeMin.b, colorRangeMax.b),
                        MathUtils.random(colorRangeMin.a, colorRangeMax.a)
                );

                maxLifetime = Math.max(particle.lifetime, maxLifetime);
            } else {
                particle.clear();
            }
        }

        this.lifetime = maxLifetime;
    }

    public Stream<Particle> getParticles() {
        return this.particles.stream();
    }

    public boolean hasExpired(final float currentTime) {
        return currentTime >= this.burstTimestamp + this.lifetime;
    }

    public final class Particle {
        private final Vector2 startPosition = new Vector2();
        private final Vector2 velocity = new Vector2();
        private Color color = new Color();
        private float lifetime;
        private float startAlpha;
        private float endAlpha;
        private int frameIndex;

        public boolean isAliveAt(final float currentTime) {
            return currentTime <= ParticleEmitter.this.burstTimestamp + this.lifetime;
        }

        public Vector2 startPosition() {
            return this.startPosition;
        }

        public Vector2 velocity() {
            return this.velocity;
        }

        public int frameIndex() {
            return this.frameIndex;
        }

        public float ageAt(final float currentTime) {
            return currentTime - ParticleEmitter.this.burstTimestamp;
        }

        public float alphaAt(final float currentTime) {
            return MathUtils.lerp(this.startAlpha, this.endAlpha, ageAt(currentTime) / this.lifetime);
        }

        public Color color() {
            return this.color;
        }

        public Vector2 gravity() {
            return ParticleEmitter.this.gravity;
        }

        public void clear() {
            this.startPosition.set(Vector2.Zero);
            this.velocity.set(Vector2.Zero);
            this.color.set(0);
            this.lifetime = 0.0f;
            this.startAlpha = 0.0f;
            this.endAlpha = 0.0f;
            this.frameIndex = 0;
        }
    }
}
