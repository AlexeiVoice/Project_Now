package com.avoice.projectnow.tools;

import com.avoice.projectnow.MGame;
import com.avoice.projectnow.characters.PlayerCharacter;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class WorldContactListener implements ContactListener{
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        /*collision definition*/
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        switch (cDef) {
            case MGame.PLAYER_FEET_BIT | MGame.GROUND_BIT:
                System.out.println("Contact head");
                if(fixA.getFilterData().categoryBits == MGame.PLAYER_FEET_BIT) {
                    ((PlayerCharacter)fixA.getUserData()).setDrawDustEffect(true);
                } else {
                    ((PlayerCharacter)fixB.getUserData()).setDrawDustEffect(true);
                }
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
