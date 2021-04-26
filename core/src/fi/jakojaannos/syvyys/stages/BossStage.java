package fi.jakojaannos.syvyys.stages;

import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.renderer.Camera;

public class BossStage extends RegularCircleStage {
    public BossStage(final int circleN) {
        super(circleN);
    }

    @Override
    public GameState createState(
            final GameStage gameStage,
            final GameState previousState,
            final Camera camera
    ) {
        return super.createState(gameStage, previousState, camera);
    }
}
