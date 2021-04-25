package fi.jakojaannos.syvyys.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.entities.SoulTrap;
import fi.jakojaannos.syvyys.entities.Tile;

public class PhysicsContactListener implements ContactListener {
    @Override
    public void beginContact(final Contact contact) {
        final var dataA = contact.getFixtureA().getBody().getUserData();
        final var dataB = contact.getFixtureB().getBody().getUserData();

        final var manifold = contact.getWorldManifold();
        if (resolveContact(manifold, dataA, dataB)) {
            return;
        }
        if (resolveContact(manifold, dataB, dataA)) {
            return;
        }

        System.out.printf("Unhandled contact: %s -> %s%n", dataA, dataB);
    }

    private boolean resolveContact(final WorldManifold manifold, final Object dataA, final Object dataB) {
        if (dataA instanceof Player player && dataB instanceof Tile) {
            final var contactNormal = manifold.getNormal();
            final var directionUp = Vector2.Y;

            final float upwardsness = contactNormal.dot(directionUp);
            if (upwardsness > 0.5f) {
                player.grounded = true;
            }

            return true;
        } else if (dataA instanceof Player && dataB instanceof SoulTrap trap) {
            if (trap.state == SoulTrap.State.IDLE) {
                trap.state = SoulTrap.State.BUBBLING;
            }
        }

        return false;
    }

    @Override
    public void endContact(final Contact contact) {
    }

    @Override
    public void preSolve(final Contact contact, final Manifold oldManifold) {
    }

    @Override
    public void postSolve(final Contact contact, final ContactImpulse impulse) {
    }
}
