package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import fi.jakojaannos.syvyys.entities.ShopItem;

import java.util.Arrays;

public class ShopItemRenderer implements EntityRenderer<ShopItem> {
    private final BitmapFont fontRegular;
    private final BitmapFont fontGothic;
    private final Texture abilityIcons;
    private final TextureRegion[] abilityFrames;


    public ShopItemRenderer() {
        this.abilityIcons = new Texture("ui_ability_icons.png");

        final var genRegular = new FreeTypeFontGenerator(Gdx.files.internal("pixeled.ttf"));
        final var paramRegular = new FreeTypeFontGenerator.FreeTypeFontParameter();
        paramRegular.size = 24;
        this.fontRegular = genRegular.generateFont(paramRegular);

        final var genGothic = new FreeTypeFontGenerator(Gdx.files.internal("GothicPixels.ttf"));
        final var paramGothic = new FreeTypeFontGenerator.FreeTypeFontParameter();
        paramGothic.size = 48;
        this.fontGothic = genGothic.generateFont(paramGothic);

        genRegular.dispose();
        genGothic.dispose();

        this.abilityFrames = Arrays.stream(TextureRegion.split(this.abilityIcons, 16, 16))
                                   .flatMap(Arrays::stream)
                                   .toArray(TextureRegion[]::new);
    }

    @Override
    public boolean rendersLayer(final RenderLayer layer) {
        return layer == RenderLayer.FOREGROUND;
    }

    @Override
    public <I extends Iterable<ShopItem>> void render(
            final I items,
            final RenderContext context
    ) {
        items.forEach(shopItem -> {
            context.batch().setColor(0.75f, 0.1f, 0.1f, 1.0f);
            context.batch().draw(this.abilityFrames[shopItem.upgrade.iconIndex() % this.abilityFrames.length],
                                 shopItem.body().getPosition().x - 0.5f,
                                 shopItem.body().getPosition().y - 0.5f,
                                 1.0f,
                                 1.0f);
        });

        final var proj = new Matrix4(context.batch().getProjectionMatrix());
        context.batch().end();

        context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.batch().setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, context.screenWidth(), context.screenHeight()));
        context.batch().begin();

        items.forEach(shopItem -> {
            final var screenHeight = context.screenHeight();
            final var screenWidth = context.screenWidth();

            if (shopItem.isInContactWithPlayer && context.gameState().getPlayer().isPresent()) {

                this.fontGothic.setColor(0.8f, 0.95f, 0.8f, 1.0f);
                this.fontRegular.setColor(0.9f, 0.9f, 0.9f, 1.0f);

                final var y = screenHeight * 0.8f;
                final var y2 = screenHeight * 0.2f;
                this.fontGothic.draw(context.batch(),
                                     shopItem.upgrade.label().get(context.gameState(), context.gameState().getPlayer().get()),
                                     0.0f,
                                     y,
                                     screenWidth,
                                     Align.center,
                                     true);

                final var canAfford = context.gameState().souls >= shopItem.upgrade.cost();
                this.fontRegular.draw(context.batch(),
                                      String.format("%s (%d)",
                                                    canAfford ? "Press space to purchase" : "Not enough souls",
                                                    shopItem.upgrade.cost()),
                                      0.0f,
                                      y2,
                                      screenWidth,
                                      Align.center,
                                      false);

                if (canAfford && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    // Deduct cost
                    context.gameState().souls -= shopItem.upgrade.cost();

                    // Apply the upgrade
                    shopItem.upgrade.action().apply(context.gameState(), context.gameState().getPlayer().get());

                    // Update pool
                    context.gameState().upgradePool.remove(shopItem.upgrade);
                    context.gameState().upgradePool.addAll(Arrays.asList(shopItem.upgrade.unlocks().get()));

                    // Delete the item
                    context.gameState().deletThis(shopItem);
                }
            }
        });

        context.batch().end();

        context.batch().setProjectionMatrix(proj);
        context.batch().begin();
    }

    @Override
    public void close() {
        this.fontGothic.dispose();
        this.fontRegular.dispose();
        this.abilityIcons.dispose();
    }
}
