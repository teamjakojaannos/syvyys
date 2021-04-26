package fi.jakojaannos.syvyys.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import fi.jakojaannos.syvyys.GameState;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.Upgrade;
import fi.jakojaannos.syvyys.entities.*;
import fi.jakojaannos.syvyys.stages.RegularCircleStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileLevelGenerator extends LevelGenerator {
    protected static final int[] TILE_ID_FLOOR = {0, 0, 0, 1, 2};
    protected static final int TILE_ID_WALL_RIGHT = 3;
    protected static final int TILE_ID_WALL_LEFT = 4;
    protected static final int[] TILE_ID_CEILING = {5, 5, 5, 6, 7};
    protected static final int[] TILE_ID_DECORATIVE = {8, 9};

    private static final int SHOP_HALLWAY_HEIGHT = 4;


    private final Random random;
    private final Random enemyRandom;
    private final float createTrapChance;
    private final float spawnDemonChance;
    private final float spawnHellspiderChance;
    private final int worldLength;

    public TileLevelGenerator(
            final long seed,
            final float createTrapChance,
            final float spawnDemonChance,
            final float spawnHellspiderChance,
            final int worldLength
    ) {
        this.random = new Random(seed);
        this.enemyRandom = new Random();
        this.createTrapChance = createTrapChance;
        this.spawnDemonChance = spawnDemonChance;
        this.spawnHellspiderChance = spawnHellspiderChance;
        this.worldLength = worldLength;
    }

    @Override
    public Level generateLevel(final World world, final GameState gameState) {
        final var worldStart = -7;
        final List<Tile> tiles = new ArrayList<>();
        final List<Entity> entities = new ArrayList<>();

        int generated = 0;
        int previousHeight = 200;
        int lastEndX = worldStart;
        final var isFirstCircle = gameState.getCurrentStage() instanceof RegularCircleStage circle && circle.circleN == 1;
        boolean isFirst = SyvyysGame.Constants.GENERATE_SHOP && !isFirstCircle;
        while (generated < this.worldLength) {
            final var startX = worldStart + generated;
            final var stripLength = this.random.nextInt(16) + 4;
            previousHeight = generateStrip(world, tiles, entities, startX, startX + stripLength, previousHeight, isFirst, gameState);

            generated += stripLength;
            lastEndX = startX + stripLength;
            isFirst = false;
        }

        generateWall(world, tiles, lastEndX, -300, previousHeight, false);

        return new Level(tiles, entities);
    }


    protected int generateStrip(
            final World world,
            final List<Tile> tiles,
            final List<Entity> entities,
            final int stripStartTileX,
            final int stripEndTileX,
            final int previousStripTileY,
            final boolean isFirst,
            final GameState gameState
    ) {
        final var tileWidth = 0.5f;
        final var tileHeight = 0.5f;

        final var stripTileY = this.random.nextInt(5) - 2;
        generateWall(world, tiles, stripStartTileX, stripTileY, previousStripTileY, isFirst);

        if (isFirst) {
            generateShop(world, tiles, entities, stripStartTileX, gameState, tileWidth, tileHeight, stripTileY);
        }

        final var n = stripEndTileX - stripStartTileX;
        for (int x = 0; x < n; x++) {
            final var tileX = (stripStartTileX + x);
            final var position = new Vector2(tileX * tileWidth, stripTileY * tileHeight);

            final var isNotInSpawn = position.x > 15.0f;
            final var isFarInTheLevel = position.x > 100.0f;
            final var isNotOnEdge = x > 3 && x + 3 < n;
            final var isSuitablePositionForTrap = isNotInSpawn && isNotOnEdge;
            if (isSuitablePositionForTrap && this.enemyRandom.nextFloat() < this.createTrapChance) {
                entities.add(SoulTrap.create(world, new Vector2(position).add(0.0f, tileHeight)));
            }

            final var isSuitablePositionForDemon = isNotInSpawn;
            if (isSuitablePositionForDemon && this.enemyRandom.nextFloat() < this.spawnDemonChance) {
                entities.add(Demon.create(world, new Vector2(position).add(0.0f, tileHeight)));
            }

            final var isSuitablePositionForSpoder = isNotInSpawn && isFarInTheLevel;
            if (isSuitablePositionForSpoder && this.enemyRandom.nextFloat() < this.spawnHellspiderChance) {
                entities.add(Hellspider.create(world, new Vector2(position).add(0.0f, tileHeight)));
            }

            tiles.add(Tile.create(
                    world,
                    tileWidth, tileHeight,
                    position,
                    randomTile(TILE_ID_FLOOR))
            );

            // Spawn decor
            if (this.enemyRandom.nextFloat() < 0.5f && !SyvyysGame.Constants.DEBUG_PHYSICS) {
                final var decorYOffset = MathUtils.random(-5, 10);
                if (decorYOffset != 0) {
                    tiles.add(Tile.create(
                            world,
                            tileWidth, tileHeight,
                            position.add(0.0f, decorYOffset * tileHeight),
                            randomTile(TILE_ID_DECORATIVE),
                            false,
                            decorYOffset < 0
                                    ? new Color(1.0f, 1.0f, 1.0f, 1.0f)
                                    : new Color(0.2f, 0.2f, 0.2f, 0.2f))
                    );
                }
            }
        }

        return stripTileY;
    }

    protected void generateShop(
            final World world,
            final List<Tile> tiles,
            final List<Entity> entities,
            final int stripStartTileX,
            final GameState gameState,
            final float tileWidth,
            final float tileHeight,
            final int stripTileY
    ) {
        final var shopHallwayLength = 20;
        final int shopWidth = 15;
        final int shopHeight = 8;

        for (int x = 0; x < shopHallwayLength + shopWidth; x++) {
            final int tileX = stripStartTileX - 1 - x;
            final var positionFloor = new Vector2(tileX * tileWidth, stripTileY * tileHeight);

            final var ceilOffset = x < shopHallwayLength ? SHOP_HALLWAY_HEIGHT : shopHeight;
            final var positionCeil = new Vector2(tileX * tileWidth, (stripTileY + ceilOffset) * tileHeight);
            tiles.add(Tile.create(
                    world,
                    tileWidth, tileHeight,
                    positionFloor,
                    randomTile(TILE_ID_FLOOR)
            ));

            tiles.add(Tile.create(
                    world,
                    tileWidth, tileHeight,
                    positionCeil,
                    randomTile(TILE_ID_CEILING)
            ));
        }

        final int shopLeftX = stripStartTileX - shopHallwayLength - shopWidth;
        final int shopRightX = shopLeftX + shopWidth;
        final int shopCeilingY = stripTileY + shopHeight;
        generateWall(world, tiles, shopLeftX, stripTileY, shopCeilingY, false);
        generateWall(world, tiles, shopRightX, shopCeilingY, stripTileY + SHOP_HALLWAY_HEIGHT, false);

        final var shopItemCount = 3;
        final int shopItemMargin = 2;
        final var shopItemWidth = (shopWidth - shopItemMargin) / (float) shopItemCount;
        final var shopItemStartX = shopLeftX + shopItemMargin;
        final var added = new ArrayList<Upgrade>();
        for (int i = 0; i < shopItemCount; i++) {
            final var itemX = (shopItemStartX + i * shopItemWidth) * tileWidth + 0.5f;

            // HACK: remove from pool instead of get to make sure we get no duplicates. Added back after selection
            if (gameState.upgradePool.size() == 0) {
                break;
            }
            final var upgrade = gameState.upgradePool.remove(MathUtils.random(gameState.upgradePool.size() - 1));
            added.add(upgrade);

            entities.add(ShopItem.create(world, new Vector2(itemX, (stripTileY + 2) * tileHeight + 0.25f), upgrade));
        }
        gameState.upgradePool.addAll(added);
    }

    protected int randomTile(final int[] tiles) {
        return tiles[MathUtils.random(tiles.length - 1)];
    }

    protected void generateWall(
            final World world,
            final List<Tile> tiles,
            final int stripStartTileX,
            final int stripTileY,
            final int previousStripTileY,
            final boolean isFirst
    ) {
        if (stripTileY == previousStripTileY) {
            return;
        }

        final var tileWidth = 0.5f;
        final var tileHeight = 0.5f;

        final boolean shouldGenerateAbove = stripTileY < previousStripTileY;
        final int firstOffset = isFirst ? SHOP_HALLWAY_HEIGHT : 0; // HACK: Generate "doorway" if this is the first wall

        final var wallTileX = stripStartTileX - (shouldGenerateAbove ? 1 : 0);
        final var step = shouldGenerateAbove ? 1 : -1;
        for (int y = stripTileY + step + firstOffset; y != previousStripTileY; y += step) {
            tiles.add(Tile.create(
                    world,
                    tileWidth,
                    tileHeight,
                    new Vector2(wallTileX * tileWidth,
                                y * tileHeight),
                    shouldGenerateAbove ? TILE_ID_WALL_LEFT : TILE_ID_WALL_RIGHT)
            );
        }
    }
}
