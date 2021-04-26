package fi.jakojaannos.syvyys.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.entities.HasHealth;
import fi.jakojaannos.syvyys.entities.Tile;

public class RayCast {
    private RayCast() {}

    public static HitInfo nearestHit(
            final World world,
            final Vector2 rayStart,
            final Vector2 rayEnd,
            final Filter filter
    ) {
        final var hitInfo = new HitInfo();
        hitInfo.closestFraction = 1.0f;
        hitInfo.closestPoint.set(rayEnd);
        hitInfo.normal.set(rayEnd).sub(rayStart).nor();
        hitInfo.thereWasAHit = false;
        world.rayCast((fixture, point, normal, fraction) -> {
            if (SyvyysGame.Constants.DEBUG_ATTACK_RAYCAST) {
                System.out.println("Hit:\t" + fixture.toString());
                System.out.println("Point:\t" + point.toString());
                System.out.println("RayStart:\t" + rayStart.toString());
                System.out.println("RayEnd:\t" + rayEnd.toString());
                System.out.println("Fraction:\t" + fraction);
                System.out.println("isSensor:\t" + fixture.isSensor());
            }

            // Ignore any sensors
            if (fixture.isSensor()) {
                return -1;
            }

            // Ignore corpses
            if (fixture.getBody().getUserData() instanceof HasHealth killable && killable.dead()) {
                return -1;
            }

            // Apply any additional filters
            if (!filter.canHit(fixture, point, normal, fraction)) {
                return -1;
            }

            hitInfo.thereWasAHit = true;
            if (fraction < hitInfo.closestFraction) {
                hitInfo.closestPoint.set(point);
                hitInfo.normal.set(normal);
                hitInfo.closestFraction = fraction;
                hitInfo.body = fixture.getBody();
            }

            return 1;
        }, rayStart, rayEnd);

        return hitInfo;
    }

    public interface Filter {
        boolean canHit(Fixture fixture, Vector2 point, Vector2 normal, float fraction);

        static Filter isTile() {
            return (fixture, point, normal, fraction) -> fixture.getBody().getUserData() instanceof Tile;
        }

        static Filter everything() {
            return (fixture, point, normal, fraction) -> true;
        }
    }
}
