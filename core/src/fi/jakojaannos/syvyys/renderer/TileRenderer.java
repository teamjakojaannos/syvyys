package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.Arrays;

public class TileRenderer implements EntityRenderer<Tile> {
    private final Texture texture;
    private final TextureRegion[] frames;

    public TileRenderer() {
        this.texture = new Texture("tileset_hell.png");
        this.frames = Arrays.stream(TextureRegion.split(this.texture, 8, 8))
                            .flatMap(Arrays::stream)
                            .toArray(TextureRegion[]::new);
    }

    @Override
    public <I extends Iterable<Tile>> void render(
            final I tiles,
            final RenderContext context
    ) {
        tiles.forEach(tile -> {
            final var frameIndex = Math.abs(tile.tileIndex()) % this.frames.length;
            final var frame = this.frames[frameIndex];
            final var position = tile.body().getPosition();
            context.batch()
                   .setColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.batch()
                   .draw(frame,
                         position.x - tile.width() / 2.0f,
                         position.y - tile.height() / 2.0f,
                         tile.width(),
                         tile.height());
        });
    }

    @Override
    public void close() {
        this.texture.dispose();
    }
}
