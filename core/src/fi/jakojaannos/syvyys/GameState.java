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
    private final List<Entity> entitiesToBeSpawned = new ArrayList<>();
    private final List<Entity> entitiesToBeRemoved = new ArrayList<>();

    private final Camera camera;
    private final List<ParticleEmitter> particleEmittersPool = new ArrayList<>();
    private Player player;
    private float currentTime;
    private GameStage nextStage;
    private boolean hardReset;
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

    public void changeStage(final GameStage nextStage, final boolean hardReset) {
        this.nextStage = nextStage;
        this.hardReset = hardReset;
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
        if (this.particleEmittersPool.isEmpty()) {
            final var emitter = new ParticleEmitter();
            this.entitiesToBeSpawned.add(emitter);

            return emitter;
        }

        return this.particleEmittersPool.remove(this.particleEmittersPool.size() - 1);
    }

    public void returnToPool(final ParticleEmitter emitter) {
        this.particleEmittersPool.add(emitter);
        this.entitiesToBeRemoved.remove(emitter);
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

    public void deletThis(final Entity entity) {
        this.entitiesToBeRemoved.add(entity);
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(this.player);
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public GameStage getCurrentStage() {
        return this.gameStage;
    }

    public void spawnEntities() {
        this.entities.addAll(this.entitiesToBeSpawned);
        this.entitiesToBeSpawned.clear();
    }

    public void purgeEntities() {
        this.entitiesToBeRemoved.forEach(entity -> {
            if (entity instanceof HasBody bodyOwner) {
                this.physicsWorld.destroyBody(bodyOwner.body());
            }
        });

        this.entities.removeAll(this.entitiesToBeRemoved);
        this.entitiesToBeRemoved.clear();
    }

    public void spawn(final Entity entity) {
        this.entitiesToBeSpawned.add(entity);
    }

    public boolean isHardReset() {
        return this.hardReset;
    }
}
