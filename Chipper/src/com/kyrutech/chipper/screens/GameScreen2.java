package com.kyrutech.chipper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;
import com.kyrutech.chipper.gameobjects.Hand;
import com.kyrutech.chipper.screens.actors.CardActor;

public class GameScreen2 implements Screen {

	ChipperGame game;

	private Stage gameStage;
	private SpriteBatch batch;
	private Texture cardsheet, tilesheet;
	private BitmapFont font;

	private float edgePadding, cardHeight, cardWidth, width, height;
	
	private CardActor cardActor[][] = new CardActor[4][13];

	public GameScreen2(ChipperGame game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gameStage.act(Gdx.graphics.getDeltaTime());
		gameStage.draw();

	}

	@Override
	public void resize(int width, int height) {
		gameStage.setViewport(480, 800, true);
		gameStage.getCamera().translate(-gameStage.getGutterWidth(), -gameStage.getGutterHeight(), 0);

	}

	@Override
	public void show() {
		batch = new SpriteBatch();

		gameStage = new Stage(480, 800, true, batch);

		cardsheet = new Texture(Gdx.files.internal("data/cardsheet.png"));
		cardsheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Texture fontTexture = new Texture("font/georgia32white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/georgia32white.fnt"), new TextureRegion(fontTexture), false);

		for(int suit = 0;suit<4;suit++) {
			for(int value = 0;value<13;value++) {
				cardActor[suit][value] = new CardActor(new TextureRegion(cardsheet, 
						value*Constants.SHEET_CARD_WIDTH, 
						suit*Constants.SHEET_CARD_HEIGHT, 
						Constants.SHEET_CARD_WIDTH, 
						Constants.SHEET_CARD_HEIGHT), game.engine);
				cardActor[suit][value].setVisible(false);
			}
		}

		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		edgePadding = (Gdx.graphics.getHeight()*0.0125f);
		cardWidth = (Gdx.graphics.getWidth()*0.15f);
		cardHeight = (Gdx.graphics.getHeight()*0.12f);

		
		Gdx.input.setInputProcessor(gameStage);
	}
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		gameStage.dispose();

	}

}
