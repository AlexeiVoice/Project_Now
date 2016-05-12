package com.avoice.projectnow.screens;

import com.avoice.projectnow.MGame;
import com.avoice.projectnow.characters.PlayerCharacter;
import com.avoice.projectnow.tools.B2worldCreator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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

    private boolean moveCamRight,moveCamLeft, moveCamUp, moveCamDown;


    public PlayScreen(MGame game) {
        this.game = game;

        world = new World(new Vector2(0,-10), true);

        atlas = new TextureAtlas("chars.atlas");
        gameCam = new OrthographicCamera();
        viewport = new FitViewport(MGame.V_WIDTH / MGame.PPM,
                MGame.V_HEIGHT / MGame.PPM, gameCam);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("Project_Now_test.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MGame.PPM);

        gameCam.position.set(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2, 0);
        System.out.println("CameraWidth=" + viewport.getWorldWidth() +
                ", CameraHeight=" + viewport.getWorldHeight());

        //create mario in our game world
        player = new PlayerCharacter(this);

        worldCreator = new B2worldCreator(world, map);

        mapWidth = map.getProperties().get("width", Integer.class) * 128 /MGame.PPM;
        mapHeight = map.getProperties().get("height", Integer.class) * 128 /MGame.PPM;
        System.out.println("WorldWidth=" + mapWidth +
                ", WorldHeight=" + mapHeight);

        b2dr = new Box2DDebugRenderer();//DEBUG
    }

    public void handleInput(float delta) {
        if(player.getCurrentState() != PlayerCharacter.State.DEAD) {
            if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
               player.jump();
            }
            if(Gdx.input.isKeyPressed(Keys.RIGHT) && player.getB2dBody().getLinearVelocity().x <= 5
                    ) {
                player.makeStepRight();
            }
            if(Gdx.input.isKeyPressed(Keys.LEFT) && player.getB2dBody().getLinearVelocity().x >= -5) {
                player.makeStepLeft();
            }
        }
    }

    private void update(float delta) {
        //handle input first:
        handleInput(delta);
        world.step(1/60f, 6, 2);

        player.update(delta);
        //Cam shouldn't go out of world bounds
        if((player.getB2dBody().getPosition().x >= 0.01f + viewport.getWorldWidth()/2) &&
                (player.getB2dBody().getPosition().x < mapWidth - viewport.getWorldWidth()/2)) {
            gameCam.position.x = player.getB2dBody().getPosition().x;
        }
        if((player.getB2dBody().getPosition().y >= 0.01f + viewport.getWorldHeight()/2) &&
                (player.getB2dBody().getPosition().y < mapHeight - viewport.getWorldHeight()/2)) {
            gameCam.position.y = player.getB2dBody().getPosition().y;
        }
        gameCam.update();
        //tell our renderer to draw only what our camera can see in our game world
        renderer.setView(gameCam);
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
        renderer.render();

        //DEBUG
        b2dr.render(world, gameCam.combined);

        game.getBatch().setProjectionMatrix(gameCam.combined);
        game.getBatch().begin();
        player.draw(game.getBatch());
        game.getBatch().end();
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
