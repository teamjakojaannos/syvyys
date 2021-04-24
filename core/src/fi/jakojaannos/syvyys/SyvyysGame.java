package fi.jakojaannos.syvyys;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import fi.jakojaannos.syvyys.level.DefaultLevelGenerator;
import fi.jakojaannos.syvyys.renderer.Renderer;

import java.util.List;

public class SyvyysGame extends ApplicationAdapter {
    private Renderer renderer;

    private Player player;
    private float accumulator;

    private World physicsWorld;
    private GameState gameState;

    @Override
    public void create() {
        this.renderer = new Renderer();

        this.physicsWorld = new World(new Vector2(0.0f, -9.81f), true);

        this.player = Player.create(this.physicsWorld, new Vector2(3.0f, 3.0f));
        this.gameState = new GameState();

        new DefaultLevelGenerator(666).generateLevel(this.physicsWorld);
    }

    private void tick(final float deltaSeconds) {
        this.gameState.getTimers().tick(deltaSeconds);

        final float frameTime = Math.min(deltaSeconds, 0.25f);
        this.accumulator += frameTime;
        while (this.accumulator >= Constants.TIME_STEP) {
            final int leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                    Gdx.input.isKeyPressed(Input.Keys.A)
                    ? 1 : 0;

            final int rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                    Gdx.input.isKeyPressed(Input.Keys.D)
                    ? 1 : 0;

            final boolean attackPressed = Gdx.input.isKeyPressed(Input.Keys.Z) ||
                    Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

            final boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

            this.player.input = new Player.Input(
                    rightPressed - leftPressed,
                    attackPressed,
                    jumpPressed
            );
            Player.tick(List.of(this.player), this.gameState);

            this.physicsWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            this.accumulator -= Constants.TIME_STEP;
        }
    }

    @Override
    public void render() {
        tick(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(1, 0, 0, 1);

        this.renderer.getCamera().lerpNewPosition(this.player.body().getPosition());

        this.renderer.render(this.physicsWorld, List.of(this.player));
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        this.renderer.onScreenResized(width, height);
    }

    @Override
    public void dispose() {
        this.renderer.close();
    }

    private static class Constants {
        public static final int TICKS_PER_SECOND = 50;
        public static final float TIME_STEP = 1.0f / TICKS_PER_SECOND;

        public static final int VELOCITY_ITERATIONS = 6;
        public static final int POSITION_ITERATIONS = 2;
    }
}
