package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileLevelGenerator extends LevelGenerator {
     private static final int TILE_ID_FLOOR = 0;
     private static final int TILE_ID_WALL_RIGHT = 1;
     private static final int TILE_ID_WALL_LEFT = 2;

    private final Random random;

    public TileLevelGenerator(final long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Level generateLevel(final World world) {
        final var worldStart = -150;
        final var worldLength = 200;
        final List<Tile> tiles = new ArrayList<>();

        int generated = 0;
        int previousHeight = 10;
        while (generated < worldLength) {
            final var startX = worldStart + generated;
            final var stripLength = this.random.nextInt(16) + 4;
            previousHeight = generateStrip(world, tiles, startX, startX + stripLength, previousHeight);

            generated += stripLength;
        }

        return new Level(tiles);
    }


    private int generateStrip(
            final World world,
            final List<Tile> tiles,
            final int stripStartTileX,
            final int stripEndTileX,
            final int previousStripTileY
    ) {
        final var tileWidth = 0.5f;
        final var tileHeight = 0.5f;

        final var stripTileY = this.random.nextInt(5) - 2;
        generateWall(world, tiles, stripStartTileX, stripTileY, previousStripTileY);

        final var n = stripEndTileX - stripStartTileX;
        for (int x = 0; x < n; x++) {
            final var tileX = (stripStartTileX + x);
            tiles.add(Tile.create(
                    world,
                    tileWidth, tileHeight,
                    new Vector2(tileX * tileWidth, stripTileY * tileHeight),
                    TILE_ID_FLOOR)
            );
        }

        return stripTileY;
    }

    private void generateWall(
            final World world,
            final List<Tile> tiles,
            final int stripStartTileX,
            final int stripTileY,
            final int previousStripTileY
    ) {
        if (stripTileY == previousStripTileY) {
            return;
        }

        final var tileWidth = 0.5f;
        final var tileHeight = 0.5f;

        final boolean shouldGenerateAbove = stripTileY < previousStripTileY;

        final var wallTileX = stripStartTileX - (shouldGenerateAbove ? 1 : 0);
        final var step = shouldGenerateAbove ? 1 : -1;
        for (int y = stripTileY + step; y != previousStripTileY; y += step) {
            tiles.add(Tile.create(
                    world,
                    tileWidth,
                    tileHeight,
                    new Vector2(wallTileX * tileWidth,
                                y * tileHeight),
                    shouldGenerateAbove ? TILE_ID_WALL_LEFT : TILE_ID_WALL_RIGHT)
            );
        }
    }
}
