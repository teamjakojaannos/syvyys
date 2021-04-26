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
        super(0L, 0, 0, 0, 0);
    }

    @Override
    public Level generateLevel(
            final World world,
            final GameState gameState
    ) {
        final List<Tile> tiles = new ArrayList<>();
        final List<Entity> entities = new ArrayList<>();

        generateWall(world, tiles, +2, 0, 300, true);
        generateWall(world, tiles, -2, 300, 0, false);

        generateFloor(world, tiles, -2, 20, 0, TILE_ID_FLOOR);
        generateFloor(world, tiles, +2, 20, 5, TILE_ID_CEILING);

        generateFloor(world, tiles, 20, 60, 0, TILE_ID_FLOOR);

        generateWall(world, tiles, 60, 0, 300, false);

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
}
