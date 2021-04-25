package fi.jakojaannos.syvyys;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.renderer.Camera;
import fi.jakojaannos.syvyys.stages.GameStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GameState {
    private final GameStage gameStage;
    private final Timers timers = new Timers();
    private final World physicsWorld;

    private final List<Entity> entities;
    private final Player player;
    private final Camera camera;

    private float currentTime;

    private GameStage nextStage;
    private Color backgroundColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);

    public GameState(
            final GameStage gameStage, final World physicsWorld,
            final Collection<Entity> entities,
            final Player player,
            final Camera camera
    ) {
        this.gameStage = gameStage;
        this.physicsWorld = physicsWorld;
        this.entities = new ArrayList<>(entities);
        this.player = player;
        this.camera = camera;
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

    public <T extends Entity> Stream<T> getEntities(final Class<? extends T> clazz) {
        return getEntities(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> Stream<T> getEntities(final Class<? extends T> clazz, final boolean includeDead) {
        return this.entities
                .stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .filter(e -> includeDead || !(e instanceof HasHealth health) || !health.dead())
                .map(e -> (T) e);
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(final Color color) {
        this.backgroundColor = color;
    }

    public boolean deletThis(final Entity entity) {
        if (entity instanceof HasBody bodyOwner) {
            this.physicsWorld.destroyBody(bodyOwner.body());
        }

        return this.entities.remove(entity);
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(this.player);
    }

    public Camera getCamera() {
        return camera;
    }

    public GameStage getCurrentStage() {
        return this.gameStage;
    }
}
