package com.kyrutech.chipper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.kyrutech.chipper.screens.BettingScreen;
import com.kyrutech.chipper.screens.GameOverScreen;
import com.kyrutech.chipper.screens.GameScreen3;
import com.kyrutech.chipper.screens.InstructionsScreen;
import com.kyrutech.chipper.screens.MenuScreen;

public class ChipperGame extends Game implements InputProcessor {
	private OrthographicCamera camera;
	
	public ChipperEngine engine;
	
	private static final int RENDER_MENU = 0;
	private static final int RENDER_PLAY = 1;
	private static final int RENDER_GAMEOVER = 2;
	private static final int RENDER_INSTRUCTIONS = 3;
	private static final int RENDER_BETTING = 4;
	
	private int renderState = RENDER_MENU;
	
	public static final String VERSION = "1.4";
	
	public MenuScreen menuScreen;
//	public GameScreen gameScreen;
	public GameScreen3 gameScreen3;
	public GameOverScreen gameOverScreen;
	public InstructionsScreen instructionScreen;
	public BettingScreen bettingScreen;
	
	@Override
	public void create() {		
		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(w, h);
		
		menuScreen = new MenuScreen(this);
//		gameScreen = new GameScreen(this);
		gameScreen3 = new GameScreen3(this);
		gameOverScreen = new GameOverScreen(this);
		instructionScreen = new InstructionsScreen(this);
		bettingScreen = new BettingScreen(this);
		
		setScreen(menuScreen);
	}

	/**
	 * Initialize main game
	 */
	public void initializePlay() {
		renderState = RENDER_PLAY;
		engine = new ChipperEngine();
		engine.initialize();
		
		setScreen(gameScreen3);
//		Gdx.input.setInputProcessor(gameScreen);

//		engine.shuffleDeck();
//		engine.dealCards();
	}
	
	/**
	 * Goes to the betting screen
	 */
	public void viewBetting() {
		renderState = RENDER_BETTING;
		setScreen(bettingScreen);
	}
	
	/**
	 * Goes to the instruction screen
	 */
	public void viewInstructions() {
		renderState = RENDER_INSTRUCTIONS;
		setScreen(instructionScreen);
	}
	
	/**
	 * Sets us at the end of the game
	 */
	public void endPlay() {
		renderState = RENDER_GAMEOVER;
		setScreen(gameOverScreen);
	}
	
	/**
	 * Returns to the main menu
	 */
	public void returnToMenu() {
		renderState = RENDER_MENU;
		setScreen(menuScreen);
	}
	
	@Override
	public void dispose() {
		menuScreen.dispose();
//		gameScreen.dispose();
		gameScreen3.dispose();
		gameOverScreen.dispose();
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		Vector3 pos = new Vector3(x, y, 0);
		camera.unproject(pos);
		
		if(renderState == RENDER_MENU) {
			
		} else if(renderState == RENDER_PLAY) {

		}
		
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
}
