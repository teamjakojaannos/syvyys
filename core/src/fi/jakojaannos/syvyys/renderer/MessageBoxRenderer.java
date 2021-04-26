package fi.jakojaannos.syvyys.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.entities.GameCharacter;
import fi.jakojaannos.syvyys.entities.Player;
import fi.jakojaannos.syvyys.entities.UI;
import fi.jakojaannos.syvyys.stages.RegularCircleStage;

import java.util.Arrays;
import java.util.stream.StreamSupport;

public class MessageBoxRenderer implements EntityRenderer<UI> {
    public static final Color COLOR_NOPE = new Color(0.95f, 0.5f, 0.5f, 1.0f);
    public static final Color COLOR_INACTIVE = new Color(0.25f, 0.25f, 0.25f, 1.0f);
    private final BitmapFont fontRegular;
    private final BitmapFont fontGothic;
    private final Texture panel;
    private final TextureRegion[] panelFrames;

    private final Texture healthBar;
    private final TextureRegion[] healthBarFrames;

    private final Texture abilityIcons;
    private final TextureRegion[] abilityFrames;

    public MessageBoxRenderer() {
        this.panel = new Texture("ui_background.png");
        this.abilityIcons = new Texture("ui_ability_icons.png");
        this.healthBar = new Texture("healthbar.png");

        this.panelFrames = Arrays.stream(TextureRegion.split(this.panel, 2, 2))
                                 .flatMap(Arrays::stream)
                                 .toArray(TextureRegion[]::new);
        this.healthBarFrames = Arrays.stream(TextureRegion.split(this.healthBar, 1, 9))
                                     .flatMap(Arrays::stream)
                                     .toArray(TextureRegion[]::new);
        this.abilityFrames = Arrays.stream(TextureRegion.split(this.abilityIcons, 16, 16))
                                   .flatMap(Arrays::stream)
                                   .toArray(TextureRegion[]::new);

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
    }

    @Override
    public <I extends Iterable<UI>> void render(
            final I uis,
            final RenderContext context
    ) {
        final var proj = new Matrix4(context.batch().getProjectionMatrix());
        context.batch().end();

        context.batch().setColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.batch().setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, context.screenWidth(), context.screenHeight()));
        context.batch().begin();

        StreamSupport.stream(uis.spliterator(), false).findFirst().ifPresent(ui -> {
            final var screenHeight = context.screenHeight();
            final var screenWidth = context.screenWidth();

            if (ui.messageText != null) {
                drawBox(context, this.panelFrames, 0, screenHeight - 100.0f, screenWidth, 100.0f, 5.0f, 5.0f);

                this.fontRegular.setColor(0f, 0f, 0f, 1.0f);
                this.fontRegular.draw(context.batch(), ui.messageText, 25, screenHeight - 25);
                this.fontRegular.draw(context.batch(), "z", -25, screenHeight - 50, screenWidth, Align.bottomRight, false);
            }

            if (ui.showPlayerHp && context.gameState().getPlayer().map(player -> !player.dead()).orElse(false)) {
                final var height = screenHeight / 15.0f;
                final var width = screenWidth / 4.0f;
                final var unit = height / 9.0f;
                final var fillAreaWidth = width - 2 * unit;

                final var startX = 10.0f;
                final var startY = 5.0f * unit;
                context.batch().draw(this.healthBarFrames[0], startX, startY, unit, height);
                context.batch().draw(this.healthBarFrames[3], startX + unit + fillAreaWidth, startY, unit, height);

                final var maybePlayer = context.gameState().getPlayer();
                final var currentHp = maybePlayer
                        .map(GameCharacter::health)
                        .orElse(75.0f);
                final var maxHp = maybePlayer
                        .map(GameCharacter::maxHealth)
                        .orElse(100.0f);
                final var progress = Math.max(0.0f, currentHp) / maxHp;


                final var currentHpWidth = fillAreaWidth * progress;
                final var missingHpWidth = fillAreaWidth - currentHpWidth;
                context.batch().draw(this.healthBarFrames[1], startX + unit, startY, currentHpWidth, height);
                context.batch().draw(this.healthBarFrames[2], startX + unit + currentHpWidth, startY, missingHpWidth, height);

                this.fontGothic.draw(context.batch(),
                                     String.format("%d souls", context.gameState().souls),
                                     0.0f,
                                     screenHeight,
                                     screenWidth - 15.0f,
                                     Align.topRight,
                                     false);

                final var abilitiesStartX = startX + width + unit;
                final var player = maybePlayer.get();
                for (int ability = 0; ability < this.abilityFrames.length; ++ability) {
                    if (!isAbilityUnlocked(player, ability)) {
                        continue;
                    }

                    final var abilitySize = unit * 16.0f;

                    final var isOnCooldown = isAbilityOnCooldown(context.gameState(), player, ability);
                    final var tryingToForceIt = abilityInput(player, ability);

                    final var offset = tryingToForceIt ? -unit : 0.0f;

                    context.batch().setColor(isOnCooldown
                                                     ? (tryingToForceIt ? COLOR_NOPE : COLOR_INACTIVE)
                                                     : Color.WHITE);
                    context.batch().draw(this.abilityFrames[ability],
                                         abilitiesStartX + (ability * (abilitySize + unit)) + offset,
                                         startY - unit * 4.0f + offset,
                                         abilitySize + offset * -2,
                                         abilitySize + offset * -2);
                }
            }

            final var circleN = context.gameState().getCurrentStage() instanceof RegularCircleStage circleStage
                    ? circleStage.circleN
                    : 1;

            final var postfix = switch (circleN % 10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };

            if (context.gameState().getCamera().lockedToPlayer) {
                this.fontGothic.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                final var y = screenHeight * 0.8f;
                this.fontGothic.draw(context.batch(), String.format("%d%s Circle of Hell", circleN, postfix), 0.0f, y, screenWidth, Align.center, false);
            }

            if (ui.showPlayerHp && context.gameState().getPlayer().map(GameCharacter::dead).orElse(false)) {
                this.fontGothic.setColor(0.7f, 0.1f, 0.1f, 1.0f);
                this.fontRegular.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                final var y = screenHeight * 0.8f;
                final var y2 = screenHeight * 0.2f;
                this.fontGothic.draw(context.batch(), String.format("You were slain on the %d%s Circle", circleN, postfix), 0.0f, y, screenWidth, Align.center, true);

                if (context.gameState().getPlayer().map(GameCharacter::deathSequenceHasFinished).orElse(true)) {
                    this.fontRegular.draw(context.batch(), "Press space to continue", 0.0f, y2, screenWidth, Align.center, false);
                }
            }
        });

        context.batch().end();

        context.batch().setProjectionMatrix(proj);
        context.batch().begin();
    }

    private boolean isAbilityUnlocked(final Player player, final int ability) {
        return switch (ability) {
            case 0 -> true;
            case 1 -> player.dashUnlocked;
            default -> false;
        };
    }

    private boolean isAbilityOnCooldown(final GameState gameState, final Player player, final int ability) {
        return switch (ability) {
            case 0 -> player.attacking();
            case 1 -> !player.canDash(gameState);
            default -> true;
        };
    }

    private boolean abilityInput(final Player player, final int ability) {
        return switch (ability) {
            case 0 -> player.input().attack() || player.isHoldingAttack;
            case 1 -> player.abilityInput().dashInput();
            default -> false;
        };
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
        this.fontGothic.dispose();
        this.fontRegular.dispose();
        this.panel.dispose();
        this.healthBar.dispose();
        this.abilityIcons.dispose();
    }
}
