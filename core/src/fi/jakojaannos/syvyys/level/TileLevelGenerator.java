package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.SoulTrap;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileLevelGenerator extends LevelGenerator {
    private static final int TILE_ID_FLOOR = 0;
    private static final int TILE_ID_WALL_RIGHT = 1;
    private static final int TILE_ID_WALL_LEFT = 2;

    private final Random random;
    private final float createTrapChance;

    public TileLevelGenerator(final long seed, final float createTrapChance) {
        this.random = new Random(seed);
        this.createTrapChance = createTrapChance;
    }

    @Override
    public Level generateLevel(final World world) {
        final var worldStart = -7;
        final var worldLength = 200;
        final List<Tile> tiles = new ArrayList<>();
        final List<Entity> entities = new ArrayList<>();

        int generated = 0;
        int previousHeight = 200;
        int lastEndX = worldStart;
        while (generated < worldLength) {
            final var startX = worldStart + generated;
            final var stripLength = this.random.nextInt(16) + 4;
            previousHeight = generateStrip(world, tiles, entities, startX, startX + stripLength, previousHeight);

            generated += stripLength;
            lastEndX = startX + stripLength;
        }

        generateWall(world, tiles, lastEndX, -300, previousHeight);

        return new Level(tiles, entities);
    }


    private int generateStrip(
            final World world,
            final List<Tile> tiles,
            final List<Entity> entities,
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
            final var position = new Vector2(tileX * tileWidth, stripTileY * tileHeight);

            final var isNotInSpawn = Math.abs(position.x) > 10.0f;
            final var isNotOnEdge = x > 3 && x + 3 < n;
            final var isSuitablePosition = isNotInSpawn && isNotOnEdge;
            if (isSuitablePosition && this.random.nextFloat() < this.createTrapChance) {
                entities.add(SoulTrap.create(world, new Vector2(position).add(0.0f, tileHeight)));
            }

            tiles.add(Tile.create(
                    world,
                    tileWidth, tileHeight,
                    position,
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
