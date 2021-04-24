package fi.jakojaannos.syvyys;

import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.ParticleEmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameState {
    private final Timers timers = new Timers();
    private final World physicsWorld;

    private final List<ParticleEmitter> particleEmitters = new ArrayList<>();

    private float currentTime;

    public GameState(final World physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    public Timers getTimers() {
        return this.timers;
    }

    public float getCurrentTime() {
        return this.currentTime;
    }

    public World getPhysicsWorld() {
        return this.physicsWorld;
    }

    public void updateTime(final float deltaSeconds) {
        this.currentTime += deltaSeconds;
    }

    public ParticleEmitter obtainParticleEmitter() {
        // FIXME: pool these
        final var emitter = new ParticleEmitter();
        this.particleEmitters.add(emitter);

        return emitter;
    }

    public Collection<ParticleEmitter> getAllParticleEmitters() {
        return this.particleEmitters;
    }
}
