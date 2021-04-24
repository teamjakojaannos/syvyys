package fi.jakojaannos.syvyys;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.Demon;
import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.ParticleEmitter;
import fi.jakojaannos.syvyys.stages.GameStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GameState {
    private final Timers timers = new Timers();
    private final World physicsWorld;

    private final List<Entity> entities;

    private float currentTime;

    private GameStage nextStage;
    private Color backgroundColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);

    public GameState(final World physicsWorld, final Collection<Entity> entities) {
        this.physicsWorld = physicsWorld;
        this.entities = new ArrayList<>(entities);
    }

    public void changeStage(final GameStage nextStage) {
        this.nextStage = nextStage;
    }

    public Optional<GameStage> getNextStage() {
        return Optional.ofNullable(this.nextStage);
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
        this.entities.add(emitter);

        return emitter;
    }

    public Stream<Entity> getAllEntities() {
        return this.entities.stream();
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(final Color color) {
        this.backgroundColor = color;
    }

    public boolean deletThis(final Entity entity) {
        // FIXME: interface HasBody
        if (entity instanceof Demon demon) {
            this.physicsWorld.destroyBody(demon.body());
        }

        return this.entities.remove(entity);
    }
}
