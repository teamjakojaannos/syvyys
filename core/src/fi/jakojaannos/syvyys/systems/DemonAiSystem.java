package fi.jakojaannos.syvyys.systems;


import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.CharacterInput;
import fi.jakojaannos.syvyys.entities.Demon;
import fi.jakojaannos.syvyys.entities.Player;

public class DemonAiSystem implements EcsSystem<Demon> {


    @Override
    public void tick(final Iterable<Demon> demons, final GameState gameState) {
        final var maybePlayer = gameState.getPlayer();

        demons.forEach(demon -> {
            if (maybePlayer.isPresent()) {
                moveTowardsPlayer(gameState, demon, maybePlayer.get());
            } else {
                doWanderMovement(gameState, demon);
            }
        });
    }

    private void doWanderMovement(final GameState gameState, final Demon demon) {
    }

    private void moveTowardsPlayer(final GameState gameState, final Demon demon, final Player player) {
        final var posDifference = new Vector2(player.body().getPosition())
                .sub(demon.body().getPosition());


        if (Math.abs(posDifference.x) > demon.maxChaseDistance) {
            return;
        }

        var attack = false;
        final float dir;

        if (Math.abs(posDifference.x) < demon.attackDistance) {
            if (Math.abs(posDifference.y) < player.height() / 2.0f) {
                attack = true;
            }
        }

        if (Math.abs(posDifference.x) < 1) {
            dir = 0.0f;
        } else {
            dir = posDifference.x > 0 ? 1 : -1;
        }

        demon.input(new CharacterInput(dir, attack, !demon.hasMoved() && !attack));
    }
}
