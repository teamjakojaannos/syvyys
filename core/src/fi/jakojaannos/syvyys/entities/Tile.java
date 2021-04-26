package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.SyvyysGame;

public record Tile(
        float width,
        float height,
        Body body,
        int tileIndex,
        Color tint,
        boolean hasCollision
) implements Entity, HasBody {
    public static Tile create(
            final World physicsWorld,
            final float width,
            final float height,
            final Vector2 position,
            final int tileIndex
    ) {
        return create(physicsWorld, width, height, position, tileIndex, true, new Color(1.0f, 1.0f, 1.0f, 1.0f));
    }

    public static Tile create(
            final World physicsWorld,
            final float width,
            final float height,
            final Vector2 position,
            final int tileIndex,
            final boolean hasCollision,
            final Color tint
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
        hbFixture.density = 0.0f;
        hbFixture.isSensor = !hasCollision;
        hbFixture.filter.categoryBits = hasCollision ? SyvyysGame.Constants.Collision.CATEGORY_TERRAIN : 0x0000;
        hbFixture.filter.maskBits = hasCollision ? SyvyysGame.Constants.Collision.MASK_TERRAIN : 0x0000;

        body.createFixture(hbFixture);
        hitBox.dispose();

        final var tile = new Tile(width, height, body, tileIndex, tint, hasCollision);
        tile.body.setUserData(hasCollision ? tile : null);
        return tile;
    }
}
