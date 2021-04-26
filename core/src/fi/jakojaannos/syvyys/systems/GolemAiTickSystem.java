package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.CharacterInput;
import fi.jakojaannos.syvyys.entities.Golem;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.util.RayCast;

import java.util.stream.Stream;

public class GolemAiTickSystem implements EcsSystem<Golem> {

    @Override
    public void tick(final Stream<Golem> entities, final GameState gameState) {
        entities.forEach(golem -> {
            final var optPlayer = gameState.getPlayer();
            if (optPlayer.isEmpty()) {
                return;
            }

            boolean didAttack = false;
            if (canAttack(gameState, golem)) {
                System.out.println("Trying to attack....");
                didAttack = tryAttack(gameState, golem, optPlayer.get());
            }

            if (!didAttack) {
                tryMove(gameState, golem, optPlayer.get());
            }
        });
    }

    private void tryMove(final GameState gameState, final Golem golem, final Player player) {

        final var playerPos = player.body().getPosition();
        final var myPos = golem.body().getPosition();

        if (Math.abs(playerPos.x - myPos.x) > golem.chaseRange) {
            golem.input(new CharacterInput(0.0f, false, false));
            return;
        }

        final var moveDir = playerPos.x > myPos.x ? 1.0f : -1.0f;

        final var rayStart = new Vector2(myPos)
                .add(moveDir, 0.0f);
        final var rayEnd = new Vector2(rayStart)
                .sub(0, 5.0f);
        final var hitInfoForward = RayCast.nearestHit(gameState.getPhysicsWorld(), rayStart, rayEnd, RayCast.Filter.isTile());

        rayStart.set(myPos);
        rayEnd.set(rayStart)
              .sub(0, 5.0f);
        final var hitInfoMyFeet = RayCast.nearestHit(gameState.getPhysicsWorld(), rayStart, rayEnd, RayCast.Filter.isTile());

        if (!hitInfoForward.thereWasAHit || !hitInfoMyFeet.thereWasAHit) {
            // can't find tiles, edge or something
            golem.input(new CharacterInput(0.0f, false, false));
            return;
        }

        if (hitInfoForward.body.getPosition().y < hitInfoMyFeet.body.getPosition().y) {
            // don't go down
            golem.input(new CharacterInput(0.0f, false, false));
            return;
        }

        golem.input(new CharacterInput(moveDir, false, false));
    }

    private boolean tryAttack(final GameState gameState, final Golem golem, final Player player) {

        final var playerPos = player.body().getPosition();
        final var myPos = golem.body().getPosition();

        if (Math.abs(playerPos.x - myPos.x) > golem.attackRange) {
            return false;
        }

        golem.input(new CharacterInput(0.0f, true, false));
        golem.attackTimer = gameState.getTimers().set(golem.attackDelay, false, () -> {});
        return true;
    }

    private boolean canAttack(final GameState gameState, final Golem golem) {
        return !gameState.getTimers().isActiveAndValid(golem.attackTimer);
    }
}
