package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.entities.intro.IntroDemonicSpawn;
import fi.jakojaannos.syvyys.renderer.intro.IntroDemonicSpawnRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
                Map.entry(Tile.class, new TileRenderer()),
                Map.entry(ParticleEmitter.class, new ParticleEmitterRenderer()),
                Map.entry(IntroDemonicSpawn.class, new IntroDemonicSpawnRenderer()),
                Map.entry(Demon.class, new DemonRenderer()),
                Map.entry(Hellspider.class, new HellspiderRenderer()),
                Map.entry(UI.class, new MessageBoxRenderer()),
                Map.entry(SoulTrap.class, new SoulTrapRenderer()),
                Map.entry(DemonBall.class, new DemonBallRenderer()),
                Map.entry(ShopItem.class, new ShopItemRenderer()),
                Map.entry(SpikeNode.class, new SpikeRenderer()),
                Map.entry(SpikeBarrier.class, new SpikeBarrierRenderer()),
                Map.entry(Golem.class, new GolemRenderer()),
                Map.entry(Boss.class, new BossRenderer())
        );

        this.physicsDebugRenderer = new Box2DDebugRenderer();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void render(final GameState gameState, final Stream<Entity> entities) {
        final var entitiesByClass =
                entities.filter(e -> isInRenderableRadius(gameState, e))
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

        for (final var layer : RenderLayer.backToFront()) {
            this.batch.setProjectionMatrix(this.camera.getCombinedMatrix());
            this.batch.begin();
            final var context = new RenderContext(this.batch, gameState, this.camera, layer);
            entitiesByClass.forEach((clazz, entitiesOfClazz) -> {
                // UI is hardcoded last
                if (clazz == UI.class) {
                    return;
                }

                final EntityRenderer renderer = this.renderers.get(clazz);
                if (renderer != null && renderer.rendersLayer(layer)) {
                    renderer.render(entitiesOfClazz, context);
                }
            });
            this.batch.end();
        }

        this.batch.setProjectionMatrix(this.camera.getCombinedMatrix());
        this.batch.begin();
        final EntityRenderer renderer = this.renderers.get(UI.class);
        if (renderer != null) {
            final var context = new RenderContext(this.batch, gameState, this.camera, null);
            renderer.render(entitiesByClass.get(UI.class), context);
        }
        this.batch.end();


        if (SyvyysGame.Constants.DEBUG_PHYSICS) {
            this.physicsDebugRenderer.render(gameState.getPhysicsWorld(), this.camera.getCombinedMatrix());
        }
    }

    private boolean isInRenderableRadius(final GameState gameState, final Entity e) {
        if (e instanceof HasBody bodyOwner) {
            final var hDistanceFromPlayer = Math.abs(gameState.getLastKnownPlayerLocation().x - bodyOwner.body().getPosition().x);
            return hDistanceFromPlayer < gameState.getCamera().getWidthInUnits2(gameState);
        }

        return true;


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
