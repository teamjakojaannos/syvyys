package fi.jakojaannos.syvyys.level.garbage;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.entities.Tile;
import fi.jakojaannos.syvyys.level.Level;
import fi.jakojaannos.syvyys.level.LevelGenerator;

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
        final int n = 5000;
        final var tileWidth = 0.1f;
        final var tileHeight = 0.10f;

        final var graphs = new ArrayList<Graph>();
        for (int i = 0; i < 5; i++) {
            graphs.add(Graph.randomGraph(this.random, 0.1));
        }
        final var noise = new SinNoise(graphs);

        for (int x = 0; x < n; x++) {
            final var xPos = (x - n * 0.5f) * tileWidth;
            var yPos = Math.pow(noise.getProductAt(x), 2) + noise.getSumAt(x);
            yPos = yPos * 7.5;

            var mountainPos = noise.getProductAt(x) * 2;
            mountainPos = mountainPos * mountainPos * 125;
            if (mountainPos > yPos) {
                yPos = mountainPos;
            }

            Tile.create(world, tileWidth, tileHeight, new Vector2(xPos, (float) yPos), 0);
        }
    }
}
