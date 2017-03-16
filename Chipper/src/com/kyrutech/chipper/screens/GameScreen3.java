package com.kyrutech.chipper.screens;

import java.text.DecimalFormat;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.kyrutech.chipper.ChipperEngine;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;
import com.kyrutech.chipper.aiagent.AIAgent;
import com.kyrutech.chipper.gameobjects.Hand;
import com.kyrutech.chipper.gameobjects.Trick;

public class GameScreen3 implements Screen, InputProcessor {
	
	ChipperGame game;

//	ShapeRenderer renderer;
	
	private OrthographicCamera camera;
	private Rectangle viewport;
	private SpriteBatch batch;
	private Texture cardsheet, tilesheet;
	private Sprite cardSprites[][] = new Sprite[4][13];
	private boolean cardVisible[][] = new boolean[4][13];
	private Sprite cardBack, tableBackground;
	private Sprite arrowSprite, dealerSprite, leadSprite, heartSprite, diamondSprite, clubSprite, spadeSprite, noTrumpSprite;
	private Sprite bidPassSprite, bidTwoSprite, bidThreeSprite, bidFourSprite, bidFiveSprite, bidSixSprite, bidChipperSprite, backgroundSprite;
	private NinePatch background, antibackground;

	private BitmapFont font;

	private Rectangle bidHelp, bidHelpDisplay;
	private Rectangle bidChipper, bidSix, bidFive, bidFour, bidThree, bidTwo, bidPass;
	private Rectangle bidHearts, bidDiamonds, bidSpades, bidClubs, bidNoTrump;
	private Rectangle stay, stayPass;
	private Rectangle playerCards[];
	private Rectangle playerPlayedCard = null;
	private Rectangle nextHandButton = null;
	private Rectangle playedCardPlayer0, playedCardPlayer1, playedCardPlayer2, playedCardPlayer3;
	private Rectangle quickPlayButton;
	
	private boolean bidChipperPressed, bidSixPressed, bidFivePressed, bidFourPressed, bidThreePressed, bidTwoPressed, bidPassPressed, 
		bidHeartsPressed, bidDiamondsPressed, bidSpadesPressed, bidClubsPressed, bidNoTrumpPressed, stayPressed, stayPassPressed, nextHandButtonPressed,
		quickPlayPressed;
	
	private boolean bidHelpPressed, bidHelpShown;
	private float[] bidHelpBids;
	
	private TweenManager manager;

	private float edgePadding, cardWidth, cardHeight;
	
	private int playedCardIndex = -1;
	
	private boolean followSuit = false;
	
	//For debugging
	private boolean showCards = false;

	public GameScreen3(ChipperGame game) {
		this.game = game;
	}

	@Override
	public void render(float delta) {
		
        // update camera
        camera.update();
        camera.apply(Gdx.gl10);

        // set viewport
        Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
                          (int) viewport.width, (int) viewport.height);
                          
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
//		renderer.setColor(0, 0.5f, 0, 1);
//		renderer.begin(ShapeType.Filled);
//		renderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//		renderer.end();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		drawDealerIndicator();
		drawPlayedCardsBackground();		
		drawAICards();
		drawAIScores();
		drawUserScores();
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {			
//			drawBidWonState();
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_STAY) {
//			drawWinningBid();
			drawBidStayState();
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			drawWinningBid();
			drawLeadArrow();
			drawCurrentPlayerArrow();
			checkForCardAnimate();
			drawQuickPlay();
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_OVER) {
			checkForCardAnimate();
			drawWinningBid();
			if(!game.engine.isAnimating()) {
				drawHandOverResults();			
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_GAMEOVER) {
			game.endPlay();
		}
		
		setupUserCards();
		
		//Draw cards
		for(int suit = 0;suit<4;suit++) {
			for(int value = 0;value<13;value++) {
				if(cardVisible[suit][value]) {
					cardSprites[suit][value].draw(batch);
				}
			}
		}
		//Draw bid numbers
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_NUMBER) {
			drawBidNumberState();
			
			if(bidHelpShown) { //Displays the bid helper box
				drawBidHelp();
			}
		}
		
		//Needed after card drawing
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {			
			drawBidWonState();
			
			if(bidHelpShown) { //Displays the bid helper box
				drawBidHelp();
			}
		}
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			if(followSuit) {
				drawFollowSuit();
			}
		}
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_NO_BID) {
			drawNoBid();
		}
		
		batch.end();
		
		manager.update(Gdx.graphics.getDeltaTime());
		game.engine.update(Gdx.graphics.getDeltaTime());
	}
	
	/**
	 * Draws the bid help box on screen
	 */
	private void drawBidHelp() {
		// TODO Auto-generated method stub
		background.draw(batch, bidHelpDisplay.x, bidHelpDisplay.y, bidHelpDisplay.width, bidHelpDisplay.height);
		
		DecimalFormat dm = new DecimalFormat("0.00");
		
		float bidsX = bidHelpDisplay.x + edgePadding;
		float bidsY = bidHelpDisplay.y + bidHelpDisplay.height - edgePadding;
		float bidsWidth = bidHelpDisplay.width - (edgePadding*2);
		float textHeight = font.getBounds("HEIGHT").height;
		
		font.drawMultiLine(batch, "Bid Estimates", bidsX, bidsY, bidHelpDisplay.width, HAlignment.CENTER);
		bidsY-=textHeight + edgePadding;
		
		//Clubs
		font.drawMultiLine(batch, "Clubs", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
		font.drawMultiLine(batch, dm.format(bidHelpBids[Constants.CLUBS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
		bidsY-=textHeight + edgePadding;
		//Spades
		font.drawMultiLine(batch, "Spades", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
		font.drawMultiLine(batch, dm.format(bidHelpBids[Constants.SPADES]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
		bidsY-=textHeight + edgePadding;
		//Hearts
		font.drawMultiLine(batch, "Hearts", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
		font.drawMultiLine(batch, dm.format(bidHelpBids[Constants.HEARTS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
		bidsY-=textHeight + edgePadding;
		//Diamonds
		font.drawMultiLine(batch, "Diamonds", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
		font.drawMultiLine(batch, dm.format(bidHelpBids[Constants.DIAMONDS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
		bidsY-=textHeight + edgePadding;
		//No Trump
		font.drawMultiLine(batch, "No Trump", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
		font.drawMultiLine(batch, dm.format(bidHelpBids[Constants.NO_TRUMP]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
		bidsY-=textHeight + edgePadding;
	}

	/**
	 * Draw quick play button
	 */
	private void drawQuickPlay() {
		if(!game.engine.isQuickPlay() && game.engine.getStaying()[0] == ChipperEngine.STAYING_OUT) {
			if(quickPlayPressed) {
				antibackground.draw(batch, quickPlayButton.x, quickPlayButton.y, quickPlayButton.width, quickPlayButton.height);
			} else if(!quickPlayPressed) {
				background.draw(batch, quickPlayButton.x, quickPlayButton.y, quickPlayButton.width, quickPlayButton.height);
			}
			float textY = (quickPlayButton.y + quickPlayButton.height)-((quickPlayButton.height - (font.getBounds("Quick Play").height))/2);
			font.drawMultiLine(batch, "Quick Play", quickPlayButton.x, textY, quickPlayButton.width, HAlignment.CENTER);
		}
	}

	/**
	 * Draws the No Bid screen, virtually the same as Hand Over screen
	 */
	private void drawNoBid() {
		float overWidth = Constants.VIRTUAL_WIDTH*0.6f;
		float overHeight = Constants.VIRTUAL_HEIGHT*0.4f;
		float textHeight = font.getBounds("HEIGHT").height;
		
		background.draw(batch, -overWidth/2, -overHeight/2, overWidth, overHeight);
		float textY = (overHeight/2) - edgePadding;
		font.drawMultiLine(batch, "All Pass", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "User", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p0score = Integer.toString(game.engine.getPlayers()[0].getScore());
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p0score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 1", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p1score = Integer.toString(game.engine.getPlayers()[1].getScore());
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p1score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 2", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p2score = Integer.toString(game.engine.getPlayers()[2].getScore());
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p2score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 3", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p3score = Integer.toString(game.engine.getPlayers()[3].getScore());
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p3score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		
		if(nextHandButton == null) {
			float nextHandButtonWidth = Constants.VIRTUAL_WIDTH*0.4f;
			float nextHandButtonHeight = Constants.VIRTUAL_HEIGHT*0.08f;
			nextHandButton = new Rectangle(-nextHandButtonWidth/2, textY - nextHandButtonHeight, nextHandButtonWidth, nextHandButtonHeight);
		}
		if(nextHandButtonPressed) {
			antibackground.draw(batch,nextHandButton.x, nextHandButton.y, nextHandButton.width, nextHandButton.height);
		} else {
			background.draw(batch,nextHandButton.x, nextHandButton.y, nextHandButton.width, nextHandButton.height);
		}
		
		textY = (nextHandButton.y + nextHandButton.height) - ((nextHandButton.height - (font.getBounds("Continue").height))/2);
		font.drawMultiLine(batch, "Continue", nextHandButton.x, textY, nextHandButton.width, HAlignment.CENTER);
	}

	/**
	 * Draws the follow suit dialog
	 */
	private void drawFollowSuit() {
		float followWidth = Constants.VIRTUAL_WIDTH*0.5f;
		float followHeight = Constants.VIRTUAL_HEIGHT*0.1f;
		background.draw(batch, -followWidth/2, -followHeight/2, followWidth, followHeight);

		float textY = (followHeight/2) - ((followHeight - font.getBounds("Follow Suit").height)/2);
		font.drawMultiLine(batch, "Follow Suit", -followWidth/2, textY, followWidth, HAlignment.CENTER);
	}

	/**
	 * Checks to see if the engine wants us to animate a card being played
	 */
	private void checkForCardAnimate() {
		if(game.engine.isAnimating()) {
			if(game.engine.getAnimateTrickCard() != -1) {
				int player = game.engine.getAnimateTrickCard();
				int card[] = game.engine.getCurrentTrick().getPlayerCard(player);
				float startingX = 0, startingY = 0, endingX = 0, endingY = 0;
				switch(player) {
				case 1:
					startingX = -(Constants.VIRTUAL_WIDTH/2) + edgePadding;
					startingY = -(cardHeight/2);
					endingX = -cardWidth;
					endingY = -(cardHeight/2);
					break;
				case 2:
					startingX = -(cardWidth/2);
					startingY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - cardHeight;
					endingX = -(cardWidth/2);
					endingY = cardHeight/2;
					break;
				case 3:
					startingX = (Constants.VIRTUAL_WIDTH/2) - edgePadding - cardWidth;
					startingY = -(cardHeight/2);
					endingX = 0;
					endingY = -(cardHeight/2);
					break;
				}
				cardSprites[card[0]][card[1]].setBounds(startingX, startingY, cardWidth, cardHeight);
				cardVisible[card[0]][card[1]] = true;
				
				float animationDuration = 0.25f;
				
				if(game.engine.isQuickPlay()) {
					animationDuration = 0.04f;
				} else {
					animationDuration = 0.25f;
				}
				
				Tween.to(cardSprites[card[0]][card[1]], SpriteAccessor.POSITION_XY, animationDuration)
					.target(endingX, endingY)						
					.ease(Quad.INOUT)
					.setCallbackTriggers(TweenCallback.COMPLETE)
					.setCallback(new TweenCallback() {
						@Override
						public void onEvent(int type, BaseTween<?> source) {
							game.engine.setAnimateTrickCard(-1);
							game.engine.setAnimating(false);
						}						
					}).start(manager);
			} else if(game.engine.getAnimateTakenTrick() != -1) {
				int player = game.engine.getAnimateTakenTrick();
				float endingX = 0, endingY = 0;
				switch(player) {
				case 0:
					endingX = -(cardWidth/2);
					endingY = -(Constants.VIRTUAL_HEIGHT/2) + edgePadding;
					break;
				case 1:					
					endingX = -(Constants.VIRTUAL_WIDTH/2) + edgePadding;
					endingY = -(cardHeight/2);
					break;
				case 2:
					endingX = -(cardWidth/2);
					endingY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - cardHeight;
					break;
				case 3:
					endingX = (Constants.VIRTUAL_WIDTH/2) - edgePadding - cardWidth;
					endingY = -(cardHeight/2);
					break;
				}
				Trick trick = game.engine.getLastTrick();
				for(int i = 0;i<4;i++) {
					if(game.engine.getStaying()[i] == ChipperEngine.STAYING_STAY) {
						final int card[] = trick.getPlayerCard(i);
						
						float animationDuration = 0.25f;
						
						if(game.engine.isQuickPlay()) {
							animationDuration = 0.04f;
						} else {
							animationDuration = 0.25f;
						}
						
						Tween.to(cardSprites[card[0]][card[1]], SpriteAccessor.POSITION_XY, animationDuration)
							.target(endingX, endingY)						
							.ease(Quad.INOUT)
							.setCallbackTriggers(TweenCallback.COMPLETE)
							.setCallback(new TweenCallback() {
								@Override
								public void onEvent(int type, BaseTween<?> source) {
									game.engine.setAnimateTakenTrick(-1);
									game.engine.setAnimating(false);
									cardVisible[card[0]][card[1]] = false;
								}						
							}).start(manager);
					}
				}
			}
		}
	}

	@Override
	public void show() {
		float w = Constants.VIRTUAL_WIDTH;
		float h = Constants.VIRTUAL_HEIGHT;
		
		camera = new OrthographicCamera(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
		batch = new SpriteBatch();
		
//		renderer = new ShapeRenderer();
		
		edgePadding = (h*0.0125f);
		cardWidth = (w*0.15f);
		cardHeight = (h*0.12f);
				
		cardsheet = new Texture(Gdx.files.internal("data/cardsheet.png"));
		cardsheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
//		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);

		for(int suit = 0;suit<4;suit++) {
			for(int value = 0;value<13;value++) {
				cardSprites[suit][value] = new Sprite(new TextureRegion(cardsheet, value*Constants.SHEET_CARD_WIDTH, suit*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
				cardVisible[suit][value] = false;
			}
		}
		
		cardBack = new Sprite(new TextureRegion(cardsheet, 2*Constants.SHEET_CARD_WIDTH, 4*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		
		bidPassSprite = new Sprite(new TextureRegion(cardsheet, 0, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidTwoSprite = new Sprite(new TextureRegion(cardsheet, Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidThreeSprite = new Sprite(new TextureRegion(cardsheet, 2*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidFourSprite = new Sprite(new TextureRegion(cardsheet, 3*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidFiveSprite = new Sprite(new TextureRegion(cardsheet, 4*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidSixSprite = new Sprite(new TextureRegion(cardsheet, 5*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		bidChipperSprite = new Sprite(new TextureRegion(cardsheet, 6*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		backgroundSprite = new Sprite(new TextureRegion(cardsheet, 7*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		
		arrowSprite = new Sprite(new TextureRegion(tilesheet, 0, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		dealerSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		leadSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*3, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		heartSprite = new Sprite(new TextureRegion(tilesheet, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		spadeSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		diamondSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		clubSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*3, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		noTrumpSprite = new Sprite(new TextureRegion(tilesheet, 0, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		tableBackground = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		
		background = new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 5, 5, 5, 5);
		antibackground = new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/antibackground.png"))), 5, 5, 5, 5);
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);

		//Scale font right off the bat to how we scale it later
//		while(font.getBounds("How many do you bid?").width < w*0.6f) {
//			font.scale(0.1f);
//		}
//		while(font.getBounds("How many do you bid?").width > w*0.6f) {
//			font.scale(-0.1f);
//		}
		
		float bidButtonWidth = w*0.5f;
		float bidButtonHeight = w*0.12f;
		float bidButtonPadding = w*0.01f;
		float bidButtonY = bidButtonHeight*2;
		bidChipper = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidSix = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidFive = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidFour = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidThree = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidTwo = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidButtonY-=bidButtonPadding;
		bidPass = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		
		float suitButtonWidth = w*0.25f;
		float noTrumpButtonHeight = w*0.12f;
		float suitButtonOffset = w*0.02f;
		
		bidHearts = new Rectangle(-suitButtonWidth, 0+suitButtonOffset, suitButtonWidth, suitButtonWidth);
		bidDiamonds = new Rectangle(0, 0+suitButtonOffset, suitButtonWidth, suitButtonWidth);
		bidSpades = new Rectangle(-suitButtonWidth, -suitButtonWidth+suitButtonOffset, suitButtonWidth, suitButtonWidth);
		bidClubs = new Rectangle(0, -suitButtonWidth+suitButtonOffset, suitButtonWidth, suitButtonWidth);
		bidNoTrump = new Rectangle(-suitButtonWidth, -suitButtonWidth-noTrumpButtonHeight, suitButtonWidth*2, noTrumpButtonHeight);
		
		
		stay = new Rectangle(-bidButtonWidth/2, bidButtonPadding/2, bidButtonWidth, bidButtonHeight);
		stayPass = new Rectangle(-bidButtonWidth/2, -bidButtonHeight - (bidButtonPadding/2), bidButtonWidth, bidButtonHeight);
		
		playerPlayedCard = new Rectangle(-cardWidth/2, -cardHeight/2 - cardHeight, cardWidth, cardHeight);
		playedCardPlayer0 = new Rectangle(-cardWidth/2, -cardHeight/2 - cardHeight, cardWidth, cardHeight);
		playedCardPlayer1 = new Rectangle(-cardWidth, -cardHeight/2, cardWidth, cardHeight);
		playedCardPlayer2 = new Rectangle(-cardWidth/2, cardHeight/2, cardWidth, cardHeight);
		playedCardPlayer3 = new Rectangle(0, -cardHeight/2, cardWidth, cardHeight);
		
		quickPlayButton = new Rectangle(-cardWidth, -cardHeight/2 - cardHeight, cardWidth*2, bidButtonHeight);
		
		bidHelp = new Rectangle((Constants.VIRTUAL_WIDTH/2)-edgePadding-cardWidth, -(Constants.VIRTUAL_HEIGHT*0.7f)/2, cardWidth, cardHeight);
		
		float bidsWidth = Constants.VIRTUAL_WIDTH*0.5f;
		float bidsHeight = Constants.VIRTUAL_HEIGHT*0.25f;
		bidHelpDisplay = new Rectangle((Constants.VIRTUAL_WIDTH/2)-edgePadding-bidsWidth, bidHelp.y, bidsWidth, bidsHeight);
		
		manager = new TweenManager();
		
		Tween.registerAccessor(Sprite.class, new SpriteAccessor());
		
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);
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
//        Gdx.app.log("VIEWPORT", viewport.x + ":" + viewport.y + ":" + viewport.width + ":" + viewport.height);

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.BACK) {
			game.returnToMenu();			
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 pos = new Vector3(screenX, screenY, 0);
//		camera.unproject(pos);
		camera.unproject(pos, viewport.x, viewport.y, viewport.width, viewport.height);
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_NUMBER) {
						
			if(bidHelpShown) {
				return true;
			}
			
			int highBid = Constants.BID_PASS;
			for(int i = 1;i<4;i++) {
				if(game.engine.getPlayers()[i].getBidValue() > highBid) {
					highBid = game.engine.getPlayers()[i].getBidValue();
				}
			}
			boolean bid = false;
			if(game.engine.getCurrentBidder() == 0) { //Only allow clicking when it's actually the players bid
				if(bidChipper.contains(pos.x, pos.y)) {
					bidChipperPressed = true;
				} else if(bidSix.contains(pos.x, pos.y) && highBid < Constants.BID_SIX) {
					bidSixPressed = true;
				} else if(bidFive.contains(pos.x, pos.y) && highBid < Constants.BID_FIVE) {
					bidFivePressed = true;
				} else if(bidFour.contains(pos.x, pos.y) && highBid < Constants.BID_FOUR) {
					bidFourPressed = true;
				} else if(bidThree.contains(pos.x, pos.y) && highBid < Constants.BID_THREE) {
					bidThreePressed = true;
				} else if(bidTwo.contains(pos.x, pos.y) && highBid < Constants.BID_TWO) {
					bidTwoPressed = true;
				} else if(bidPass.contains(pos.x, pos.y)) {
					bidPassPressed = true;
				} else if(bidHelp.contains(pos.x, pos.y)) {
					bidHelpPressed = true;
				}
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {
			boolean bid = false;
			if(bidHearts.contains(pos.x, pos.y)) {
				bidHeartsPressed = true;
			} else if(bidDiamonds.contains(pos.x, pos.y)) {
				bidDiamondsPressed = true;
			} else if(bidSpades.contains(pos.x, pos.y)) {
				bidSpadesPressed = true;
			} else if(bidClubs.contains(pos.x, pos.y)) {
				bidClubsPressed = true;
			} else if(bidNoTrump.contains(pos.x, pos.y)) {
				bidNoTrumpPressed = true;
			} else if(bidHelp.contains(pos.x, pos.y)) {
				bidHelpPressed = true;
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_STAY) {
			if(game.engine.getCurrentStayer() == 0) { //Only allow clicking when it's actually the players turn to stay
				if(stay.contains(pos.x, pos.y)){
					stayPressed = true;
				} else if(stayPass.contains(pos.x, pos.y)) {
					stayPassPressed = true;
				}
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			if(quickPlayButton.contains(pos.x, pos.y) && game.engine.getStaying()[0] == ChipperEngine.STAYING_OUT) { //Only register if user is out
				quickPlayPressed = true;
			}

		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_OVER) {
			if(nextHandButton.contains(pos.x, pos.y)) {
				nextHandButtonPressed = true;
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_NO_BID) {
			if(nextHandButton.contains(pos.x, pos.y)) {
				nextHandButtonPressed = true;
			}
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 pos = new Vector3(screenX, screenY, 0);
//		camera.unproject(pos);
		camera.unproject(pos, viewport.x, viewport.y, viewport.width, viewport.height);
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_NUMBER) {
			
			
			if(bidHelpShown) { //Catch here and return
				bidHelpShown = false;
				bidHelpPressed = false;
				return true;
			}
			
			if(bidHelpPressed && bidHelp.contains(pos.x, pos.y)) {
				bidHelpBids = AIAgent.getBids(game.engine.getPlayers()[0].getHand());
				bidHelpShown = true;
			}
			
			int highBid = Constants.BID_PASS;
			for(int i = 1;i<4;i++) {
				if(game.engine.getPlayers()[i].getBidValue() > highBid) {
					highBid = game.engine.getPlayers()[i].getBidValue();
				}
			}
			boolean bid = false;
			if(bidChipperPressed && bidChipper.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_CHIPPER);
				bid = true;
//				Gdx.app.log("BIDCLICK", "chipper");
			} else if(bidSixPressed && bidSix.contains(pos.x, pos.y) && highBid < Constants.BID_SIX) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_SIX);
				bid = true;
//				Gdx.app.log("BIDCLICK", "6");
			} else if(bidFivePressed && bidFive.contains(pos.x, pos.y) && highBid < Constants.BID_FIVE) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_FIVE);
				bid = true;				
//				Gdx.app.log("BIDCLICK", "5");
			} else if(bidFourPressed && bidFour.contains(pos.x, pos.y) && highBid < Constants.BID_FOUR) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_FOUR);
				bid = true;
//				Gdx.app.log("BIDCLICK", "4");
			} else if(bidThreePressed && bidThree.contains(pos.x, pos.y) && highBid < Constants.BID_THREE) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_THREE);
				bid = true;
//				Gdx.app.log("BIDCLICK", "3");
			} else if(bidTwoPressed && bidTwo.contains(pos.x, pos.y) && highBid < Constants.BID_TWO) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_TWO);
				bid = true;
//				Gdx.app.log("BIDCLICK", "2");
			} else if(bidPassPressed && bidPass.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_PASS);
				bid = true;
//				Gdx.app.log("BIDCLICK", "pass");
			}
			if(bid) {
				game.engine.handlePlayerBid();
			}
			//Reset all flags on up
			bidChipperPressed = false;
			bidSixPressed = false;
			bidFivePressed = false;
			bidFourPressed = false;
			bidThreePressed = false;
			bidTwoPressed = false;
			bidPassPressed = false;
			bidHelpPressed = false;
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {
			
			
			if(bidHelpShown) {	//Catch here and return
				bidHelpShown = false;
				return true;
			}
			
			if(bidHelpPressed && bidHelp.contains(pos.x, pos.y)) {
				bidHelpShown = true;
			}
			
			boolean bid = false;
			if(bidHeartsPressed && bidHearts.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.HEARTS);
				bid = true;
//				Gdx.app.log("SUITCLICK", "hearts");
			} else if(bidDiamondsPressed && bidDiamonds.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.DIAMONDS);
				bid = true;
//				Gdx.app.log("SUITCLICK", "diamonds");
			} else if(bidSpadesPressed && bidSpades.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.SPADES);
				bid = true;
//				Gdx.app.log("SUITCLICK", "spades");
			} else if(bidClubsPressed && bidClubs.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.CLUBS);
				bid = true;
//				Gdx.app.log("SUITCLICK", "clubs");
			} else if(bidNoTrumpPressed && bidNoTrump.contains(pos.x, pos.y) && game.engine.getPlayers()[0].getBidValue() > 2) { //Need to bid at least 3 to go no trump
				game.engine.getPlayers()[0].setBidSuit(Constants.NO_TRUMP);
				bid = true;
//				Gdx.app.log("SUITCLICK", "no trump");
			}
			if(bid) {
				game.engine.handlePlayerSuit();
			}
			//Reset all flags on up
			bidHeartsPressed = false;
			bidDiamondsPressed = false;
			bidSpadesPressed = false;
			bidClubsPressed = false;
			bidNoTrumpPressed = false;		
			bidHelpPressed = false;
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_STAY) {
			if(stayPressed && stay.contains(pos.x, pos.y)){
				game.engine.playerStaying(ChipperEngine.STAYING_STAY);
			} else if(stayPassPressed && stayPass.contains(pos.x, pos.y)) {
				game.engine.playerStaying(ChipperEngine.STAYING_OUT);
			}
			//Reset all flags on up
			stayPressed = false;
			stayPassPressed = false;
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			if(quickPlayPressed && quickPlayButton.contains(pos.x, pos.y) && game.engine.getStaying()[0] == ChipperEngine.STAYING_OUT) { //Only register is user it out
				game.engine.setQuickPlay(true);
			}
			if(isFollowSuit()) {
				game.engine.setSelectedCard(-1);
//				Gdx.app.log("SELECTEDCARD", "isFollowSuit");
				setFollowSuit(false);
			} else {
				if(game.engine.getCurrentPlayer() == 0) {
					if(game.engine.getSelectedCard() != -1 && playerPlayedCard.contains(pos.x, pos.y)) {
						int[] card = game.engine.getPlayers()[0].getHand().getCards().get(game.engine.getSelectedCard());
						
						if(game.engine.canFollowSuit(0) && !game.engine.isFollowingSuit(card)) { //Not following suit and can
							setFollowSuit(true); //Display follow suit prompt
						} else { //Is following suit or can't follow suit
							playedCardIndex = game.engine.getSelectedCard();
							
							Tween.to(cardSprites[card[0]][card[1]], SpriteAccessor.POSITION_XY, 0.25f)
								.target(playerPlayedCard.x, playerPlayedCard.y)
								.ease(Quad.INOUT)
								.setCallbackTriggers(TweenCallback.COMPLETE)
								.setCallback(new TweenCallback() {
									@Override
									public void onEvent(int type, BaseTween<?> source) {
										game.engine.playPlayerCard(game.engine.getSelectedCard());
										game.engine.setSelectedCard(-1);
//										Gdx.app.log("SELECTEDCARD", "playPlayerCard");
										game.engine.setAnimating(false);
										playedCardIndex = -1;
									}						
								}).start(manager);
							
							game.engine.setAnimating(true);	
						}
					}
				}
			}
			
			//Reset flags
			quickPlayPressed = false;
			
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_OVER) {
			if(nextHandButtonPressed && nextHandButton.contains(pos.x, pos.y)) {
				//Reset card visibility
				for(int suit = 0;suit<4;suit++) {
					for(int value = 0;value<13;value++) {
						cardVisible[suit][value] = false;	
						cardSprites[suit][value].setColor(Color.WHITE);
					}
				}
				game.engine.nextHand();
			}
			//Reset flag on up
			nextHandButtonPressed = false;
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_NO_BID) {
			if(nextHandButtonPressed && nextHandButton.contains(pos.x, pos.y)) {
				//Reset card visibility
				for(int suit = 0;suit<4;suit++) {
					for(int value = 0;value<13;value++) {
						cardVisible[suit][value] = false;
						cardSprites[suit][value].setColor(Color.WHITE);		
					}
				}
				game.engine.nextHand();
			}
			//Reset flag on up
			nextHandButtonPressed = false;
		}
//		Gdx.app.log("touchDown", "original: " + screenX + "," + screenY + " -- unprojected: " + pos.x + "," + pos.y);
		for(int card = 0;card < playerCards.length; card++) {
			if(playerCards[card].contains(pos.x, pos.y)) {
				//Select the card
				if(game.engine.getSelectedCard() == -1) {
					game.engine.setSelectedCard(card);
				}
				//Un-select the card
				else if(game.engine.getSelectedCard() == card) {
					game.engine.setSelectedCard(-1);
//					Gdx.app.log("SELECTEDCARD", "unSelected");
				}
				//Move the cards
				else {
					game.engine.swapPlayerCards(game.engine.getSelectedCard(), card);
					game.engine.setSelectedCard(-1);
//					Gdx.app.log("SELECTEDCARD", "moveCard");
				}
			}
		}

//		engine.nextDealer();

		return true;
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
	 * Draws the dealer indicator
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawDealerIndicator() {
		//Draw dealer indicator
		float spriteWidth = (Constants.VIRTUAL_WIDTH*0.075f);

		dealerSprite.setSize(spriteWidth, spriteWidth);
//		dealerSprite.setOrigin(spriteWidth/2, spriteWidth/2);
		switch(game.engine.getDealer()) {
		case 0:
			//			dealerSprite.setRotation(180);
			dealerSprite.setPosition(-spriteWidth-spriteWidth, -(Constants.VIRTUAL_HEIGHT/2) + edgePadding + cardHeight);
			break;
		case 1:
			//			dealerSprite.setRotation(90);
			dealerSprite.setPosition(-(Constants.VIRTUAL_WIDTH/2) + edgePadding + cardHeight, spriteWidth);
			break;
		case 2:
			//			dealerSprite.setRotation(0);
			dealerSprite.setPosition(spriteWidth, Constants.VIRTUAL_HEIGHT/2 - edgePadding - cardHeight - spriteWidth);
			break;
		case 3:
			//			dealerSprite.setRotation(270);
			dealerSprite.setPosition(Constants.VIRTUAL_WIDTH/2 - edgePadding - cardHeight - spriteWidth, - spriteWidth - spriteWidth);
			break;
		}
		dealerSprite.draw(batch);
	}

	/**
	 * Draws the winning bid next to the player that won it
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawWinningBid() {
		float bidWidth = (Constants.VIRTUAL_WIDTH*0.075f);
		float bidValueX = 0;
		float bidValueY = 0;
		float bidSuitX = 0;
		float bidSuitY = 0;
		switch(game.engine.getWinningBidder()) {
		case 0:
			bidValueX = -bidWidth;
			bidValueY = -Constants.VIRTUAL_HEIGHT/2 + edgePadding + cardHeight;
			bidSuitX = 0;
			bidSuitY = bidValueY;			
			break;
		case 1:
			bidValueX = -Constants.VIRTUAL_WIDTH/2 + edgePadding + cardHeight;
			bidValueY = 0;
			bidSuitX = bidValueX;
			bidSuitY = -bidWidth;
			break;
		case 2:
			bidValueX = -bidWidth;
			bidValueY = Constants.VIRTUAL_HEIGHT/2 - edgePadding - cardHeight - bidWidth;
			bidSuitX = 0;
			bidSuitY = bidValueY;
			break;
		case 3:
			bidValueX = Constants.VIRTUAL_WIDTH/2 - edgePadding - cardHeight - bidWidth;
			bidValueY = 0;
			bidSuitX = bidValueX;
			bidSuitY = -bidWidth;
			break;
		}
		switch(game.engine.getWinningBidderPlayer().getBidValue()) {
		case Constants.BID_TWO:
			bidTwoSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidTwoSprite.draw(batch);
			break;
		case Constants.BID_THREE:
			bidThreeSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidThreeSprite.draw(batch);
			break;
		case Constants.BID_FOUR:
			bidFourSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidFourSprite.draw(batch);
			break;
		case Constants.BID_FIVE:
			bidFiveSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidFiveSprite.draw(batch);
			break;
		case Constants.BID_SIX:
			bidSixSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidSixSprite.draw(batch);
			break;
		case Constants.BID_CHIPPER:
			bidChipperSprite.setBounds(bidValueX, bidValueY, bidWidth, bidWidth);
			bidChipperSprite.draw(batch);
			break;
		}
		
		backgroundSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
		backgroundSprite.draw(batch);
		switch(game.engine.getWinningBidderPlayer().getBidSuit()) {
		case Constants.HEARTS:
			heartSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
			heartSprite.draw(batch);
			break;
		case Constants.DIAMONDS:
			diamondSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
			diamondSprite.draw(batch);
			break;
		case Constants.CLUBS:
			clubSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
			clubSprite.draw(batch);
			break;
		case Constants.SPADES:
			spadeSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
			spadeSprite.draw(batch);
			break;
		case Constants.NO_TRUMP:
			noTrumpSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
			noTrumpSprite.draw(batch);
			break;
		}
	}
	
	/**
	 * Draws the background for the played cards area
	 */
	public void drawPlayedCardsBackground() {
		if(game.engine.getStaying()[0] == ChipperEngine.STAYING_STAY || game.engine.getStaying()[0] == ChipperEngine.STAYING_WAITING) {
			backgroundSprite.setBounds(playedCardPlayer0.x, playedCardPlayer0.y, playedCardPlayer0.width, playedCardPlayer0.height);
			backgroundSprite.draw(batch, 0.2f);
		}
		if(game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY || game.engine.getStaying()[1] == ChipperEngine.STAYING_WAITING) {
			backgroundSprite.setBounds(playedCardPlayer1.x, playedCardPlayer1.y, playedCardPlayer1.width, playedCardPlayer1.height);
			backgroundSprite.draw(batch, 0.2f);
		}
		if(game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY || game.engine.getStaying()[2] == ChipperEngine.STAYING_WAITING) {
			backgroundSprite.setBounds(playedCardPlayer2.x, playedCardPlayer2.y, playedCardPlayer2.width, playedCardPlayer2.height);
			backgroundSprite.draw(batch, 0.2f);
		}
		if(game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY || game.engine.getStaying()[3] == ChipperEngine.STAYING_WAITING) {
			backgroundSprite.setBounds(playedCardPlayer3.x, playedCardPlayer3.y, playedCardPlayer3.width, playedCardPlayer3.height);
			backgroundSprite.draw(batch, 0.2f);
		}
	}	
	
	/**
	 * Sets up cards in the players hand for drawing
	 */
	public void setupUserCards() {
		//Draw player 0 cards (bottom)
		Hand hand = game.engine.getPlayers()[0].getHand();
		playerCards = new Rectangle[hand.getCards().size()];
		float player0HandOriginX = -cardWidth*((float)hand.getCards().size()/2);
		float player0HandOriginY = -(Constants.VIRTUAL_HEIGHT/2) + edgePadding;

		for(int card = 0;card<hand.getCards().size();card++) {
			int[] currentCard = hand.getCards().get(card);

			playerCards[card] = new Rectangle(player0HandOriginX+(cardWidth*card), player0HandOriginY, cardWidth, cardHeight);
			if(card != playedCardIndex) {
				cardSprites[currentCard[0]][currentCard[1]].setBounds(playerCards[card].x, playerCards[card].y, playerCards[card].width, playerCards[card].height);
			}
			//Adjust card display to indicate selected card
			if(game.engine.getSelectedCard() == card && playedCardIndex != card) {
				cardSprites[currentCard[0]][currentCard[1]].setColor(0.4f, 0.4f, 1.0f, 1.0f);
			} else {
				cardSprites[currentCard[0]][currentCard[1]].setColor(Color.WHITE);
			}
			
			cardVisible[currentCard[0]][currentCard[1]] = true;
		}
	}
	
	/**
	 * Draws the scores for players
	 */
	public void drawScores() {
		
	}
	
	/**
	 * Draws the AI cards
	 */
	public void drawAICards() {
		//Player 1
		Hand player1Hand = game.engine.getPlayers()[1].getHand();
		float player1HandX = -(Constants.VIRTUAL_WIDTH/2) + edgePadding;		
//		float player1HandY = -(cardWidth/2) - (cardWidth/4) + ((cardWidth/2)*(player1Hand.getCards().size()/2));
		float player1HandY = ((((float)player1Hand.getCards().size()+1f)/4f)*cardWidth);
		if(game.engine.getStaying()[1] == ChipperEngine.STAYING_OUT) {
			cardBack.setOrigin(0, 0);
			cardBack.setRotation(270);
			cardBack.setBounds(player1HandX, cardWidth/2, cardWidth, cardHeight);
			cardBack.draw(batch);
		} else {
			for(int i = 0;i<player1Hand.getCards().size();i++) {
				if(showCards) {
					int[] card = player1Hand.getCards().get(i);
					cardSprites[card[0]][card[1]].setOrigin(0, 0);
					cardSprites[card[0]][card[1]].setRotation(270);
					cardSprites[card[0]][card[1]].setBounds(player1HandX, player1HandY, cardWidth, cardHeight);				
					cardSprites[card[0]][card[1]].draw(batch);
				} else {
					cardBack.setOrigin(0, 0);
					cardBack.setRotation(270);
					cardBack.setBounds(player1HandX, player1HandY, cardWidth, cardHeight);				
					cardBack.draw(batch);
				}
				player1HandY = player1HandY - (cardWidth/2);
			}
		}
		//Player 2
		Hand player2Hand = game.engine.getPlayers()[2].getHand();
		float player2HandX = -(cardWidth/2) - (cardWidth/4) + ((cardWidth/2)*player2Hand.getCards().size()/2);
		float player2HandY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - cardHeight; 
		if(game.engine.getStaying()[2] == ChipperEngine.STAYING_OUT) {
			cardBack.setOrigin(0, 0);
			cardBack.setRotation(0);
			cardBack.setBounds(-(cardWidth/2), player2HandY, cardWidth, cardHeight);
			cardBack.draw(batch);
		} else {
			for(int i = 0;i<player2Hand.getCards().size();i++) {
				if(showCards) {
					int[] card = player2Hand.getCards().get(i);
					cardSprites[card[0]][card[1]].setOrigin(0, 0);
					cardSprites[card[0]][card[1]].setRotation(0);
					cardSprites[card[0]][card[1]].setBounds(player2HandX, player2HandY, cardWidth, cardHeight);
					cardSprites[card[0]][card[1]].draw(batch);
				} else {
					cardBack.setOrigin(0, 0);
					cardBack.setRotation(0);
					cardBack.setBounds(player2HandX, player2HandY, cardWidth, cardHeight);
					cardBack.draw(batch);
				}
				player2HandX = player2HandX - (cardWidth/2);

			}
		}
		
		//Player 3
		Hand player3Hand = game.engine.getPlayers()[3].getHand();
		float player3HandX = (Constants.VIRTUAL_WIDTH/2) - edgePadding - cardHeight;
		float player3HandY = -((((float)player3Hand.getCards().size()+1f)/4f)*cardWidth)+cardWidth;
		if(game.engine.getStaying()[3] == ChipperEngine.STAYING_OUT) {
			cardBack.setOrigin(0, 0);
			cardBack.setRotation(270);
			cardBack.setBounds(player3HandX, cardWidth/2, cardWidth, cardHeight);
			cardBack.draw(batch);
		} else {
			for(int i = 0;i<player3Hand.getCards().size();i++) {			
				if(showCards) {
					int[] card = player3Hand.getCards().get(i);
					cardSprites[card[0]][card[1]].setOrigin(0, 0);
					cardSprites[card[0]][card[1]].setRotation(270);
					cardSprites[card[0]][card[1]].setBounds(player3HandX, player3HandY, cardWidth, cardHeight);
					cardSprites[card[0]][card[1]].draw(batch);
				} else {
					cardBack.setOrigin(0, 0);
					cardBack.setRotation(270);
					cardBack.setBounds(player3HandX, player3HandY, cardWidth, cardHeight);
					cardBack.draw(batch);
				}
				player3HandY = player3HandY + (cardWidth/2);
			}
		}
	}
	
	/**
	 * Draws the user scores
	 */
	public void drawUserScores() {
		String scoreString = "Score " + game.engine.getPlayers()[0].getScore();
//		float scoreX = -((Constants.VIRTUAL_WIDTH*0.075f)*2) - edgePadding - font.getBounds(scoreString).width;
		float scoreX = -(cardWidth*3);
		float scoreY = -(Constants.VIRTUAL_HEIGHT/2) + cardHeight + edgePadding + ((Constants.VIRTUAL_WIDTH*0.075f)*2) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);
		font.draw(batch, scoreString, scoreX, scoreY);
		
		String tricksString = "Tricks --";
		if(game.engine.getStaying()[0] == ChipperEngine.STAYING_STAY) {
			tricksString = "Tricks " + game.engine.getPlayers()[0].getWonTricks().size();
		}
//		float tricksX = ((Constants.VIRTUAL_WIDTH*0.075f)*2) + edgePadding;
		float tricksX = -(cardWidth*3);
		float tricksY = -(Constants.VIRTUAL_HEIGHT/2) + cardHeight + edgePadding + (Constants.VIRTUAL_WIDTH*0.075f) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);;
		font.draw(batch, tricksString, tricksX, tricksY);
	}
	
	/**
	 * Draws the scores for the AI players
	 */
	public void drawAIScores() {
		//Player 1
		String scoreString = "Score " + game.engine.getPlayers()[1].getScore();
		float scoreX = -(Constants.VIRTUAL_WIDTH/2) + edgePadding;
		float scoreY = (cardWidth*2) + ((Constants.VIRTUAL_WIDTH*0.075f)*2) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);
		font.draw(batch, scoreString, scoreX, scoreY);
		String trickString = "Tricks --";
		if(game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY) {
			trickString = "Tricks " + game.engine.getPlayers()[1].getWonTricks().size();
		}
		float trickX = -(Constants.VIRTUAL_WIDTH/2) + edgePadding;
		float trickY = (cardWidth*2) + (Constants.VIRTUAL_WIDTH*0.075f) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);
		font.draw(batch, trickString, trickX, trickY);
		
		//Player 2
		scoreString = "Score " + game.engine.getPlayers()[2].getScore();
		scoreX = (cardWidth*3) - font.getBounds(scoreString).width;
		scoreY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - cardHeight - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);
		font.draw(batch, scoreString, scoreX, scoreY);
		
		trickString = "Tricks --";
		if(game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY) {
			trickString = "Tricks " + game.engine.getPlayers()[2].getWonTricks().size();
		}
		trickX = (cardWidth*3) - font.getBounds(trickString).width;
		trickY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - cardHeight - (Constants.VIRTUAL_WIDTH*0.075f) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(trickString).height)/2);
		font.draw(batch, trickString, trickX, trickY);
		
		//Player 3
		scoreString = "Score " + game.engine.getPlayers()[3].getScore();
		scoreX = (Constants.VIRTUAL_WIDTH/2) - edgePadding - edgePadding - font.getBounds(scoreString).width;
		scoreY = -(cardWidth) - (cardWidth/2) - (cardWidth/4) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(scoreString).height)/2);
		font.draw(batch, scoreString, scoreX, scoreY);
		trickString = "Tricks --";
		if(game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY) {
			trickString = "Tricks " + game.engine.getPlayers()[3].getWonTricks().size();
		}
		trickX = (Constants.VIRTUAL_WIDTH/2) - edgePadding - edgePadding - font.getBounds(trickString).width;
		trickY = -(cardWidth) - (cardWidth/2) - (cardWidth/4) - (Constants.VIRTUAL_WIDTH*0.075f) - (((Constants.VIRTUAL_WIDTH*0.075f) - font.getBounds(trickString).height)/2);
		font.draw(batch, trickString, trickX, trickY);
	}
	
	/**
	 * Draws the arrow that indicates the current player
	 */
	private void drawCurrentPlayerArrow() {
		float currentPlayerX = 0;			
		float currentPlayerY = 0;
		float spriteWidth = (Constants.VIRTUAL_WIDTH*0.075f);
		arrowSprite.setOrigin(spriteWidth/2, spriteWidth/2);
		switch(game.engine.getCurrentPlayer()) {
		case 0:
			arrowSprite.setRotation(180);
			currentPlayerX = -spriteWidth/2;
			currentPlayerY = -cardHeight/2 - cardHeight;
			break;
		case 1:
			arrowSprite.setRotation(90);
			currentPlayerX = -cardWidth;
			currentPlayerY = -spriteWidth/2;
			break;
		case 2:
			arrowSprite.setRotation(0);
			currentPlayerX = -spriteWidth/2;
			currentPlayerY = cardHeight/2 + cardHeight - spriteWidth;
			break;
		case 3:			
			arrowSprite.setRotation(270);
			currentPlayerX = cardWidth - spriteWidth;
			currentPlayerY = -spriteWidth/2;
			break;
		}
		arrowSprite.setBounds(currentPlayerX, currentPlayerY, spriteWidth, spriteWidth);
		arrowSprite.draw(batch);
	}
	
	/**
	 * Draws the arrow indicating lead player
	 */
	private void drawLeadArrow() {
		float spriteWidth = (Constants.VIRTUAL_WIDTH*0.1f);
		leadSprite.setOrigin(leadSprite.getWidth()/2, leadSprite.getHeight()/2);
		switch(game.engine.getPlayLead()) {
		case 0:		
			leadSprite.setRotation(180);
			leadSprite.setBounds(-spriteWidth/2, -cardHeight -(cardHeight/2)-spriteWidth, spriteWidth, spriteWidth);
			break;
		case 1:
			leadSprite.setRotation(90);
			leadSprite.setBounds(-cardWidth - spriteWidth, -spriteWidth/2, spriteWidth, spriteWidth);
			break;
		case 2:
			leadSprite.setRotation(0);
			leadSprite.setBounds(-spriteWidth/2, cardHeight + (cardHeight/2), spriteWidth, spriteWidth);
			break;
		case 3:
			leadSprite.setRotation(270);
			leadSprite.setBounds(cardWidth, -spriteWidth/2, spriteWidth, spriteWidth);
			break;
		}
		leadSprite.draw(batch);
	}
	/**
	 * Draws the bid number state
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawBidNumberState() {
		float bidWidth = Constants.VIRTUAL_WIDTH*0.6f;
		float bidHeight = Constants.VIRTUAL_HEIGHT*0.7f;
		
		if(game.engine.getCurrentBidder() == 0) {
			int highBid = Constants.BID_PASS;
			for(int i = 1;i<4;i++) {
				if(game.engine.getPlayers()[i].getBidValue() > highBid) {
					highBid = game.engine.getPlayers()[i].getBidValue();
				}
			}

			background.draw(batch, -bidWidth/2, -bidHeight/2, bidWidth, bidHeight);

			font.drawMultiLine(batch,"How many do\nyou bid?", -bidWidth/2, (bidHeight/2)-8, bidWidth, HAlignment.CENTER);

			float fontY = 0;
			if(highBid < Constants.BID_CHIPPER) {
				if(bidChipperPressed) {
					antibackground.draw(batch, bidChipper.x, bidChipper.y, bidChipper.width, bidChipper.height);
				} else {
					background.draw(batch, bidChipper.x, bidChipper.y, bidChipper.width, bidChipper.height);
				}
				fontY = (bidChipper.y+bidChipper.height-5)-((bidChipper.height-10-font.getBounds("Chipper").height)/2);
				font.drawMultiLine(batch, "Chipper", bidChipper.x, fontY, bidChipper.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_SIX) {
				if(bidSixPressed) {
					antibackground.draw(batch, bidSix.x, bidSix.y, bidSix.width, bidSix.height);
				} else {
					background.draw(batch, bidSix.x, bidSix.y, bidSix.width, bidSix.height);
				}
				fontY = (bidSix.y+bidSix.height-5)-((bidSix.height-10-font.getBounds("Six").height)/2);
				font.drawMultiLine(batch, "Six", bidSix.x, fontY, bidSix.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_FIVE) {
				if(bidFivePressed) {
					antibackground.draw(batch, bidFive.x, bidFive.y, bidFive.width, bidFive.height);
				} else {
					background.draw(batch, bidFive.x, bidFive.y, bidFive.width, bidFive.height);
				}
				fontY = (bidFive.y+bidFive.height-5)-((bidFive.height-10-font.getBounds("Five").height)/2);
				font.drawMultiLine(batch, "Five", bidFive.x, fontY, bidFive.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_FOUR) {
				if(bidFourPressed) {
					antibackground.draw(batch, bidFour.x, bidFour.y, bidFour.width, bidFour.height);
				} else {
					background.draw(batch, bidFour.x, bidFour.y, bidFour.width, bidFour.height);
				}
				fontY = (bidFour.y+bidFour.height-5)-((bidFour.height-10-font.getBounds("Four").height)/2);
				font.drawMultiLine(batch, "Four", bidFour.x, fontY, bidFour.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_THREE) {
				if(bidThreePressed) {
					antibackground.draw(batch, bidThree.x, bidThree.y, bidThree.width, bidThree.height);
				} else {
					background.draw(batch, bidThree.x, bidThree.y, bidThree.width, bidThree.height);
				}
				fontY = (bidThree.y+bidThree.height-5)-((bidThree.height-10-font.getBounds("Three").height)/2);
				font.drawMultiLine(batch, "Three", bidThree.x, fontY, bidThree.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_TWO) {
				if(bidTwoPressed) {
					antibackground.draw(batch, bidTwo.x, bidTwo.y, bidTwo.width, bidTwo.height);
				} else {
					background.draw(batch, bidTwo.x, bidTwo.y, bidTwo.width, bidTwo.height);
				}
				fontY = (bidTwo.y+bidTwo.height-5)-((bidTwo.height-10-font.getBounds("Two").height)/2);
				font.drawMultiLine(batch, "Two", bidTwo.x, fontY, bidTwo.width, HAlignment.CENTER);
			}
			
			if(bidPassPressed) {
				antibackground.draw(batch, bidPass.x, bidPass.y, bidPass.width, bidPass.height);
			} else {
				background.draw(batch, bidPass.x, bidPass.y, bidPass.width, bidPass.height);
			}
			fontY = (bidPass.y+bidPass.height-5)-((bidPass.height-10-font.getBounds("Pass").height)/2);
			font.drawMultiLine(batch, "Pass", bidPass.x, fontY, bidPass.width, HAlignment.CENTER);
			
			//Draw bid help button
			if(bidHelpPressed) {
				antibackground.draw(batch, bidHelp.x, bidHelp.y, bidHelp.width, bidHelp.height);
			} else {
				background.draw(batch, bidHelp.x, bidHelp.y, bidHelp.width, bidHelp.height);
			}
			float bidHelpY = bidHelp.y + (bidHelp.height/2) + font.getBounds("HEIGHT").height + (edgePadding/2);
			font.drawMultiLine(batch, "Bid", bidHelp.x, bidHelpY, bidHelp.width, HAlignment.CENTER );
			font.drawMultiLine(batch, "Help", bidHelp.x, bidHelpY - font.getBounds("HEIGHT").height-edgePadding, bidHelp.width, HAlignment.CENTER );
			
		} else {
			float biddingHeight = Constants.VIRTUAL_HEIGHT*0.2f;
			background.draw(batch, -bidWidth/2, -biddingHeight/2, bidWidth, biddingHeight);
			float fontY = ((biddingHeight/2)-5) - ((biddingHeight-10-font.getBounds("Bidding").height)/2);
			font.drawMultiLine(batch, "Bidding", -bidWidth/2, fontY, bidWidth, HAlignment.CENTER);
		}
		if(game.engine.getPlayers()[0].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[0].getBidValue(), -cardWidth/2, -Constants.VIRTUAL_HEIGHT/2 + edgePadding);
//				Gdx.app.log("DRAWBID", "Player 1");
		}		
		if(game.engine.getPlayers()[1].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[1].getBidValue(), -Constants.VIRTUAL_WIDTH/2 + edgePadding, -cardHeight/2);
//				Gdx.app.log("DRAWBID", "Player 2");
		}
		if(game.engine.getPlayers()[2].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[2].getBidValue(), -cardWidth/2, Constants.VIRTUAL_HEIGHT/2 - edgePadding - cardHeight);
//				Gdx.app.log("DRAWBID", "Player 3");
		}
		if(game.engine.getPlayers()[3].getBidValue() != -1) {					
			drawBidSprite(game.engine.getPlayers()[3].getBidValue(), Constants.VIRTUAL_WIDTH/2 - edgePadding - cardWidth, -cardHeight/2);
//				Gdx.app.log("DRAWBID", "Player 4");
		}
	}
	
	/**
	 * Draws the bid sprite with the passed bounds
	 * @param bidValue
	 * @param cardWidth
	 * @param cardHeight
	 * @param x
	 * @param y
	 */
	private void drawBidSprite(int bidValue, float x, float y) {
		switch(bidValue) {
		case Constants.BID_PASS:
			bidPassSprite.setBounds(x, y, cardWidth, cardHeight);
			bidPassSprite.draw(batch);
			break;
		case Constants.BID_TWO:
			bidTwoSprite.setBounds(x, y, cardWidth, cardHeight);
			bidTwoSprite.draw(batch);
			break;
		case Constants.BID_THREE:
			bidThreeSprite.setBounds(x, y, cardWidth, cardHeight);
			bidThreeSprite.draw(batch);
			break;
		case Constants.BID_FOUR:
			bidFourSprite.setBounds(x, y, cardWidth, cardHeight);
			bidFourSprite.draw(batch);
			break;
		case Constants.BID_FIVE:
			bidFiveSprite.setBounds(x, y, cardWidth, cardHeight);
			bidFiveSprite.draw(batch);
			break;
		case Constants.BID_SIX:
			bidSixSprite.setBounds(x, y, cardWidth, cardHeight);
			bidSixSprite.draw(batch);
			break;
		case Constants.BID_CHIPPER:
			bidChipperSprite.setBounds(x, y, cardWidth, cardHeight);
			bidChipperSprite.draw(batch);
			break;
		}
		
	}
	
	/**
	 * Draws the bid won state
	 */
	private void drawBidWonState() {
		float bidWidth = Constants.VIRTUAL_WIDTH*0.6f;
		float bidHeight = Constants.VIRTUAL_HEIGHT*0.5f;
		
		background.draw(batch, -bidWidth/2, -bidHeight/2, bidWidth, bidHeight);
		
		while(font.getBounds("In what suit?").width > bidWidth) {
			font.scale(-0.1f);
		}
		font.drawMultiLine(batch,"In what suit?", -bidWidth/2, (bidHeight/2)-8, bidWidth, HAlignment.CENTER);
		
		if(bidHeartsPressed) {
			antibackground.draw(batch, bidHearts.x, bidHearts.y, bidHearts.width, bidHearts.height);
		}
		heartSprite.setBounds(bidHearts.x, bidHearts.y, bidHearts.width, bidHearts.height);
		heartSprite.draw(batch);

		if(bidDiamondsPressed) {
			antibackground.draw(batch, bidDiamonds.x, bidDiamonds.y, bidDiamonds.width, bidDiamonds.height);
		}
		diamondSprite.setBounds(bidDiamonds.x, bidDiamonds.y, bidDiamonds.width, bidDiamonds.height);
		diamondSprite.draw(batch);

		if(bidSpadesPressed) {
			antibackground.draw(batch, bidSpades.x, bidSpades.y, bidSpades.width, bidSpades.height);
		}
		spadeSprite.setBounds(bidSpades.x, bidSpades.y, bidSpades.width, bidSpades.height);
		spadeSprite.draw(batch);

		if(bidClubsPressed) {
			antibackground.draw(batch, bidClubs.x, bidClubs.y, bidClubs.width, bidClubs.height);
		}
		clubSprite.setBounds(bidClubs.x, bidClubs.y, bidClubs.width, bidClubs.height);
		clubSprite.draw(batch);
		
		if(game.engine.getPlayers()[0].getBidValue() > 2) { //Need to bid at least 3 to get no trump
			if(bidNoTrumpPressed) {
				antibackground.draw(batch, bidNoTrump.x, bidNoTrump.y, bidNoTrump.width, bidNoTrump.height);
			} else {
				background.draw(batch, bidNoTrump.x, bidNoTrump.y, bidNoTrump.width, bidNoTrump.height);
			}
			float fontY = (bidNoTrump.y+bidNoTrump.height) - ((bidNoTrump.height-font.getBounds("No Trump").height)/2);
			font.drawMultiLine(batch, "No Trump", bidNoTrump.x, fontY, bidNoTrump.width, HAlignment.CENTER);
		}
		if(game.engine.getPlayers()[0].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[0].getBidValue(), -cardWidth/2, -Constants.VIRTUAL_HEIGHT/2 + edgePadding);
//				Gdx.app.log("DRAWBID", "Player 1");
		}
		
		//Draw bid help button
		if(bidHelpPressed) {
			antibackground.draw(batch, bidHelp.x, bidHelp.y, bidHelp.width, bidHelp.height);
		} else {
			background.draw(batch, bidHelp.x, bidHelp.y, bidHelp.width, bidHelp.height);
		}
		float bidHelpY = bidHelp.y + (bidHelp.height/2) + font.getBounds("HEIGHT").height + (edgePadding/2);
		font.drawMultiLine(batch, "Bid", bidHelp.x, bidHelpY, bidHelp.width, HAlignment.CENTER );
		font.drawMultiLine(batch, "Help", bidHelp.x, bidHelpY - font.getBounds("HEIGHT").height-edgePadding, bidHelp.width, HAlignment.CENTER );
	}
	
	/**
	 * Draw the stay play state
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawBidStayState() {
		if(game.engine.getWinningBidder() != 0) {
			
			//Draw winning bidder's suit and value
			if(game.engine.getWinningBidder() == 0) {
				drawBidSprite(game.engine.getPlayers()[0].getBidValue(), -cardWidth, -Constants.VIRTUAL_HEIGHT/2 + edgePadding + (cardHeight/2));
				drawBidSuit(game.engine.getPlayers()[0].getBidSuit(), 0, -Constants.VIRTUAL_HEIGHT/2 + edgePadding + (cardHeight/2));
			}
			if(game.engine.getWinningBidder() == 1) {
				drawBidSprite(game.engine.getPlayers()[1].getBidValue(), -Constants.VIRTUAL_WIDTH/2 + edgePadding + (cardWidth/2), 0);
				drawBidSuit(game.engine.getPlayers()[1].getBidSuit(), -Constants.VIRTUAL_WIDTH/2 + edgePadding + (cardWidth/2), -cardHeight);
			}
			if(game.engine.getWinningBidder() == 2) {
				drawBidSprite(game.engine.getPlayers()[2].getBidValue(), -cardWidth, Constants.VIRTUAL_HEIGHT/2 - edgePadding - cardHeight - (cardHeight/2));
				drawBidSuit(game.engine.getPlayers()[2].getBidSuit(), 0, Constants.VIRTUAL_HEIGHT/2 - edgePadding - cardHeight - (cardHeight/2));
			}
			if(game.engine.getWinningBidder() == 3) {					
				drawBidSprite(game.engine.getPlayers()[3].getBidValue(), Constants.VIRTUAL_WIDTH/2 - edgePadding - cardWidth - (cardWidth/2), 0);
				drawBidSuit(game.engine.getPlayers()[3].getBidSuit(), Constants.VIRTUAL_WIDTH/2 - edgePadding - cardWidth - (cardWidth/2), -cardHeight);
			}
			
			//Draw whether players are staying or not
			float stayWidth = cardWidth;
			float stayHeight = cardHeight;
			if(game.engine.getWinningBidder() != 1 && game.engine.getStaying()[1] != ChipperEngine.STAYING_WAITING) {
				float stayX = -Constants.VIRTUAL_WIDTH/2 + edgePadding;
				backgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				backgroundSprite.draw(batch);
//					background.draw(batch, player2HandOriginX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 2 && game.engine.getStaying()[2] != ChipperEngine.STAYING_WAITING) {
				backgroundSprite.setBounds(-stayWidth/2, Constants.VIRTUAL_HEIGHT/2 - edgePadding - stayHeight, stayWidth, stayHeight);
				backgroundSprite.draw(batch);
//					background.draw(batch, -stayWidth/2, Constants.VIRTUAL_HEIGHT/2 - edgePadding, stayWidth, stayHeight);
				float fontY = ((Constants.VIRTUAL_HEIGHT/2) - edgePadding - 5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, -stayWidth/2, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 3 && game.engine.getStaying()[3] != ChipperEngine.STAYING_WAITING) {
				float stayX = Constants.VIRTUAL_WIDTH/2 - edgePadding - stayWidth;
				backgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				backgroundSprite.draw(batch);					
//					background.draw(batch, stayX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			
			if(game.engine.getCurrentStayer() == 0) {
				if(stayPressed) {
					antibackground.draw(batch, stay.x, stay.y, stay.width, stay.height);
				} else {
					background.draw(batch, stay.x, stay.y, stay.width, stay.height);
				}
				float fontY = (stay.y + stay.height-5) - ((stay.height-10-font.getBounds("Stay").height)/2);
				font.drawMultiLine(batch, "Stay", stay.x, fontY, stay.width, HAlignment.CENTER);

				if(stayPassPressed) {
					antibackground.draw(batch, stayPass.x, stayPass.y, stayPass.width, stayPass.height);
				} else {
					background.draw(batch, stayPass.x, stayPass.y, stayPass.width, stayPass.height);
				}
				fontY = (stayPass.y + stayPass.height-5) - ((stayPass.height-10-font.getBounds("Pass").height)/2);
				font.drawWrapped(batch, "Pass", stayPass.x, fontY, stayPass.width, HAlignment.CENTER);
			} else {
				float stayingWidth = Constants.VIRTUAL_WIDTH*0.4f;
				float stayingHeight = Constants.VIRTUAL_HEIGHT*0.1f;
				background.draw(batch, -stayingWidth/2, -stayingHeight/2, stayingWidth, stayingHeight);
				float fontY = ((stayingHeight/2)-5) - ((stayingHeight-10-font.getBounds("Bidding").height)/2);
				font.drawMultiLine(batch, "Staying", -stayingWidth/2, fontY, stayingWidth, HAlignment.CENTER);
			}
		} else {
			//Draw whether players are staying or not
			float stayWidth = cardWidth;
			float stayHeight = cardHeight;
			if(game.engine.getWinningBidder() != 1 && game.engine.getStaying()[1] != ChipperEngine.STAYING_WAITING) {
				float stayX = -Constants.VIRTUAL_WIDTH/2 + edgePadding;
				backgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				backgroundSprite.draw(batch);
//					background.draw(batch, player2HandOriginX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 2 && game.engine.getStaying()[2] != ChipperEngine.STAYING_WAITING) {
				backgroundSprite.setBounds(-stayWidth/2, Constants.VIRTUAL_HEIGHT/2 - edgePadding - stayHeight, stayWidth, stayHeight);
				backgroundSprite.draw(batch);
//					background.draw(batch, -stayWidth/2, Constants.VIRTUAL_HEIGHT/2 - edgePadding, stayWidth, stayHeight);
				float fontY = ((Constants.VIRTUAL_HEIGHT/2) - edgePadding - 5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, -stayWidth/2, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 3 && game.engine.getStaying()[3] != ChipperEngine.STAYING_WAITING) {
				float stayX = Constants.VIRTUAL_WIDTH/2 - edgePadding - stayWidth;
				backgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				backgroundSprite.draw(batch);					
//					background.draw(batch, stayX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			
			float stayingWidth = Constants.VIRTUAL_WIDTH*0.6f;
			float stayingHeight = Constants.VIRTUAL_HEIGHT*0.2f;
			background.draw(batch, -stayingWidth/2, -stayingHeight/2, stayingWidth, stayingHeight);
			float fontY = ((stayingHeight/2)-5) - ((stayingHeight-10-font.getBounds("Bidding").height)/2);
			font.drawMultiLine(batch, "Staying", -stayingWidth/2, fontY, stayingWidth, HAlignment.CENTER);
		}
	}
	
	/**
	 * Draws the bid suit
	 * @param bidSuit
	 * @param x
	 * @param y
	 */
	public void drawBidSuit(int bidSuit, float x, float y) {
		backgroundSprite.setBounds(x, y, cardWidth, cardHeight);
		backgroundSprite.draw(batch);
		switch(bidSuit) {
		case Constants.HEARTS:
			heartSprite.setBounds(x, y, cardWidth, cardHeight);
			heartSprite.draw(batch);
			break;
		case Constants.DIAMONDS:
			diamondSprite.setBounds(x, y, cardWidth, cardHeight);
			diamondSprite.draw(batch);
			break;
		case Constants.CLUBS:
			clubSprite.setBounds(x, y, cardWidth, cardHeight);
			clubSprite.draw(batch);
			break;
		case Constants.SPADES:
			spadeSprite.setBounds(x, y, cardWidth, cardHeight);
			spadeSprite.draw(batch);
			break;
		case Constants.NO_TRUMP:
			noTrumpSprite.setBounds(x, y, cardWidth, cardHeight);
			noTrumpSprite.draw(batch);
			break;
		}
	}
	
	private void drawHandOverResults() {
		float overWidth = Constants.VIRTUAL_WIDTH*0.6f;
		float overHeight = Constants.VIRTUAL_HEIGHT*0.4f;
		float textHeight = font.getBounds("HEIGHT").height;
		
		background.draw(batch, -overWidth/2, -overHeight/2, overWidth, overHeight);
		float textY = (overHeight/2) - edgePadding;
		font.drawMultiLine(batch, "Hand Results", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "User", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p0score = getResultScoreString(0);
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p0score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 1", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p1score = getResultScoreString(1);
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p1score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 2", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p2score = getResultScoreString(2);
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p2score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		font.setColor(Color.DARK_GRAY);
		font.drawMultiLine(batch, "Player 3", -overWidth/2, textY, overWidth, HAlignment.CENTER);
		textY = textY - edgePadding;
		textY = textY - textHeight;
		String p3score = getResultScoreString(3);
		font.setColor(Color.WHITE);
		font.drawMultiLine(batch, p3score, -overWidth/2, textY, overWidth, HAlignment.CENTER);
		
		textY = textY - edgePadding;
		textY = textY - textHeight;
		
		if(nextHandButton == null) {
			float nextHandButtonWidth = Constants.VIRTUAL_WIDTH*0.4f;
			float nextHandButtonHeight = Constants.VIRTUAL_HEIGHT*0.08f;
			nextHandButton = new Rectangle(-nextHandButtonWidth/2, textY - nextHandButtonHeight, nextHandButtonWidth, nextHandButtonHeight);
		}
		if(nextHandButtonPressed) {
			antibackground.draw(batch,nextHandButton.x, nextHandButton.y, nextHandButton.width, nextHandButton.height);
		} else {
			background.draw(batch,nextHandButton.x, nextHandButton.y, nextHandButton.width, nextHandButton.height);
		}
		
		textY = (nextHandButton.y + nextHandButton.height) - ((nextHandButton.height - (font.getBounds("Continue").height))/2);
		font.drawMultiLine(batch, "Continue", nextHandButton.x, textY, nextHandButton.width, HAlignment.CENTER);
	}
	
	/**
	 * Returns a string of the players score for post hand scoring
	 * @param player
	 * @return
	 */
	private String getResultScoreString(int player) {
		StringBuilder score = new StringBuilder();
		if(game.engine.getStaying()[player] == ChipperEngine.STAYING_STAY) {
			score.append(game.engine.getPlayers()[player].getHistoryScoreToPoint(game.engine.getPlayers()[player].getScoreHistory().size()-1));
			score.append("->");
		}
		score.append(game.engine.getPlayers()[player].getFullHistoryScore());
		score.append(" (");
		if(game.engine.getStaying()[player] == ChipperEngine.STAYING_STAY) {
			int wonTricks = game.engine.getPlayers()[player].getWonTricks().size();
			if(game.engine.getWinningBidder() == player) {
				if(game.engine.getWinningBidderPlayer().getBidValue() == Constants.BID_CHIPPER) {
					if(wonTricks == 6) {
						score.append("Chipper Won");
					} else {
						score.append("Chipper Fail");
					}						
				} else if(wonTricks >= game.engine.getWinningBidderPlayer().getBidValue()) {
					score.append("+" + wonTricks);
				} else {
					score.append("Set");
				}
			} else {
				if(wonTricks > 0) {
					score.append("+" + wonTricks);
				} else {
					score.append("Set");
				}
			}
		} else {
			score.append("Passed");
		}
		score.append(")");
		return score.toString();
	}

	public void setFollowSuit(boolean followSuit) {
		this.followSuit = followSuit;
	}

	public boolean isFollowSuit() {
		return followSuit;
	}
}
