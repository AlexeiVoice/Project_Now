package com.avoice.projectnow.tools;

import com.avoice.projectnow.MGame;
import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class B2worldCreator {

    public B2worldCreator(World newWorld, TiledMap tiledMap) {

        World world = newWorld;
        TiledMap map = tiledMap;
        //Create ground bodies and fixtures
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;
        //ground layer has index 2 (index starts from the bottom in the Tiled layers view)
        for (MapObject object : map.getLayers().get("GroundLayer").getObjects().
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
        PolylineMapObject polylineObject = (PolylineMapObject)map.getLayers().get(2).getObjects().
                get("worldBounds");
        ChainShape chainShape = getPolyline(polylineObject);
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(polylineObject.getPolyline().getOriginX()/MGame.PPM,
                polylineObject.getPolyline().getOriginY()/MGame.PPM);
        body = world.createBody(bdef);

        fixtureDef.shape = chainShape;
        body.createFixture(chainShape, 1);

        shape.dispose();
        chainShape.dispose();
    }

    private ChainShape getPolyline(PolylineMapObject polylineObject) {
        float[] vertices = polylineObject.getPolyline().getTransformedVertices();
        Vector2[] worldVertices = new Vector2[vertices.length / 2];

        for (int i = 0; i < vertices.length / 2; ++i) {
            worldVertices[i] = new Vector2();
            worldVertices[i].x = vertices[i * 2] / MGame.PPM;
            worldVertices[i].y = vertices[i * 2 + 1] / MGame.PPM;
        }
        ChainShape chain = new ChainShape();
        chain.createChain(worldVertices);
        return chain;
    }
}
