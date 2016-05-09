package com.avoice.projectnow.characters;


import com.avoice.projectnow.MGame;
import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class PlayerCharacter extends Sprite{
    public enum State{ STANDING, RUNNING, JUMPING, FALLING, DEAD}
    private State currentState;
    private State previousState;
    private float stateTimer;
    private World world;
    /* box 2d body */
    private Body b2dBody;

    private TextureRegion standTexture;
    private Animation runAnimation;
    private TextureRegion jumpTexture;

    private boolean runningRight;
    private boolean isDead;

    public PlayerCharacter(PlayScreen screen) {
        //set texture to sprite:
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;
        isDead = false;

        Array<TextureRegion> animFrames = new Array<TextureRegion>();
        //set run animation:
        /*for (int i = 1; i < 6; i++) {
            animFrames.add(new TextureRegion(screen.getAtlas().findRegion("walk_frame", i)));
        }*/
        runAnimation = new Animation(.15f, screen.getAtlas().getRegions());
        animFrames.clear();

        standTexture = new TextureRegion(screen.getAtlas().findRegion("walkj_frame"));
        setBounds(0, 0, 128 / MGame.PPM, 128 / MGame.PPM);
        setRegion(standTexture);
        initPlayer();
    }

    public void initPlayer() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(128 / MGame.PPM, 128 / MGame.PPM );
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2dBody = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(50 / MGame.PPM);
        fixtureDef.shape = shape;
        fixtureDef.friction = 1f;
        b2dBody.createFixture(fixtureDef);
    }

    public void update(float delta) {
        setPosition(b2dBody.getPosition().x - getWidth() / 2, b2dBody.getPosition().y
                - getHeight() / 2);
        setRegion(getFrame(delta));
    }

    public TextureRegion getFrame(float delta) {
        currentState = getState();
        TextureRegion region = null;
        switch (currentState) {
            case DEAD:
                region = standTexture;
                break;
            case JUMPING:
                region = standTexture;
                break;
            case RUNNING:
                region = runAnimation.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = standTexture;
                break;
        }
        if ((b2dBody.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        } else if((b2dBody.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + delta : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        if (isDead) {
            return State.DEAD;
        }
        if ((b2dBody.getLinearVelocity().y > 0) ||
                (b2dBody.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        }
        if (b2dBody.getLinearVelocity().y < 0) {
            return State.FALLING;
        }
        if (b2dBody.getLinearVelocity().x != 0) {
            return State.RUNNING;
        }
        return State.STANDING;
    }

    public Body getB2dBody() {
        return b2dBody;
    }

    public State getCurrentState() {
        return currentState;
    }

}
