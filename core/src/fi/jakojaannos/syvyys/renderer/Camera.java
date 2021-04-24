package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

public class Camera {
    private final float widthInUnits = 10.0f;
    private final OrthographicCamera camera;

    public Camera(final int windowWidth, final int windowHeight) {
        final float aspectRatio = (float) windowHeight / windowWidth;

        final float heightInUnits = aspectRatio * this.widthInUnits;

        this.camera = new OrthographicCamera(this.widthInUnits, heightInUnits);
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
}
