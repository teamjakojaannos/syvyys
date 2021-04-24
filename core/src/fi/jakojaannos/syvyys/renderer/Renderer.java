package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.ParticleEmitter;
import fi.jakojaannos.syvyys.entities.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class Renderer implements AutoCloseable {
    private final Camera camera;

    private final SpriteBatch batch;
    private final Map<Class<?>, ? extends EntityRenderer<?>> renderers;
    private final Box2DDebugRenderer physicsDebugRenderer;

    public Renderer() {
        this.camera = new Camera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.batch = new SpriteBatch();

        this.renderers = Map.ofEntries(
                Map.entry(Player.class, new PlayerRenderer()),
                Map.entry(ParticleEmitter.class, new ParticleEmitterRenderer())
        );

        this.physicsDebugRenderer = new Box2DDebugRenderer();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void render(final GameState gameState, final Iterable<? extends Entity> entities) {
        final var entitiesByClass =
                StreamSupport.stream(entities.spliterator(), false)
                             .reduce(new HashMap<Class<?>, List<Entity>>(), (results, entity) -> {
                                         final var clazz = entity.getClass();
                                         results.computeIfAbsent(clazz, key -> new ArrayList<>())
                                                .add(entity);
                                         return results;
                                     },
                                     (a, b) -> {
                                         b.forEach((clazz, entry) -> a.computeIfAbsent(clazz, key -> new ArrayList<>())
                                                                      .addAll(entry));
                                         return a;
                                     });

        this.camera.update();
        this.batch.setProjectionMatrix(this.camera.getProjectionMatrix());
        this.batch.setTransformMatrix(this.camera.getTransformMatrix());
        this.batch.begin();

        final var context = new RenderContext(this.batch, gameState);
        entitiesByClass.forEach((clazz, entitiesOfClazz) -> {
            final EntityRenderer renderer = this.renderers.get(clazz);
            if (renderer != null) {
                renderer.render(entitiesOfClazz, context);
            }
        });

        this.batch.end();

        this.physicsDebugRenderer.render(gameState.getPhysicsWorld(), this.camera.getCombinedMatrix());
    }

    @Override
    public void close() {
        this.renderers.values().forEach(EntityRenderer::close);
    }

    public void onScreenResized(final int windowWidth, final int windowHeight) {
        this.camera.resize(windowWidth, windowHeight);
    }

    public Camera getCamera() {
        return this.camera;
    }
}
