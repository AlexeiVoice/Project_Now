package com.avoice.projectnow.characters;


import com.avoice.projectnow.MGame;
import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class PlayerCharacter extends Sprite implements Disposable{

    public enum State{ STANDING, RUNNING, JUMPING, FALLING, DEAD}
    private State currentState;
    private State previousState;
    private float stateTimer;
    private World world;
    /* box 2d body */
    private Body b2dBody;

    private TextureRegion standTexture;
    private Animation runAnimation;
    private Animation jumpAnimation;
    private Animation attackAnimation;

    private boolean runningRight;
    private boolean isDead;
    private boolean isAttacking;

    private final float SPEED = .3f;
    private final float JUMPSPEED = 6f;
    private final float MAXSPEED = 3f;

    private ParticleEffect jumpDustEffect;
    private boolean drawDustEffect;

    public PlayerCharacter(PlayScreen screen) {
        //set texture to sprite:
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;
        isDead = false;

        Array<TextureRegion> animFrames;
        //set run animation:
        animFrames = getFramesFromAtlas(screen.getAtlas(), "walk_frame", 8);
        runAnimation = new Animation(.15f, animFrames);
        animFrames.clear();
        //set jump animation
        animFrames = getFramesFromAtlas(screen.getAtlas(), "jump_frame", 3);
        jumpAnimation = new Animation(.2f, animFrames);
        animFrames.clear();
        //set attack animation
        animFrames = getFramesFromAtlas(screen.getAtlas(), "attack_frame", 4);
        attackAnimation = new Animation(.1f, animFrames);
        animFrames.clear();

        standTexture = new TextureRegion(screen.getAtlas().findRegion("walk_frame", 1));
        setBounds(0, 0, MGame.TILESIZE / MGame.PPM, MGame.TILESIZE / MGame.PPM);
        setRegion(standTexture);
        initPlayer();

        //init effects
        jumpDustEffect = new ParticleEffect();
        jumpDustEffect.load(Gdx.files.internal("jump_dust.p"), Gdx.files.internal("."));
        drawDustEffect = false;
        jumpDustEffect.scaleEffect(1/MGame.PPM);
        jumpDustEffect.setDuration(5);
    }

    public void initPlayer() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(MGame.TILESIZE / MGame.PPM, 17 * MGame.TILESIZE / MGame.PPM );
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        b2dBody = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(60 / MGame.PPM);
        fixtureDef.shape = shape;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = .15f;
        fixtureDef.filter.categoryBits = MGame.PLAYER_BIT;
        fixtureDef.filter.maskBits = MGame.GROUND_BIT;
        b2dBody.createFixture(fixtureDef).setUserData(this);

        /*now let's create sensor-fixture for feet*/
        EdgeShape feet = new EdgeShape();
        feet.set(new Vector2(-30/MGame.PPM, -70/MGame.PPM),
                new Vector2(30/MGame.PPM, -70/MGame.PPM));
        fixtureDef.shape = feet;
        fixtureDef.filter.categoryBits = MGame.PLAYER_FEET_BIT;
        fixtureDef.filter.maskBits = MGame.GROUND_BIT;
        fixtureDef.isSensor = false;
        b2dBody.createFixture(fixtureDef).setUserData(this);

    }

    public void update(float delta) {
        setPosition(b2dBody.getPosition().x - getWidth() / 2, b2dBody.getPosition().y
                - getHeight() / 2);
        setRegion(getFrame(delta));

        if(drawDustEffect && jumpDustEffect.isComplete()) {
            drawDustEffect = false;
        }
    }

    public void render(SpriteBatch batch, float delta) {
        super.draw(batch);

        if(drawDustEffect) {
            jumpDustEffect.setPosition(getBodyXPos(), getY());
            jumpDustEffect.draw(batch, delta);
        }
    }

    //region GETTERS and SETTERS
    public TextureRegion getFrame(float delta) {
        currentState = getState();
        TextureRegion region;
        switch (currentState) {
            case DEAD:
                region = standTexture;
                break;
            case JUMPING:
                region = isAttacking ? attackAnimation.getKeyFrame(stateTimer, false) :
                        jumpAnimation.getKeyFrame(stateTimer, false);
                break;
            case RUNNING:
                region = isAttacking ? attackAnimation.getKeyFrame(stateTimer, false) :
                        runAnimation.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = isAttacking ? attackAnimation.getKeyFrame(stateTimer, true) : standTexture;

                break;
        }
        if((b2dBody.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        }else if((b2dBody.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
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

    public State getPreviousState() {
        return previousState;
    }

    public Array<TextureRegion> getFramesFromAtlas(TextureAtlas atlas, String regionName, int count) {
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < count+1; i++) {
            frames.add(new TextureRegion(atlas.findRegion(regionName, i)));
        }
        return frames;
    }

    public float getBodyXPos() {
        return b2dBody.getPosition().x;
    }
    public float getBodyYPos() {
        return b2dBody.getPosition().y;
    }

    public void setDrawDustEffect(boolean drawDustEffect) {
        this.drawDustEffect = drawDustEffect;
        jumpDustEffect.start();
    }
    //endregion

    //region PHYSICAL ACTIONS
    public void attack() {
        isAttacking = true;
    }
    public void stopAttack() {
        isAttacking = false;
    }

    public void makeStepLeft() {
        //Will accelerate till reaches maxpeed
        if(b2dBody.getLinearVelocity().x >= -MAXSPEED) {
            b2dBody.applyLinearImpulse(new Vector2(-SPEED, 0), b2dBody.getWorldCenter(),
                    true);
        }
    }

    public void makeStepRight() {
        //Will accelerate till reaches maxpeed
        if(b2dBody.getLinearVelocity().x <= MAXSPEED) {
            b2dBody.applyLinearImpulse(new Vector2(SPEED, 0), b2dBody.getWorldCenter(),
                    true);
        }
    }

    public void jump() {
        b2dBody.applyLinearImpulse(new Vector2(0, JUMPSPEED), b2dBody.getWorldCenter(),
                true);
        //dust effect related things
    }
    //endregion

    @Override
    public void dispose() {
        jumpDustEffect.dispose();
    }
}
