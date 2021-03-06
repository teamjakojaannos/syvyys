package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.GameCharacter;

public class Camera {
    private final float widthInUnits = 17.5f;
    private final OrthographicCamera camera;

    private final Vector3 tmp = new Vector3();
    public boolean lockedToPlayer;
    private int screenWidth, screenHeight;
    private float heightInUnits;

    public Camera(final int windowWidth, final int windowHeight) {
        final float aspectRatio = (float) windowHeight / windowWidth;

        this.heightInUnits = aspectRatio * this.widthInUnits;
        this.camera = new OrthographicCamera(this.widthInUnits, this.heightInUnits);

        this.screenWidth = windowWidth;
        this.screenHeight = windowHeight;
    }

    public void resize(final int windowWidth, final int windowHeight) {
        final float aspectRatio = (float) windowHeight / windowWidth;

        this.heightInUnits = aspectRatio * this.widthInUnits;
        final var pos = new Vector3(this.camera.position);
        this.camera.setToOrtho(false, this.widthInUnits, this.heightInUnits);
        this.camera.position.set(pos);
        this.camera.update();

        this.screenWidth = windowWidth;
        this.screenHeight = windowHeight;
    }

    public Matrix4 getCombinedMatrix() {
        return this.camera.combined;
    }

    public void lerpNewPosition(final Vector2 newPosition) {
        this.tmp.set(newPosition, 0.0f);

        // (Tiny bit more) accurate lerp using Fused Multiply Add
        final var alpha = 0.075f;
        this.camera.position.x = Math.fma(alpha, (this.tmp.x - this.camera.position.x), this.camera.position.x);
        this.camera.position.y = Math.fma(alpha, (this.tmp.y - this.camera.position.y), this.camera.position.y);
        this.camera.position.z = Math.fma(alpha, (this.tmp.z - this.camera.position.z), this.camera.position.z);

        // Don't let the player fall off the screen
        this.camera.position.y = Math.min(this.camera.position.y, this.tmp.y + this.heightInUnits / 4.0f);
    }

    public void update() {
        this.camera.update();
    }

    public float getScreenWidth() {
        return this.screenWidth;
    }

    public float getScreenHeight() {
        return this.screenHeight;
    }

    public Vector3 getLocation() {
        return this.camera.position;
    }

    public void setLocation(final Vector2 position) {
        this.camera.position.set(position, 0.0f);
    }

    public float getWidthInUnits2(final GameState gameState) {
        return this.widthInUnits + gameState.getPlayer()
                                            .map(GameCharacter::maxSpeed)
                                            .map(x -> x * 3.0f) // * n of physics ticks for a single frame until player can see offscreen
                                            .orElse(10.0f);
    }
}
