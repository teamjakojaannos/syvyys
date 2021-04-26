package fi.jakojaannos.syvyys.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.Player;

import java.util.stream.Stream;

public class PlayerAbilityTickSystem implements EcsSystem<Player> {
    @Override
    public void tick(final Stream<Player> entities, final GameState gameState) {
        entities.forEach(player -> {
            final var input = player.abilityInput();

            if (input.dashInput()) {
                doDashAction(gameState, player);
            }

            if (player.isDashing(gameState)) {
                final var dashProgress = MathUtils.clamp(
                        gameState.getTimers().getTimeElapsed(player.dashTimer) / player.dashTimer.duration(),
                        0.0f,
                        1.0f
                );

                final var facingVec = new Vector2(player.facingRight ? 1.0f : -1.0f, 0.0f)
                        .scl(MathUtils.lerp(player.dashStrength, 0.0f, dashProgress) * (player.grounded() ? 1.0f : 0.5f));
                player.body().applyLinearImpulse(facingVec, player.body().getPosition(), true);
            }
        });
    }

    private void doDashAction(final GameState gameState, final Player player) {
        if (!player.canDash(gameState)) {
            return;
        }

        player.dashCooldownTimer = gameState.getTimers().set(player.dashCoolDown, false, () -> { });
        player.dashTimer = gameState.getTimers().set(player.dashDuration, false, () -> { });
        player.meNoDieTimer = gameState.getTimers().set(player.meNoDieTime, false, () -> {});

        final var position = new Vector2(player.body().getPosition())
                .add(0.0f, -0.5f);

        final var invFacingVec = new Vector2(player.facingRight ? -1.0f : 1.0f, 0.0f);
        gameState.obtainParticleEmitter()
                 .spawnBurst(gameState.getCurrentTime(),
                             10,
                             position,
                             0.01f,
                             invFacingVec,
                             0.25f,
                             0.1f, 0.75f,
                             2.5f, 5.0f,
                             0.5f, 0.75f,
                             0.0f, 0.125f,
                             new Color(1.0f, 1.0f, 1.0f, 1.0f),
                             new Color(1.0f, 1.0f, 1.0f, 1.0f),
                             new Vector2(0.25f, 0.25f)
                 );
    }
}
