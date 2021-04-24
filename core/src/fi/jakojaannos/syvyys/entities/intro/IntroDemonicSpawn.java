package fi.jakojaannos.syvyys.entities.intro;

import com.badlogic.gdx.math.Vector2;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.entities.Entity;

public class IntroDemonicSpawn implements Entity {
    public final Vector2 position;
    public Stage stage;
    public TimerHandle stageTimer;
    public TimerHandle flashTimer;

    public IntroDemonicSpawn(final Vector2 position) {
        this.position = position;
        this.stage = Stage.TIMPPA_HUOLLOSTA;
    }

    public enum Stage {
        TIMPPA_HUOLLOSTA,
        HATS_OFF,
        SPLIT,
        SPLURT,
        HATCHING,
        IDLE_HATCHED,
        TREMBLING
    }
}
