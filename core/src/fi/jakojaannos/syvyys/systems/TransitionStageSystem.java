package fi.jakojaannos.syvyys.systems;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.stages.FirstCircleStage;

import java.util.stream.Stream;

public class TransitionStageSystem implements EcsSystem<Player> {
    @Override
    public void tick(final Stream<Player> entities, final GameState gameState) {
        entities.forEach(entity -> {
            if (entity.grounded()) {
                gameState.getCamera().lockedToPlayer = false;
            } else if (gameState.getCamera().lockedToPlayer) {
                entity.body().setLinearVelocity(0.0f, -20.0f);
            }

            if (entity.body().getPosition().y < -100.0f) {
                var circleN = 1;
                if (gameState.getCurrentStage() instanceof FirstCircleStage circleStage) {
                    circleN = circleStage.circleN + 1;
                }

                gameState.changeStage(new FirstCircleStage(circleN));
            }
        });
    }
}
