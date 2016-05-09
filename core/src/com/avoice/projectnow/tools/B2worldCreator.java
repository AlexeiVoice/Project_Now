package com.avoice.projectnow.tools;

import com.avoice.projectnow.MGame;
import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class B2worldCreator {

    public B2worldCreator(PlayScreen screen) {
        World world = screen.getWorld();
        TiledMap map = screen.getMap();
        //Create ground bodies and fixtures
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;
        //ground layer has index 2 (index starts from the bottom in the Tiled layers view)
        for (MapObject object : map.getLayers().get(2).getObjects().
                getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject)object).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MGame.PPM,
                    (rect.getY() + rect.getHeight() / 2) / MGame.PPM);
            body = world.createBody(bdef);
            shape.setAsBox((rect.getWidth() / 2) / MGame.PPM,
                    (rect.getHeight() / 2) / MGame.PPM);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
        }
    }
}
