package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.List;

public class BossLevelGenerator extends TileLevelGenerator {

    public static final float TILE_HEIGHT = 0.5f;
    public static final float TILE_WIDTH = 0.5f;

    public BossLevelGenerator() {
        super(0L, 0, 0, 0, 0, 0);
    }

    @Override
    public Level generateLevel(
            final World world,
            final GameState gameState
    ) {
        final List<Tile> tiles = new ArrayList<>();
        final List<Entity> entities = new ArrayList<>();

        generateWall(world, tiles, +3, 4, 300, false);
        generateWall(world, tiles, -3, 0, 300, true);

        generateShop(world, tiles, entities, -3, gameState, TILE_WIDTH, TILE_HEIGHT, 0);

        generateFloor(world, tiles, -3, 20, 0, TILE_ID_FLOOR);
        generateFloor(world, tiles, +3, 20, 5, TILE_ID_CEILING);

        generateFloor(world, tiles, 20, 60, 0, TILE_ID_FLOOR);

        generateWall(world, tiles, 20, 5, 300, false);
        generateWall(world, tiles, 60, 300, 0, false);

        return new Level(tiles, entities);
    }

    private void generateFloor(
            final World world,
            final List<Tile> tiles,
            final int minTileX,
            final int maxTileX,
            final int tileY,
            final int[] ids
    ) {
        for (int ix = minTileX; ix < maxTileX; ++ix) {
            tiles.add(Tile.create(
                    world,
                    TILE_WIDTH,
                    TILE_HEIGHT,
                    new Vector2(ix * TILE_WIDTH,
                                tileY * TILE_HEIGHT),
                    randomTile(ids))
            );
        }
    }

    public Level generateBossPlatforms(final World world, final GameState gameState) {
        final List<Tile> tiles = new ArrayList<>();
        final List<Entity> entities = new ArrayList<>();

        final var arenaStartX = 20;
        final var arenaEndX = 60;

        final var firstPlatformY = 4;

        generateFloor(world, tiles, arenaStartX + 4, arenaStartX + 8, firstPlatformY, TILE_ID_FLOOR);
        generateFloor(world, tiles, arenaEndX - 8, arenaEndX - 4, firstPlatformY, TILE_ID_FLOOR);

        final var secondPlatformY = 8;
        generateFloor(world, tiles, arenaStartX + 1, arenaStartX + 4, secondPlatformY, TILE_ID_FLOOR);
        generateFloor(world, tiles, arenaEndX - 4, arenaEndX - 1, secondPlatformY, TILE_ID_FLOOR);

        final var thirdPlatformY = 12;
        generateFloor(world, tiles, arenaStartX + 5, arenaStartX + 6, thirdPlatformY, TILE_ID_FLOOR);
        generateFloor(world, tiles, arenaEndX - 6, arenaEndX - 5, thirdPlatformY, TILE_ID_FLOOR);

        return new Level(tiles, entities);
    }
}
