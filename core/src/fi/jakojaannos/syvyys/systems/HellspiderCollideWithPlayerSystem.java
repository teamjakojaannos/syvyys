package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Hellspider;

import java.util.stream.Stream;

public class HellspiderCollideWithPlayerSystem implements EcsSystem<Hellspider> {
    @Override
    public void tick(final Stream<Hellspider> spooders, final GameState gameState) {
        spooders.forEach(spoder -> {
            if (spoder.isInContactWithPlayer) {
                gameState.getPlayer().ifPresent(player -> {
                    player.dealDamage(spoder.attackDamage, gameState);
                    final var position = player.body().getPosition();
                    gameState.obtainParticleEmitter()
                             .spawnBurst(gameState.getCurrentTime(),
                                         25,
                                         position,
                                         0.05f,
                                         new Vector2(spoder.body().getPosition())
                                                 .sub(position)
                                                 .nor(),
                                         0.125f * 10,
                                         1.0f, 3.5f,
                                         0.05f, 0.3f,
                                         1.0f, 0.9f,
                                         0.0f, 0.0f,
                                         new Color(0.75f, 0.00f, 0.00f, 1.0f),
                                         new Color(1.0f, 0.00f, 0.00f, 1.0f),
                                         new Vector2(0, -0.25f)
                             );
                });

                spoder.isInContactWithPlayer = false;
            }
        });
    }
}
