package fi.jakojaannos.syvyys;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.ScreenUtils;
import fi.jakojaannos.syvyys.renderer.Renderer;
import fi.jakojaannos.syvyys.stages.BossStage;
import fi.jakojaannos.syvyys.stages.GameStage;
import fi.jakojaannos.syvyys.stages.IntroStage;
import fi.jakojaannos.syvyys.stages.RegularCircleStage;

public class SyvyysGame extends ApplicationAdapter {
    private Renderer renderer;

    private float accumulator;

    private GameState gameState;
    private GameStage currentStage;

    @SuppressWarnings("NonAsciiCharacters")
    private Music kauhuambianssiPisteÄmPeeKolme;

    @Override
    public void create() {
        this.renderer = new Renderer();
        this.kauhuambianssiPisteÄmPeeKolme = Gdx.audio.newMusic(Gdx.files.internal("Paskapersekauhuambianssi.mp3"));
        this.kauhuambianssiPisteÄmPeeKolme.setVolume(0.05f);
        this.kauhuambianssiPisteÄmPeeKolme.play();
        this.kauhuambianssiPisteÄmPeeKolme.setPosition(7.5f);
        this.kauhuambianssiPisteÄmPeeKolme.setLooping(true);

        // Initialize game
        if (Constants.BOSSRUSH) {
            changeStage(new BossStage(10));
        } else if (Constants.FAST_INTRO) {
            changeStage(new RegularCircleStage(1));
        } else {
            changeStage(new IntroStage());
        }
    }

    private void changeStage(final GameStage stage) {
        if (this.currentStage != null) {
            this.currentStage.close();
        }

        this.currentStage = stage;
        this.gameState = this.currentStage.createState(this.currentStage,
                                                       this.gameState == null || this.gameState.isHardReset() ? null : this.gameState,
                                                       this.renderer.getCamera());
        this.gameState.spawnEntities();
        this.gameState.purgeEntities();

        this.renderer.getCamera().update();
    }

    private void tick(final float deltaSeconds) {
        this.gameState.getTimers().tick(deltaSeconds);

        this.currentStage.tick(deltaSeconds, this.gameState);

        final float frameTime = Math.min(deltaSeconds, 0.25f);
        this.accumulator += frameTime;
        while (this.accumulator >= Constants.TIME_STEP) {
            this.gameState.updateTime(Constants.TIME_STEP);

            this.currentStage.systemTick(this.gameState);

            this.gameState.spawnEntities();
            this.gameState.purgeEntities();

            this.gameState.getPhysicsWorld()
                          .step(Constants.TIME_STEP,
                                Constants.VELOCITY_ITERATIONS,
                                Constants.POSITION_ITERATIONS);
            this.accumulator -= Constants.TIME_STEP;

            this.currentStage.lateSystemTick(this.renderer, this.gameState);
        }

        this.gameState.getNextStage()
                      .ifPresent(this::changeStage);
    }

    @Override
    public void render() {
        tick(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(this.gameState.getBackgroundColor());
        this.renderer.render(this.gameState, this.gameState.getAllEntities());
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        this.renderer.onScreenResized(width, height);
    }

    @Override
    public void dispose() {
        this.renderer.close();
        this.kauhuambianssiPisteÄmPeeKolme.dispose();
    }

    public static class Constants {
        public static final int TICKS_PER_SECOND = 50;
        public static final float TIME_STEP = 1.0f / TICKS_PER_SECOND;

        public static final int VELOCITY_ITERATIONS = 6;
        public static final int POSITION_ITERATIONS = 2;

        public static final boolean GENERATE_SHOP = true;

        // Debug
        public static final boolean FAST_INTRO = true;
        public static final boolean BOSSRUSH = false;
        public static final boolean DEBUG_PHYSICS = true;
        public static final boolean DEBUG_ATTACK_RAYCAST = false;
        public static final boolean SATANMODE = false; // Can't be "Godmode" cuz' we're in hell ;)

        public static class Collision {
            public static final short CATEGORY_TERRAIN = 0x0001;
            public static final short CATEGORY_PLAYER = 0x0002;
            public static final short CATEGORY_ENEMY = 0x0004;
            public static final short CATEGORY_CORPSE = 0x0008;
            public static final short CATEGORY_PROJECTILE_PLAYER = 0x0010;
            public static final short CATEGORY_PROJECTILE_ENEMY = 0x0020;

            public static final short MASK_PLAYER = ~(CATEGORY_PLAYER | CATEGORY_PROJECTILE_PLAYER);
            public static final short MASK_ENEMY = ~(CATEGORY_ENEMY | CATEGORY_PROJECTILE_ENEMY);
            public static final short MASK_PROJECTILE_ENEMY = (CATEGORY_PLAYER | CATEGORY_TERRAIN);
            public static final short MASK_TERRAIN = ~CATEGORY_TERRAIN;
            public static final short MASK_CORPSE = CATEGORY_TERRAIN;
        }
    }
}
