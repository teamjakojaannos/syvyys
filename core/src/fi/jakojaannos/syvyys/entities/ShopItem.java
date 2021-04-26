package fi.jakojaannos.syvyys.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import fi.jakojaannos.syvyys.SyvyysGame;
import fi.jakojaannos.syvyys.Upgrade;

public class ShopItem extends TracksPlayerContact.Simple implements Entity, HasBody {
    public final Upgrade upgrade;
    private final Body body;

    public ShopItem(final Body body, final Upgrade upgrade) {
        this.body = body;
        this.upgrade = upgrade;
    }

    @Override
    public Body body() {
        return this.body;
    }

    public float width() {
        return 0.5f;
    }

    public float height() {
        return 0.5f;
    }

    public static ShopItem create(
            final World physicsWorld,
            final Vector2 position,
            final Upgrade upgrade
    ) {
        final var bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.gravityScale = 0.0f;
        bodyDef.fixedRotation = true;

        final var body = physicsWorld.createBody(bodyDef);
        final var hitBox = new CircleShape();
        hitBox.setRadius(0.5f);
        final var fixture = new FixtureDef();
        fixture.filter.categoryBits = SyvyysGame.Constants.Collision.CATEGORY_TERRAIN;
        fixture.filter.maskBits = SyvyysGame.Constants.Collision.MASK_TERRAIN;
        fixture.isSensor = true;
        fixture.shape = hitBox;
        fixture.density = 100.0f;
        fixture.friction = 0.0f;
        fixture.restitution = 0.0f;
        body.createFixture(fixture);
        hitBox.dispose();

        final var item = new ShopItem(body, upgrade);
        item.body.setUserData(item);
        return item;
    }
}
