package fi.jakojaannos.syvyys.renderer;

import fi.jakojaannos.syvyys.entities.Entity;

public interface EntityRenderer<T extends Entity> extends AutoCloseable {
    <I extends Iterable<T>> void render(final I entities, final RenderContext context);

    // Override to remove `... throws Exception`
    @Override
    void close();

    default boolean rendersLayer(final RenderLayer layer) {
        return layer == RenderLayer.MAIN;
    }
}
