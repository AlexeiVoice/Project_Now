package com.avoice.projectnow;

import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MGame extends Game {
	public static final int V_WIDTH = 1500;
	public static final int V_HEIGHT = 1000;
	public static final float PPM = 200;//pixels per meter
	public static final int TILESIZE = 128;

	public static final short NOTHING_BIT = 0;
	public static final short GROUND_BIT = 1;
	public static final short PLAYER_BIT = 2;
	public static final short PLAYER_FEET_BIT = 4;

	public SpriteBatch getBatch() {
		return batch;
	}

	SpriteBatch batch;
	PlayScreen playScreen;
	
	@Override
	public void create () {
		playScreen = new PlayScreen(this);
		batch = new SpriteBatch();
		setScreen(playScreen);
		Gdx.input.setInputProcessor(playScreen);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
		playScreen.dispose();
		super.dispose();
	}
}
