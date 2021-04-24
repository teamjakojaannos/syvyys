package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Camera {
    private final float widthInUnits = 10.0f;
    private final OrthographicCamera camera;

    private final Vector3 tmp;

    public Camera(final int windowWidth, final int windowHeight) {
        final float aspectRatio = (float) windowHeight / windowWidth;

        final float heightInUnits = aspectRatio * this.widthInUnits;

        this.camera = new OrthographicCamera(this.widthInUnits, heightInUnits);

        this.tmp = new Vector3();
    }

    public void resize(final int windowWidth, final int windowHeight) {
        final float aspectRatio = (float) windowHeight / windowWidth;

        final float heightInUnits = aspectRatio * this.widthInUnits;
        this.camera.setToOrtho(false, this.widthInUnits, heightInUnits);
    }

    public Matrix4 getProjectionMatrix() {
        return this.camera.projection;
    }

    public Matrix4 getTransformMatrix() {
        return this.camera.view;
    }

    public Matrix4 getCombinedMatrix() {
        return this.camera.combined;
    }

    public void setLocation(final Vector2 position) {
        this.camera.position.set(position, 0.0f);
    }

    public void lerpNewPosition(final Vector2 newPosition){
        this.tmp.set(newPosition, 0.0f);

        // Accurate lerp using Fused Multiply Add
        final var alpha = 0.075f;
        this.camera.position.x = Math.fma(alpha, (this.tmp.x - this.camera.position.x), this.camera.position.x);
        this.camera.position.y = Math.fma(alpha, (this.tmp.y - this.camera.position.y), this.camera.position.y);
        this.camera.position.z = Math.fma(alpha, (this.tmp.z - this.camera.position.z), this.camera.position.z);
    }

    public void update() {
        this.camera.update();
    }
}
