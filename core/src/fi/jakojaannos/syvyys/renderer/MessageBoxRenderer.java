package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import fi.jakojaannos.syvyys.entities.MessageBox;

import java.util.Arrays;
import java.util.stream.StreamSupport;

public class MessageBoxRenderer implements EntityRenderer<MessageBox> {
    private final BitmapFont fontRegular;
    private final Texture background;
    private final TextureRegion[] frames;

    public MessageBoxRenderer() {
        this.background = new Texture("ui_background.png");
        this.frames = Arrays.stream(TextureRegion.split(this.background, 2, 2))
                            .flatMap(Arrays::stream)
                            .toArray(TextureRegion[]::new);

        final var generator = new FreeTypeFontGenerator(Gdx.files.internal("pixeled.ttf"));
        final var parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameters.size = 24;
        this.fontRegular = generator.generateFont(parameters);

        generator.dispose();
    }

    @Override
    public <I extends Iterable<MessageBox>> void render(
            final I messages,
            final RenderContext context
    ) {
        final var proj = new Matrix4(context.batch().getProjectionMatrix());
        final var transform = new Matrix4(context.batch().getTransformMatrix());
        context.batch().end();

        context.batch().setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, context.screenWidth(), context.screenHeight()));
        context.batch().setTransformMatrix(new Matrix4());
        context.batch().begin();

        StreamSupport.stream(messages.spliterator(), false).findFirst().ifPresent(message -> {
            drawBox(context);
            final var screenHeight = context.screenHeight();

            this.fontRegular.setColor(0f, 0f, 0f, 1.0f);
            this.fontRegular.draw(context.batch(), message.text, 25, screenHeight - 25);
        });

        context.batch().end();

        context.batch().setProjectionMatrix(proj);
        context.batch().setTransformMatrix(transform);
        context.batch().begin();
    }

    private void drawBox(final RenderContext context) {
        final var screenHeight = context.screenHeight();
        final var screenWidth = context.screenWidth();

        final var unit = 5.0f;
        final var margin = 5.0f;
        final var boxHeight = 100.0f;

        final var cornerSize = unit * 2;

        final var leftCorner = margin;
        final var rightCorner = screenWidth - leftCorner - margin - cornerSize;
        final var middle = leftCorner + cornerSize;
        final var middleWidth = screenWidth - cornerSize * 2 - margin * 2;
        final var middleHeight = boxHeight - cornerSize * 2 - margin * 2;
        final var startY = screenHeight - margin - cornerSize;

        context.batch().draw(this.frames[0], leftCorner, startY, cornerSize, cornerSize);
        context.batch().draw(this.frames[1], middle, startY, middleWidth, cornerSize);
        context.batch().draw(this.frames[2], rightCorner, startY, cornerSize, cornerSize);

        context.batch().draw(this.frames[3], leftCorner, startY - middleHeight, cornerSize, middleHeight);
        context.batch().draw(this.frames[4], middle, startY - middleHeight, middleWidth, middleHeight);
        context.batch().draw(this.frames[5], rightCorner, startY - middleHeight, cornerSize, middleHeight);

        context.batch().draw(this.frames[6], leftCorner, startY - cornerSize - middleHeight, cornerSize, cornerSize);
        context.batch().draw(this.frames[7], middle, startY - cornerSize - middleHeight, middleWidth, cornerSize);
        context.batch().draw(this.frames[8], rightCorner, startY - cornerSize - middleHeight, cornerSize, cornerSize);
    }

    @Override
    public void close() {
        this.fontRegular.dispose();
        this.background.dispose();
    }
}
