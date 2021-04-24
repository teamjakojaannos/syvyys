package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fi.jakojaannos.syvyys.GameState;

public record RenderContext(
        SpriteBatch batch,
        GameState gameState
) {
}
