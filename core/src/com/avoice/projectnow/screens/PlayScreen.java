package com.avoice.projectnow.screens;

import com.avoice.projectnow.MGame;
import com.avoice.projectnow.characters.PlayerCharacter;
import com.avoice.projectnow.tools.B2worldCreator;
import com.avoice.projectnow.tools.WorldContactListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.graphics.ParticleEmitterBox2D;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;

public class PlayScreen implements Screen, InputProcessor {
    private TextureAtlas atlas;

    private World world;
    private OrthogonalTiledMapRenderer renderer;
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthographicCamera gameCam;
    private Viewport viewport;

    private PlayerCharacter player;
    private MGame game;
    private Box2DDebugRenderer b2dr;//DEBUG

    private B2worldCreator worldCreator;
    /*This two for preventing camera looking out of map bounds*/
    private float mapWidth;
    private float mapHeight;

    private RayHandler rayHandler; //ray handler, as it goes
    private ConeLight lantern; //lantern that lights the way
    private PointLight light1, light2;

    public PlayScreen(MGame game) {
        this.game = game;

        world = new World(new Vector2(0,-10), true);
        world.setContactListener(new WorldContactListener());
        //Get atlas with our textures
        atlas = new TextureAtlas("chars.atlas");

        //Set camera
        gameCam = new OrthographicCamera();
        viewport = new FitViewport(MGame.V_WIDTH / MGame.PPM, MGame.V_HEIGHT / MGame.PPM, gameCam);
        //Load map
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("Project_Now_test.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MGame.PPM);
        //Get size of the map
        mapWidth = map.getProperties().get("width", Integer.class) * MGame.TILESIZE /MGame.PPM;
        mapHeight = map.getProperties().get("height", Integer.class) * MGame.TILESIZE /MGame.PPM;
        System.out.println("WorldWidth=" + mapWidth +
                ", WorldHeight=" + mapHeight);

        gameCam.position.set(viewport.getWorldWidth()/2, mapHeight - viewport.getWorldHeight()/2, 0);
        System.out.println("CameraWidth=" + viewport.getWorldWidth() +
                ", CameraHeight=" + viewport.getWorldHeight());

        //create player in our game world
        player = new PlayerCharacter(this);

        worldCreator = new B2worldCreator(world, map);

        //Create a ray and place it somewhere
        int lightDistance = 5;
        int RAYS_NUM = 100;
        float x = 5* MGame.TILESIZE / MGame.PPM;
        float y = 6 * MGame.TILESIZE / MGame.PPM;
        //Create some light:
        rayHandler = new RayHandler(world);
        rayHandler.setShadows(true);
        rayHandler.setAmbientLight(0.5f);

        light1 = new PointLight(rayHandler, RAYS_NUM, Color.RED, lightDistance, x, y);
        light2 = new PointLight(rayHandler, RAYS_NUM, Color.BLUE, lightDistance, x * 5, y/2);
        lantern = new ConeLight(rayHandler, 4, Color.WHITE, lightDistance, player.getBodyXPos(),
                player.getBodyYPos(), 0f, 20f);
        lantern.setStaticLight(false);
        b2dr = new Box2DDebugRenderer();//DEBUG
    }

    public void handleInput(float delta) {
        if(player.getCurrentState() != PlayerCharacter.State.DEAD) {
            if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
                player.jump();
            }
            if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
                lantern.setDirection(0f);
                player.makeStepRight();
            }
            if(Gdx.input.isKeyPressed(Keys.LEFT) && player.getB2dBody().getLinearVelocity().x >= -5) {
                lantern.setDirection(180f);
                player.makeStepLeft();
            }
        }
    }

    private void update(float delta) {
        //handle input first:
        handleInput(delta);
        world.step(1/60f, 6, 2);
        player.update(delta);
        //Forbid cam to look over the world boundaries
        if((player.getB2dBody().getPosition().x >= 0.01f + viewport.getWorldWidth()/2) &&
                (player.getB2dBody().getPosition().x < mapWidth - viewport.getWorldWidth()/2)) {
            gameCam.position.x = player.getB2dBody().getPosition().x;
        }
        if((player.getB2dBody().getPosition().y >= 0.01f + viewport.getWorldHeight()/2) &&
                (player.getB2dBody().getPosition().y < mapHeight - viewport.getWorldHeight()/2)) {
            gameCam.position.y = player.getB2dBody().getPosition().y;
        }

        lantern.setPosition(player.getX(), player.getY());

        viewport.apply(); //TODO is this necessary?
        gameCam.update();
        //tell our renderer to draw only what our camera can see in our game world
        renderer.setView(gameCam);

        //TODO Set player friction to 0f when he's in the air so he won't stick to platforms
    }
    //region SCREEN OVERRIDE METHODS

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update(delta);
        renderer.render(); //draw map

        //DEBUG
        b2dr.render(world, gameCam.combined);

        game.getBatch().setProjectionMatrix(gameCam.combined);
        game.getBatch().begin();
        player.render(game.getBatch(), delta);
        game.getBatch().end();
        rayHandler.setCombinedMatrix(gameCam);
        rayHandler.updateAndRender();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        player.dispose();
    }
    //endregion
    //region Input methods
    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.UP) {
            player.attack();
        }
        if (keycode == Keys.CONTROL_LEFT) {
            System.out.println("x=" + player.getB2dBody().getPosition().x + ", y=" +
                    player.getB2dBody().getPosition().y);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Keys.UP) {
            player.stopAttack();
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
    //endregion

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getMap() {
        return map;
    }
}
