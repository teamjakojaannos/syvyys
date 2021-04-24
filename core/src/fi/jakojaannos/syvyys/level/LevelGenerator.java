package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.physics.box2d.World;

public abstract class LevelGenerator {

    public abstract Level generateLevel(World world);

}
