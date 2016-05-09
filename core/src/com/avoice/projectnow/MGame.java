package com.avoice.projectnow;

import com.avoice.projectnow.screens.PlayScreen;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MGame extends Game {
	public static final int V_WIDTH = 800;
	public static final int V_HEIGHT = 400;
	public static final float PPM = 200;//pixels per meter

	public SpriteBatch getBatch() {
		return batch;
	}

	SpriteBatch batch;
	
	@Override
	public void create () {
		PlayScreen playScreen = new PlayScreen(this);
		batch = new SpriteBatch();
		setScreen(playScreen);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void render () {
		super.render();
	}
}
