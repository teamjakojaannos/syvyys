package fi.jakojaannos.syvyys.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class HitInfo {
    public final Vector2 closestPoint = new Vector2();
    public final Vector2 normal = new Vector2();
    public Body body;
    public float closestFraction = 1.0f;
    public boolean thereWasAHit = false;
}
