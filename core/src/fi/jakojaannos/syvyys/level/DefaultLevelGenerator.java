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
        final var tileWidth = 1.0f;
        final var tileHeight = 1.0f;
        final var n = 500;

        final var graphs = new ArrayList<Graph>();
        for (int i = 0; i < 50; i++) {
            graphs.add(randomGraph());
        }

        for (int x = 0; x < n; x++) {

            double total = 0;
            for (final var graph : graphs) {
                total += graph.valueAt(x);
            }
            total = total / graphs.size();
            final float yPos = (float) Math.max(total, -0.25) * 2;
            Tile.create(world, tileWidth, tileHeight, new Vector2((x - n * 0.5f) * tileWidth, yPos));
        }
    }

    private Graph randomGraph() {
        return new Graph(
                this.random.nextDouble(),
                this.random.nextDouble() * 10,
                this.random.nextDouble()
        );
    }

    private record Graph(
            double frequency,
            double offset,
            double multiplier
    ) {
        public double valueAt(final double x) {
            return Math.sin(x * this.frequency + this.offset) * this.multiplier;
        }
    }
}
