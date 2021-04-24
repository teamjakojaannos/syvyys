package fi.jakojaannos.syvyys.stages;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.renderer.Renderer;

public interface GameStage {
    GameState createState();

    void tick(float deltaSeconds, GameState gameState);

    void systemTick(GameState gameState);

    void lateSystemTick(Renderer renderer, GameState gameState);
}
