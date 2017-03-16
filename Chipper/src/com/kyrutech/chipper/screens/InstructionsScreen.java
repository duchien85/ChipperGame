package com.kyrutech.chipper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;

public class InstructionsScreen implements Screen, InputProcessor {

	private ChipperGame game;
	
	private Stage instructionStage;
	private BitmapFont font, fontSmall, fontBig;
	private NinePatchDrawable background, antibackground;
	private Texture tilesheet, scrollpane;
	private Sprite tableBackground;
	
	private OrthographicCamera camera;
	private Rectangle viewport;
	
	private InputMultiplexer inputMultiplexer;
	
	public InstructionsScreen(ChipperGame game) {
		this.game = game;
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		instructionStage.act(Gdx.graphics.getDeltaTime());
		instructionStage.draw();
		
	}

	@Override
	public void resize(int width, int height) {
		 // calculate new viewport
        float aspectRatio = (float)width/(float)height;
        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f); 
        if(aspectRatio > Constants.ASPECT_RATIO)
        {
            scale = (float)height/(float)Constants.VIRTUAL_HEIGHT;
            crop.x = (width - Constants.VIRTUAL_WIDTH*scale)/2f;
        }
        else if(aspectRatio < Constants.ASPECT_RATIO)
        {
            scale = (float)width/(float)Constants.VIRTUAL_WIDTH;
            crop.y = (height - Constants.VIRTUAL_HEIGHT*scale)/2f;
        }
        else
        {
            scale = (float)width/(float)Constants.VIRTUAL_WIDTH;
        }

        float w = (float)Constants.VIRTUAL_WIDTH*scale;
        float h = (float)Constants.VIRTUAL_HEIGHT*scale;
        viewport = new Rectangle(crop.x, crop.y, w, h);
        
        instructionStage.setCamera(camera);
        instructionStage.setViewport(w, h, true);
		
//		instructionStage.setViewport(480, 800, true);
//		instructionStage.getCamera().position.set(480/2, 800/2, 0);

	}

	@Override
	public void show() {
		
		camera = new OrthographicCamera(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
		
		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		background = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 5, 5, 5, 5));
		antibackground = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/antibackground.png"))), 5, 5, 5, 5));
		tableBackground = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		
		scrollpane = new Texture(Gdx.files.internal("data/scrollpane.png"));
		scrollpane.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);
		
		Texture fontSmallTexture = new Texture("font/franklingothicmedium24white.png");
		fontSmallTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		fontSmall = new BitmapFont(Gdx.files.internal("font/franklingothicmedium24white.fnt"), new TextureRegion(fontSmallTexture), false);
		
		Texture fontBigTexture = new Texture("font/franklingothicmedium32white.png");
		fontBigTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		fontBig = new BitmapFont(Gdx.files.internal("font/franklingothicmedium32white.fnt"), new TextureRegion(fontBigTexture), false);
		
		instructionStage = new Stage();
		
		Table table = new Table();
//		Gdx.app.log("TABLE", table.getX() + "," + table.getY());
//		table.setFillParent(true);
//		table.pad(10);
		table.add().pad(10);
		table.row();
		
		
		ScrollPaneStyle style = new ScrollPaneStyle();
//		style.background = new SpriteDrawable(tableBackground);
		style.hScroll = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 2, 13, 14, 3), 1, 1, 1, 1));
		style.hScrollKnob = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 2, 10, 6, 3), 1, 1, 1, 1));
		style.vScroll = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 0, 0, 3, 14), 1, 1, 1, 1));
		style.vScrollKnob = new NinePatchDrawable(new NinePatch(new TextureRegion(scrollpane, 3, 1, 3, 6), 1, 1, 1, 1));
		
		ScrollPane scroll = new ScrollPane(table, style);
		scroll.setFillParent(true);
		scroll.setOverscroll(false, false);		
		
		instructionStage.addActor(scroll);
		
		
		
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = font;
		labelStyle.background = background;
		
		LabelStyle labelStyleSmall = new LabelStyle();
		labelStyleSmall.font = fontSmall;
		
		LabelStyle labelStyleBig = new LabelStyle();
		labelStyleBig.font = fontBig;
		labelStyleBig.background = background;
		
		Label title = new Label("Chipper Rules", labelStyleBig);
		table.add(title).pad(10).expandX().fill().center();
		
		table.row();
		
		Label introTitle = new Label("Introduction", labelStyle);
		table.add(introTitle).pad(10).expandX().fill();

		table.row();
		
		Label intro = new Label("Chipper is a card game played in and around West Central " +
								"Wisconsin.  If you are familiar with Euchre it should be an " +
								"easy game to pick up as they are similar.", labelStyleSmall);
		intro.setWrap(true);
		intro.setAlignment(Align.top | Align.left);
		table.add(intro).pad(10).expandX().fill();
		
		table.row();
		
		Label playersTitle = new Label("Players", labelStyle);
		table.add(playersTitle).pad(10).expandX().fill();
		
		table.row();
		
		Label players = new Label("Chipper is traditionally played with 4 players.", labelStyleSmall);
		players.setWrap(true);
		players.setAlignment(Align.top | Align.left);
		table.add(players).pad(10).expandX().fill();
		
		table.row();
		
		Label cardsTitle = new Label("Cards", labelStyle);
		table.add(cardsTitle).pad(10).expandX().fill();
		
		table.row();
		
		Label cards = new Label("A pack of 24 cards containing the 9, 10, J, Q, K, and A of " +
								"each suit is used.  When a suit is bid the rank of cards is " +
								"J of that suit, J of the same colored suit, then A, K, Q, 10, " +
								"9.  All other suits are A, K, Q, J, 10, 9.  When No Trump is " +
								"bid rank is A, K, Q, J, 10, 9 in each individual suit (lead " +
								"suit each trick is high).", labelStyleSmall);
		cards.setAlignment(Align.top | Align.left);
		cards.setWrap(true);
		table.add(cards).pad(10).expandX().fill();
		
		table.row();
		
		Label biddingTitle = new Label("Bidding", labelStyle);
		table.add(biddingTitle).pad(10).expandX().fill();
		
		table.row();
		
		Label bidding = new Label("The player to the dealer’s left starts bidding and it proceeds " +
								  "clockwise around back to the dealer.  Each player can bid or " +
								  "pass.  If a player wishes to bid it must be higher than the " +
								  "previous high bid.  A bid must from 2 to 6, or 3 to 6 if you " +
								  "intend on bidding No Trump.  You can also bid Chipper which " +
								  "means you intend to take all tricks and will automatically win " +
								  "if you do so.  Since it is the highest bid, bidding stops when " +
								  "Chipper has been bid.", labelStyleSmall);
		bidding.setWrap(true);
		table.add(bidding).pad(10).expandX().fill();
		
		table.row();
		
		Label bidding2 = new Label("Once all four players have had a chance to bid, or once Chipper " +
								   "has been bid, the highest bidder declares what suit they are " +
								   "bidding in or if they bid No Trump.  Starting from the left of " +
								   "the winning bidder and moving clockwise around the table, each " +
								   "player can decide if they want to stay or pass.  If the bid was " +
								   "Chipper or No Trump all players must stay.  ", labelStyleSmall);
		bidding2.setWrap(true);
		table.add(bidding2).pad(10).expandX().fill();

		table.row();
		
		Label playTitle = new Label("Play", labelStyle);
		table.add(playTitle).pad(10).expandX().fill();

		table.row();
		
		Label play = new Label("The winning bidder gets to play the lead card and each staying player " +
							   "then plays a card moving clockwise around the table.  If you are able " +
							   "to, you must follow suit.  If you can not follow suit you may play any " +
							   "card.  The trick goes to the highest trump, or if no trump is played the " +
							   "highest card of the suit lead. The winner of a trick leads on the next trick", labelStyleSmall);
		play.setWrap(true);
		table.add(play).pad(10).expandX().fill();
		
		table.row();
		
		Label play2 = new Label("If No Trump was the bid, on the first trick the player to the right of " +
								"the winning bidder plays first.  After the first trick play proceeds the " +
								"same as it would for a suit bid with the winner of a trick leading the next trick.", labelStyleSmall);
		play2.setWrap(true);
		table.add(play2).pad(10).expandX().fill();
		

		table.row();
		
		Label scoringTitle = new Label("Scoring", labelStyle);
		table.add(scoringTitle).pad(10).expandX().fill();
		
		table.row();
		
		Label scoring = new Label("Points are scored by taking tricks.  When you win a bid you must take at " +
								   "least that many tricks in order to score points otherwise you are set.  If you " +
								   "are not winning bidder but are staying to play you must take at least one trick " +
								   "or you are set.  If you are set you lose 6 points, there is no negative score so " +
								   "you can only go down to 0.  If you make your bid or take a trick when not the winning " +
								   "bidder you will gain a number of points equal to the number of tricks you took.  If " +
								   "you pass a hand you are exempt from all scoring.", labelStyleSmall);
		
		scoring.setWrap(true);
		table.add(scoring).pad(10).expandX().fill();
		
		table.row();
		
		Label scoring2 = new Label("The game is over when one player scores 21 or more points or has succeeded in a " +
				  				   "Chipper bid.", labelStyleSmall);
		
		scoring2.setWrap(true);
		table.add(scoring2).pad(10).expandX().fill();
		
		table.row();
		
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(this);
		inputMultiplexer.addProcessor(instructionStage);
		Gdx.input.setInputProcessor(inputMultiplexer);
		Gdx.input.setCatchBackKey(true);
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
		if(keycode == Keys.BACK) {
			game.returnToMenu();			
		}
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

}
