package fi.jakojaannos.syvyys.level;

import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private final List<Tile> tiles = new ArrayList<>();

    public Level() {
    }

    public Level(final List<Tile> tiles) {
        this.tiles.addAll(tiles);
    }

    public void addTile(final Tile tile) {
        this.tiles.add(tile);
    }

    public List<Tile> getAllTiles() {
        return this.tiles;
    }
}
