package fi.jakojaannos.syvyys.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.systems.HasCharacterState;

public class PhysicsContactListener implements ContactListener {
    @Override
    public void beginContact(final Contact contact) {
        final var dataA = contact.getFixtureA().getBody().getUserData();
        final var dataB = contact.getFixtureB().getBody().getUserData();

        final var manifold = contact.getWorldManifold();
        if (resolveBeginContact(manifold, dataA, dataB)) {
            return;
        }
        resolveBeginContact(manifold, dataB, dataA);
    }

    private boolean resolveBeginContact(final WorldManifold manifold, final Object dataA, final Object dataB) {
        if (dataA instanceof HasCharacterState character && dataB instanceof Tile) {
            final var contactNormal = manifold.getNormal();
            final var directionUp = Vector2.Y;

            final float upwardsness = contactNormal.dot(directionUp);
            if (upwardsness > 0.5f) {
                character.grounded(true);
            }

            return true;
        }
        // TODO: interface TracksContactWithPlayer
        else if (dataA instanceof Player && dataB instanceof SoulTrap trap) {
            if (trap.state == SoulTrap.State.IDLE) {
                trap.state = SoulTrap.State.BUBBLING;
            }

            trap.isInContactWithPlayer = true;

            return true;
        } else if (dataA instanceof DemonBall ball && dataB instanceof Tile) {
            ball.collidedWithWall = true;
            return true;
        } else if (dataA instanceof DemonBall ball && dataB instanceof Player) {
            ball.isInContactWithPlayer = true;
            return true;
        } else if (dataA instanceof Hellspider hellspider && dataB instanceof Player) {
            if (hellspider.state == Hellspider.State.DASHING) {
                hellspider.isInContactWithPlayer = true;
            }
        }

        return false;
    }

    @Override
    public void endContact(final Contact contact) {
        final var dataA = contact.getFixtureA().getBody().getUserData();
        final var dataB = contact.getFixtureB().getBody().getUserData();

        final var manifold = contact.getWorldManifold();
        if (resolveEndContact(manifold, dataA, dataB)) {
            return;
        }
        resolveEndContact(manifold, dataB, dataA);
    }

    private boolean resolveEndContact(final WorldManifold manifold, final Object dataA, final Object dataB) {
        if (dataA instanceof Player && dataB instanceof SoulTrap trap) {
            trap.isInContactWithPlayer = false;

            return true;
        }

        return false;
    }

    @Override
    public void preSolve(final Contact contact, final Manifold oldManifold) {
    }

    @Override
    public void postSolve(final Contact contact, final ContactImpulse impulse) {
    }
}
