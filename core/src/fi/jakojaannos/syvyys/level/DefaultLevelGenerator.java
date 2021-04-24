package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.Tile;

import java.util.ArrayList;
import java.util.Random;

public class DefaultLevelGenerator extends LevelGenerator {

    private final Random random;

    public DefaultLevelGenerator(final long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Level generateLevel(final World world) {
        generateFloor(world);
        return new Level();
    }

    private void generateFloor(final World world) {
        final int n = 500;
        final var tileWidth = 1.0f;
        final var tileHeight = 1.0f;

        final var graphs = new ArrayList<Graph>();
        for (int i = 0; i < 5; i++) {
            graphs.add(Graph.randomGraph(this.random, 0.1));
        }
        final var noise = new SinNoise(graphs);

        for (int x = 0; x < n; x++) {
            final var xPos = (x - n * 0.5f) * tileWidth;
            final var yPos = Math.pow(noise.getProductAt(x), 2) + noise.getSumAt(x) * 7.5 - 15;
            Tile.create(world, tileWidth, tileHeight, new Vector2(xPos, (float) yPos));
        }
    }
}
