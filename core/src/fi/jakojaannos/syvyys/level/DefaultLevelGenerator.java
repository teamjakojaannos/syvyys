package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.Tile;

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

        for (int i = 0; i < 500; i++) {
            float yPos = 0.0f;
            if(this.random.nextInt(10) <= 2){
                yPos=this.random.nextFloat();
            }
            Tile.create(world, tileWidth, tileHeight, new Vector2(i * tileWidth, yPos));
        }
    }
}
