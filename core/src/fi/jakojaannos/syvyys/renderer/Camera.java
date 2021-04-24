package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

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

    public void setLocation(final Vector2 position) {
        this.camera.position.set(position, 0.0f);
    }

    public void lerpNewPosition(final Vector2 newPosition){
        this.camera.position.lerp(new Vector3(newPosition, 0.0f), 0.075f);
    }

    public void update() {
        this.camera.update();
    }
}
