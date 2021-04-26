package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.TimerHandle;
import fi.jakojaannos.syvyys.util.RayCast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpikeNode extends TracksPlayerContact.Simple implements Entity, HasBody {
    public static final float RUMBLING_DURATION = 2.0f;
    public static final float SPAWN_DURATION = 0.5f;
    public static final float IDLE_DURATION = 0.1f;
    public static final float SHATTER_DURATION = 0.5f;

    public final boolean facingRight;
    private final Body body;
    private final float width;
    private final float height;
    public TimerHandle stageTimer;
    public State state;
    public float initialDelay;
    public float damage = 15.0f;

    public SpikeNode(
            final Body body,
            final boolean facingRight,
            final float initialDelay,
            final float width,
            final float height
    ) {
        this.body = body;
        this.facingRight = facingRight;
        this.initialDelay = initialDelay;
        this.width = width;
        this.height = height;
        this.state = State.INITIAL;
    }

    public Optional<State> nextState() {
        return Optional.ofNullable(switch (this.state) {
            case INITIAL -> State.HIDDEN;
            case HIDDEN -> State.RUMBLING;
            case RUMBLING -> State.UP_IN_THE_ASS_OF_TIMO;
            case UP_IN_THE_ASS_OF_TIMO -> State.IDLE;
            case IDLE -> State.DED;
            case DED -> null;
        });
    }

    public float duration(final State state) {
        return switch (state) {
            case INITIAL -> throw new IllegalStateException("Fuck you");
            case HIDDEN -> this.initialDelay;
            case RUMBLING -> RUMBLING_DURATION;
            case UP_IN_THE_ASS_OF_TIMO -> SPAWN_DURATION;
            case IDLE -> IDLE_DURATION;
            case DED -> SHATTER_DURATION;
        };
    }

    @Override
    public Body body() {
        return this.body;
    }

    public float width() {
        return this.width;
    }

    public float height() {
        return this.height;
    }


    public static SpikeNode create(
            final World physicsWorld,
            final Vector2 position,
            final float width,
            final float height,
            final boolean facingRight,
            final float initialDelay
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position).add(width / 2.0f, height / 2.0f);
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new PolygonShape();
        hitBox.setAsBox(width / 2.0f, height / 2.0f);

        final var hbFixture = new FixtureDef();
        hbFixture.shape = hitBox;
        hbFixture.isSensor = true;
        hbFixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_PROJECTILE_ENEMY;
        hbFixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_PROJECTILE_ENEMY;

        body.createFixture(hbFixture);
        hitBox.dispose();

        final var trap = new SpikeNode(body, facingRight, initialDelay, width, height);
        trap.body.setUserData(trap);
        return trap;
    }

    public static Optional<SpikeNode> tryCreate(
            final World physicsWorld,
            final float xPos,
            final float width,
            final float height,
            final float parentY,
            final boolean facingRight,
            final float initialDelay
    ) {
        final var rayDistance = 5.0f;
        final var rayStartPoint = new Vector2(xPos, parentY);
        final var rayEndPoint = new Vector2(rayStartPoint)
                .add(0, -rayDistance);
        final var maybeTopTile = getTopMostTileAt(physicsWorld, rayStartPoint, rayEndPoint);

        if (maybeTopTile.isEmpty()) {
            return Optional.empty();
        }

        final var topTile = maybeTopTile.get();
        if (topTile.y > parentY) {
            return Optional.empty();
        }

        return Optional.of(create(physicsWorld, topTile, width, height, facingRight, initialDelay));
    }

    public static List<SpikeNode> spawnSpikeStrip(
            final World world,
            final Vector2 center,
            final float width,
            final float height,
            final int stripWidth,
            final float spaceBetweenSpikes,
            final float firstSpikeDelay,
            final float additionalDelayPerSpike,
            final boolean facingRight
    ) {
        final var result = new ArrayList<SpikeNode>();

        final var maybeRoot = tryCreate(
                world,
                center.x + (facingRight ? 0 : (-width)),
                width,
                height,
                center.y,
                facingRight,
                firstSpikeDelay
        );
        if (maybeRoot.isEmpty()) {
            return result;
        }
        final var root = maybeRoot.get();

        result.add(root);

        float previousX = root.body.getPosition().x;
        float previousY = root.body.getPosition().y;

        var remaining = stripWidth;
        float spikeDelay = firstSpikeDelay;
        final var step = facingRight ? spaceBetweenSpikes : (-spaceBetweenSpikes - width);
        while (remaining > 0) {
            --remaining;

            final var xPos = previousX + step;
            spikeDelay += additionalDelayPerSpike;
            final var maybeNext = tryCreate(
                    world,
                    xPos,
                    width, height,
                    previousY,
                    facingRight,
                    spikeDelay
            );
            if (maybeNext.isEmpty()) {
                break;
            }

            final var next = maybeNext.get();
            result.add(next);
            previousX = next.body.getPosition().x;
            previousY = next.body.getPosition().y;
        }

        return result;
    }


    private static Optional<Vector2> getTopMostTileAt(
            final World world,
            final Vector2 rayStartPoint,
            final Vector2 rayEndPoint
    ) {
        final var hitInfo = RayCast.nearestHit(world, rayStartPoint, rayEndPoint, RayCast.Filter.isTile());
        return hitInfo.thereWasAHit
                ? Optional.of(hitInfo.closestPoint)
                : Optional.empty();
    }


    public enum State {
        INITIAL,
        HIDDEN,
        RUMBLING,
        UP_IN_THE_ASS_OF_TIMO,
        IDLE,
        DED,
    }
}
