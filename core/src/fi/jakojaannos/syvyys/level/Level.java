package fi.jakojaannos.syvyys.level;

import fi.jakojaannos.syvyys.entities.Entity;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.List;

public class Level {

    private final List<Tile> tiles = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();

    public Level() {
    }

    public Level(final List<Tile> tiles, final List<Entity> entities) {
        this.tiles.addAll(tiles);
        this.entities.addAll(entities);
    }

    public void addTile(final Tile tile) {
        this.tiles.add(tile);
    }

    public List<Tile> getAllTiles() {
        return this.tiles;
    }

    public List<Entity> getAllEntities() {
        return this.entities;
    }
}
