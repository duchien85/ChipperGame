package com.kyrutech.chipper.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.gameobjects.Player;

public class GameOverScreen implements InputProcessor, Screen {

	private ChipperGame game;
	
	private SpriteBatch batch;
	private Stage menuStage;
	private BitmapFont font;
	private NinePatchDrawable background, antibackground;
	private Texture tilesheet, scrollpane;
	
	private final int CELL_WIDTH = 110;
	
	public GameOverScreen(ChipperGame game) {
		this.game = game;
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		menuStage.act(Gdx.graphics.getDeltaTime());
		menuStage.draw();
		
		Table.drawDebug(menuStage);

	}

	@Override
	public void resize(int width, int height) {
		menuStage.setViewport(480, 800, true);
		menuStage.getCamera().position.set(480/2, 800/2, 0);

	}

	@Override
	public void show() {
		batch = new SpriteBatch();
		
		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//		background = new NinePatchDrawable(new NinePatch(new TextureRegion(tilesheet, Constants.TILE_WIDTH*2, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH), 6, 6, 6, 6));
		
		background = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 5, 5, 5, 5));
		antibackground = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/antibackground.png"))), 5, 5, 5, 5));
		
		scrollpane = new Texture(Gdx.files.internal("data/scrollpane.png"));
		scrollpane.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);
		
//		//Scale font right off the bat to how we scale it later
//		while(font.getBounds("How many do you bid?").width < 480*0.6f) {
//			font.scale(0.1f);
//		}
//		while(font.getBounds("How many do you bid?").width > 480*0.6f) {
//			font.scale(-0.1f);
//		}
		
		menuStage = new Stage();
		
		Table header = new Table();
		header.setBounds(0, 660, 480, 130);
		menuStage.addActor(header);
		
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = font;
		
		Label title = new Label("Game Over", labelStyle);
		header.add(title).colspan(4);
		
		header.row();
		
		Label winner = new Label(getWinningString(), labelStyle);
		header.add(winner).colspan(4);
		
		header.row();
		
		
		Table table = new Table();
//		table.setFillParent(true);	
//		table.debug();
//		table.setBackground(background);
		table.center();
		
		ScrollPaneStyle style = new ScrollPaneStyle();
		style.background = background;
		style.hScroll = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 2, 13, 14, 3), 1, 1, 1, 1));
		style.hScrollKnob = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 2, 10, 6, 3), 1, 1, 1, 1));
		style.vScroll = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 0, 0, 3, 14), 1, 1, 1, 1));
		style.vScrollKnob = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 3, 1, 3, 6), 1, 1, 1, 1));
		
		ScrollPane scroll = new ScrollPane(table, style);
		scroll.setBounds(10, 150, 460, 520);
		scroll.setOverscroll(false, false);
//		scroll.setFillParent(true);
//		scroll.setHeight(600);
		menuStage.addActor(scroll);		
		
		Label user = new Label("User", labelStyle);
		user.setAlignment(Align.center);
		table.add(user).width(CELL_WIDTH);
		Label player1 = new Label("Player 1", labelStyle);
		player1.setAlignment(Align.center);
		table.add(player1).width(CELL_WIDTH);
		Label player2 = new Label("Player 2", labelStyle);
		player2.setAlignment(Align.center);
		table.add(player2).width(CELL_WIDTH);
		Label player3 = new Label("Player 3", labelStyle);
		player3.setAlignment(Align.center);
		table.add(player3).width(CELL_WIDTH);
		
		table.row();
		
		ArrayList<String> scores0 = game.engine.getPlayers()[0].getScoreHistory();
		ArrayList<String> scores1 = game.engine.getPlayers()[1].getScoreHistory();
		ArrayList<String> scores2 = game.engine.getPlayers()[2].getScoreHistory();
		ArrayList<String> scores3 = game.engine.getPlayers()[3].getScoreHistory();
		
//		ArrayList<String> scores0 = getTestScoreHistory();
//		ArrayList<String> scores1 = getTestScoreHistory();
//		ArrayList<String> scores2 = getTestScoreHistory();
//		ArrayList<String> scores3 = getTestScoreHistory();
		
		boolean stillScores = true;
		for(int i = 0;stillScores;i++) {
			boolean hasScore[] = new boolean[] {true, true, true, true};
			if(i < scores0.size()) {				
				Label score = new Label(getScoreString(i, scores0), labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
			} else {
				Label score = new Label("", labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
				hasScore[0] = false;
			}
			if(i < scores1.size()) {
				Label score = new Label(getScoreString(i, scores1), labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
			} else {
				Label score = new Label("", labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
				hasScore[1] = false;
			}
			if(i < scores2.size()) {
				Label score = new Label(getScoreString(i, scores2), labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
			} else {
				Label score = new Label("", labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
				hasScore[2] = false;
			}
			if(i < scores3.size()) {
				Label score = new Label(getScoreString(i, scores3), labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
			} else {
				Label score = new Label("", labelStyle);
				score.setAlignment(Align.center);
				table.add(score).width(CELL_WIDTH);
				hasScore[3] = false;
			}
			if(!hasScore[0] && !hasScore[1] && !hasScore[2] && !hasScore[3]) {
				stillScores = false;
			}
			
			table.row();
		}
		
		
		Table menuButtonSection = new Table();
		menuButtonSection.center();
		menuButtonSection.setBounds(0, 10, 480, 130);
		menuStage.addActor(menuButtonSection);
		
		TextButtonStyle tbStyle = new TextButtonStyle();
		tbStyle.font = font;
		tbStyle.up = background;
		tbStyle.down = antibackground;
		
		TextButton menu = new TextButton("Return to Main Menu", tbStyle);
		menu.pad(10);
		menu.addListener(new InputListener() {

			
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				game.returnToMenu();
			}
			
		});
		
		menuButtonSection.add(menu);
		
		Gdx.input.setInputProcessor(menuStage);

	}

	/**
	 * Returns the score and annotations up to the passed point for a player
	 * @param historyPoint
	 * @param player
	 * @return
	 */
	private String getScoreString(int historyPoint, Player player) {
		StringBuilder sb = new StringBuilder();
		String score = player.getScoreHistory().get(historyPoint);
		
		if("S".equals(score)) {
			sb.append("x");
			sb.append(player.getHistoryScoreToPoint(historyPoint+1));
		} else if("SS".equals(score)) {
			sb.append("xx");
			sb.append(player.getHistoryScoreToPoint(historyPoint+1));
		} else if("P".equals(score)) {
			sb.append("P");
		} else if("C".equals(score)) {
			sb.append("C");
		} else {
			sb.append(player.getHistoryScoreToPoint(historyPoint+1));
		}
		return sb.toString();
	}

	/**
	 * Gets the score string at a point
	 * @param historyPoint
	 * @param scoreHistory
	 * @return
	 */
	private String getScoreString(int historyPoint, ArrayList<String> scoreHistory) {
		StringBuilder sb = new StringBuilder();
		String score = scoreHistory.get(historyPoint);
		
		if("S".equals(score)) {
			sb.append("x");
			sb.append(getHistoryScoreToPoint(historyPoint+1, scoreHistory));
		} else if("SS".equals(score)) {
			sb.append("xx");
			sb.append(getHistoryScoreToPoint(historyPoint+1, scoreHistory));
		} else if("P".equals(score)) {
			sb.append("P");
		} else if("C".equals(score)) {
			sb.append("C");
		} else {		
			sb.append(getHistoryScoreToPoint(historyPoint+1, scoreHistory));
		}
		return sb.toString();
	}
	
	/**
	 * Returns score up to a point
	 * @param numOfScores
	 * @param scoreHistory
	 * @return
	 */
	public int getHistoryScoreToPoint(int numOfScores, ArrayList<String> scoreHistory) {
		int score = 0;
		
		for(int i = 0;i<numOfScores;i++) {
			String thisScore = scoreHistory.get(i);
			
			if("S".equals(thisScore)) {
				score -= 6;
				if(score < 0) {
					score = 0;
				}
			} else if("SS".equals(thisScore)) {
				score -= 12;
				if(score < 0) {
					score = 0;
				}
			} else if("P".equals(thisScore)) {
				//PASSED
			} else if("C".equals(thisScore)) {
				score+=21; //Should be a Chipper and a win
			} else {
				score += Integer.parseInt(thisScore);
			}
		}
		
		return score;
	}
	
	/**
	 * Gets the winning player string
	 * @return
	 */
	public String getWinningString() {
		int winningPlayer = 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0;i<4;i++) {
			if(game.engine.getPlayers()[i].getScore() >= 21) {
				winningPlayer = i;
			}
		}
		switch(winningPlayer) {
		case 0:
			sb.append("User ");
			break;
		case 1:
		case 2:
		case 3:
			sb.append("Player ");
			sb.append(winningPlayer);
			sb.append(" ");
			break;
		}
		sb.append("Won");
		
		return sb.toString();
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
		// TODO Auto-generated method stub

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
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * For testing
	 * @return
	 */
	public ArrayList<String> getTestScoreHistory() {
		ArrayList<String> scores = new ArrayList<String>();
		
		int numScores = 25;
		for(int i = 0;i<numScores;i++) {
			int score = (int) (Math.random()*6);
			if(score == 0) {
				scores.add("S");
				scores.add("P");
			} else {
				scores.add(Integer.toString(score));
			}
		}
		
		return scores;
	}
	

}
