package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.stages.BossStage;
import fi.jakojaannos.syvyys.stages.RegularCircleStage;

import java.util.stream.Stream;

public class TransitionStageSystem implements EcsSystem<Player> {
    @Override
    public void tick(final Stream<Player> players, final GameState gameState) {
        players.forEach(player -> {
            if (player.deathSequenceHasFinished()) {
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                    gameState.changeStage(new RegularCircleStage(1), true);
                }
                return;
            }

            if (player.grounded()) {
                gameState.getCamera().lockedToPlayer = false;
                gameState.setPlayer(player);
            } else if (gameState.getCamera().lockedToPlayer) {
                player.body().setLinearVelocity(0.0f, -20.0f);
            }

            if (player.body().getPosition().y < -100.0f) {
                var circleN = 1;
                if (gameState.getCurrentStage() instanceof RegularCircleStage circleStage) {
                    circleN = circleStage.circleN + 1;
                }

                gameState.changeStage(circleN % 10 != 0 ? new RegularCircleStage(circleN) : new BossStage(circleN), false);
            }
        });
    }
}
