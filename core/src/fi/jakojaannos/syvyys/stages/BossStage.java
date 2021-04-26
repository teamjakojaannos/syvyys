package fi.jakojaannos.syvyys.stages;

import fi.jakojaannos.syvyys.level.BossLevelGenerator;
import fi.jakojaannos.syvyys.level.TileLevelGenerator;

public class BossStage extends RegularCircleStage {
    public BossStage(final int circleN) {
        super(circleN);
    }

    @Override
    protected TileLevelGenerator createLevelGenerator() {
        return new BossLevelGenerator();
    }
}
