package fi.jakojaannos.syvyys.systems;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.CharacterInput;
import fi.jakojaannos.syvyys.entities.HasEnemyAI;
import fi.jakojaannos.syvyys.entities.Player;

import java.util.stream.Stream;

public class DemonAiSystem implements EcsSystem<HasEnemyAI> {
    @Override
    public void tick(final Stream<HasEnemyAI> enemies, final GameState gameState) {
        final var maybePlayer = gameState.getPlayer();

        enemies.filter(HasEnemyAI::shouldTickAi)
               .forEach(enemy -> {
                   if (maybePlayer.map(player -> !player.dead())
                                  .orElse(false)) {
                       moveTowardsPlayer(gameState, enemy, maybePlayer.get());
                   } else {
                       doWanderMovement(gameState, enemy);
                   }

                   gameState.obtainParticleEmitter()
                            .spawnBurst(gameState.getCurrentTime(),
                                        1, enemy.body().getPosition(),
                                        0.5f,
                                        new Vector2(0.0f, -1.0f),
                                        1.0f,
                                        0.1f, 0.5f,
                                        0.5f, 1.0f,
                                        0.4f, 0.7f,
                                        0.0f, 0.0f,
                                        new Color(1.0f, 0.2f, 0.2f, 1.0f),
                                        new Color(0.2f, 0.2f, 0.2f, 1.0f),
                                        new Vector2(0.0f, 0.0f));
               });
    }

    private void doWanderMovement(final GameState gameState, final HasEnemyAI demon) {
        demon.input(new CharacterInput(0.0f, false, false));
    }

    private void moveTowardsPlayer(final GameState gameState, final HasEnemyAI demon, final Player player) {
        final var posDifference = new Vector2(player.body().getPosition())
                .sub(demon.body().getPosition());


        if (Math.abs(posDifference.x) > demon.maxChaseDistance()) {
            return;
        }

        var attack = false;
        final float dir;

        // TODO: raycast check if can see player
        if (Math.abs(posDifference.x) < demon.attackDistance()) {
            if (Math.abs(posDifference.y) < player.height() / 2.0f || demon.canAttackFromDifferentY()) {
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
