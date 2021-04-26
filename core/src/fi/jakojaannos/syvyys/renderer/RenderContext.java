package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import fi.jakojaannos.syvyys.GameState;

public record RenderContext(
        SpriteBatch batch,
        GameState gameState,
        Camera camera,
        RenderLayer layer
) {
    public float screenWidth() {
        return this.camera.getScreenWidth();
    }

    public float screenHeight() {
        return this.camera.getScreenHeight();
    }
}
