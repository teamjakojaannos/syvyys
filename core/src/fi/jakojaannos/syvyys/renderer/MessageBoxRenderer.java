package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import fi.jakojaannos.syvyys.entities.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

public class MessageBoxRenderer implements EntityRenderer<UI> {
    private final BitmapFont fontRegular;
    private final Texture panel;
    private final TextureRegion[] panelFrames;

    private final Texture healthBar;
    private final TextureRegion[] healthBarFrames;

    public MessageBoxRenderer() {
        this.panel = new Texture("ui_background.png");
        this.healthBar = new Texture("healthbar.png");

        this.panelFrames = Arrays.stream(TextureRegion.split(this.panel, 2, 2))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        final var tmpFrames = new ArrayList<>(List.of(Arrays.stream(TextureRegion.split(this.healthBar, 3, 3))
                                                            .flatMap(Arrays::stream)
                                                            .toArray(TextureRegion[]::new)));
        // Split produces some extra frames, kill em'
        tmpFrames.remove(17);
        tmpFrames.remove(16);
        tmpFrames.remove(15);
        tmpFrames.remove(11);
        tmpFrames.remove(10);
        tmpFrames.remove(9);
        tmpFrames.remove(5);
        tmpFrames.remove(4);
        tmpFrames.remove(3);
        final var tmpFrames2 = Arrays.stream(TextureRegion.split(this.healthBar, 3, 9))
                                     .flatMap(Arrays::stream)
                                     .toArray(TextureRegion[]::new);
        tmpFrames.add(tmpFrames2[3]);
        tmpFrames.add(tmpFrames2[4]);
        tmpFrames.add(tmpFrames2[5]);
        this.healthBarFrames = tmpFrames.toArray(TextureRegion[]::new);

        final var generator = new FreeTypeFontGenerator(Gdx.files.internal("pixeled.ttf"));
        final var parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = 24;
        this.fontRegular = generator.generateFont(parameters);

        generator.dispose();
    }

    @Override
    public <I extends Iterable<UI>> void render(
            final I uis,
            final RenderContext context
    ) {
        final var proj = new Matrix4(context.batch().getProjectionMatrix());
        final var transform = new Matrix4(context.batch().getTransformMatrix());
        context.batch().end();

        context.batch().setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, context.screenWidth(), context.screenHeight()));
        context.batch().setTransformMatrix(new Matrix4());
        context.batch().begin();

        StreamSupport.stream(uis.spliterator(), false).findFirst().ifPresent(ui -> {
            final var screenHeight = context.screenHeight();
            final var screenWidth = context.screenWidth();

            if (ui.messageText != null) {
                drawBox(context, this.panelFrames, 0, screenHeight - 100.0f, screenWidth, 100.0f, 5.0f, 5.0f);

                this.fontRegular.setColor(0f, 0f, 0f, 1.0f);
                this.fontRegular.draw(context.batch(), ui.messageText, 25, screenHeight - 25);
            }

            if (ui.showPlayerHp) {
                drawBox(context, this.healthBarFrames, 5.0f, 15.0f, screenWidth / 4, screenHeight / 15, 5.0f, 0.0f);
            }
        });

        context.batch().end();

        context.batch().setProjectionMatrix(proj);
        context.batch().setTransformMatrix(transform);
        context.batch().begin();
    }

    private void drawBox(
            final RenderContext context,
            final TextureRegion[] frames,
            final float x,
            final float y,
            final float width,
            final float height,
            final float thickness,
            final float margin
    ) {
        final var cornerSize = thickness * 2;

        final var leftCorner = x + margin;
        final var rightCorner = x + width - leftCorner - margin - cornerSize;
        final var middle = leftCorner + cornerSize;
        final var middleWidth = width - cornerSize * 2 - margin * 2;
        final var middleHeight = height - cornerSize * 2 - margin * 2;
        final var startY = y + height - margin - cornerSize;

        context.batch().draw(frames[0], leftCorner, startY, cornerSize, cornerSize);
        context.batch().draw(frames[1], middle, startY, middleWidth, cornerSize);
        context.batch().draw(frames[2], rightCorner, startY, cornerSize, cornerSize);

        context.batch().draw(frames[3], leftCorner, startY - middleHeight, cornerSize, middleHeight);
        context.batch().draw(frames[4], middle, startY - middleHeight, middleWidth, middleHeight);
        context.batch().draw(frames[5], rightCorner, startY - middleHeight, cornerSize, middleHeight);

        context.batch().draw(frames[6], leftCorner, startY - cornerSize - middleHeight, cornerSize, cornerSize);
        context.batch().draw(frames[7], middle, startY - cornerSize - middleHeight, middleWidth, cornerSize);
        context.batch().draw(frames[8], rightCorner, startY - cornerSize - middleHeight, cornerSize, cornerSize);
    }

    @Override
    public void close() {
        this.fontRegular.dispose();
        this.panel.dispose();
    }
}
