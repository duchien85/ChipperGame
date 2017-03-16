package com.kyrutech.chipper.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.Vector3;
import com.kyrutech.chipper.ChipperEngine;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;
import com.kyrutech.chipper.gameobjects.Hand;
import com.kyrutech.chipper.gameobjects.Trick;

public class GameScreen implements Screen, InputProcessor {

	ChipperGame game;
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture cardsheet, tilesheet;
	private Sprite cardSprites[][] = new Sprite[4][13];
	private Sprite cardBack;
	private Sprite arrowSprite, dealerSprite, leadSprite, heartSprite, diamondSprite, clubSprite, spadeSprite, noTrumpSprite;
	private Sprite bidPassSprite, bidTwoSprite, bidThreeSprite, bidFourSprite, bidFiveSprite, bidSixSprite, bidChipperSprite, bidBackgroundSprite;
	private NinePatch background;
	
	private BitmapFont font;
	
	private Rectangle bidChipper, bidSix, bidFive, bidFour, bidThree, bidTwo, bidPass;
	private Rectangle bidHearts, bidDiamonds, bidSpades, bidClubs, bidNoTrump;
	private Rectangle stay, stayPass;
	private Rectangle playerCards[];
	private Rectangle playerPlayedCard = null;
	private Rectangle nextHandButton = null;
	
	private TweenManager manager;
	
	float edgePadding, cardWidth, cardHeight;
	
	//For debugging
	private boolean showCards = true;
	
	public GameScreen(ChipperGame game) {
		this.game = game;
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if(playerPlayedCard == null) {
			playerPlayedCard = new Rectangle(-cardWidth/2, -cardHeight/2 - cardHeight, cardWidth, cardHeight);
		}
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		drawAllPlayerCards(edgePadding, cardWidth, cardHeight);

		drawDealerIndicator(edgePadding, cardWidth, cardHeight);
		
		drawPlayerScores(edgePadding, cardWidth, cardHeight);
		
		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_NUMBER) {
			drawBidNumberState(edgePadding, cardWidth, cardHeight);
			
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {
			drawBidWonState(edgePadding, cardWidth, cardHeight);
			
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_STAY) {
			drawBidStayState(edgePadding, cardWidth, cardHeight);
			
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			drawWinningBid(edgePadding, cardWidth, cardHeight);

			float spriteWidth = (Gdx.graphics.getWidth()*0.075f);
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
			
			drawPlayedCards(cardWidth, cardHeight);
			
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_OVER) {
			drawWinningBid(edgePadding, cardWidth, cardHeight);
			
			drawHandOverResults(edgePadding);
			
		}

		batch.end();
		
		manager.update(delta);
		game.engine.update(delta);
		
	}

	private void drawHandOverResults(float edgePadding) {
		float overWidth = Gdx.graphics.getWidth()*0.6f;
		float overHeight = Gdx.graphics.getHeight()*0.4f;
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
			float nextHandButtonWidth = Gdx.graphics.getWidth()*0.4f;
			float nextHandButtonHeight = Gdx.graphics.getHeight()*0.07f;
			nextHandButton = new Rectangle(-nextHandButtonWidth/2, textY - nextHandButtonHeight, nextHandButtonWidth, nextHandButtonHeight);
		}
		background.draw(batch,nextHandButton.x, nextHandButton.y, nextHandButton.width, nextHandButton.height);
		
		textY = (nextHandButton.y + nextHandButton.height) - ((nextHandButton.height - (font.getBounds("Continue").height))/2);
		font.drawMultiLine(batch, "Continue", nextHandButton.x, textY, nextHandButton.width, HAlignment.CENTER);
	}

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

	private void drawPlayerScores(float edgePadding, float cardWidth,
			float cardHeight) {
		//Draw scores
		//Player 0
		StringBuilder p0Score = new StringBuilder();
		p0Score.append("S ");
		p0Score.append(game.engine.getPlayers()[0].getScore());
		p0Score.append(" T ");
		if(game.engine.getStaying()[0] == ChipperEngine.STAYING_STAY) {
			p0Score.append(game.engine.getPlayers()[0].getWonTricks().size());
		} else {
			p0Score.append("-");
		}
		float player0ScoreX = -(Gdx.graphics.getWidth()*0.075f)-font.getBounds(p0Score.toString()).width;
		float player0ScoreY = -Gdx.graphics.getHeight()/2 + edgePadding + cardHeight + font.getBounds(p0Score.toString()).height;		
		font.draw(batch, p0Score, player0ScoreX, player0ScoreY);
		//Player 1		
		StringBuilder p1Score = new StringBuilder();
		p1Score.append("S\n");
		p1Score.append(game.engine.getPlayers()[1].getScore() + "\n");
		p1Score.append("T\n");
		if(game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY) {
			p1Score.append(game.engine.getPlayers()[1].getWonTricks().size());
		} else {
			p1Score.append("-");
		}
		float player1ScoreX = -Gdx.graphics.getWidth()/2 + edgePadding + cardWidth;
		float player1ScoreY = -(Gdx.graphics.getWidth()*0.075f) - edgePadding;
		font.drawMultiLine(batch, p1Score, player1ScoreX, player1ScoreY, Gdx.graphics.getWidth()*0.075f, HAlignment.CENTER);
		//Player 2
		StringBuilder p2Score = new StringBuilder();
		p2Score.append("S ");
		p2Score.append(game.engine.getPlayers()[2].getScore());
		p2Score.append(" T ");
		if(game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY) {
			p2Score.append(game.engine.getPlayers()[2].getWonTricks().size());
		} else {
			p2Score.append("-");
		}
		float player2ScoreX = Gdx.graphics.getWidth()*0.075f;
		float player2ScoreY = Gdx.graphics.getHeight()/2 - edgePadding - cardHeight;		
		font.draw(batch, p2Score, player2ScoreX, player2ScoreY);
		//Player 3
		StringBuilder p3Score = new StringBuilder();
		p3Score.append("S\n");
		p3Score.append(game.engine.getPlayers()[3].getScore() + "\n");
		p3Score.append("T\n");
		if(game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY) {
			p3Score.append(game.engine.getPlayers()[3].getWonTricks().size());
		} else {
			p3Score.append("-");
		}
		float player3ScoreX = Gdx.graphics.getWidth()/2 - edgePadding - cardWidth - (Gdx.graphics.getWidth()*0.075f);
		float player3ScoreY = -(Gdx.graphics.getWidth()*0.075f) - edgePadding;
		font.drawMultiLine(batch, p3Score, player3ScoreX, player3ScoreY, Gdx.graphics.getWidth()*0.075f, HAlignment.CENTER);

	}

	/**
	 * Draws the played cards and the location played cards go
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawPlayedCards(float cardWidth, float cardHeight) {
		float player0CardX = -cardWidth/2;
		float player0CardY = -cardHeight/2 - cardHeight;
		float player1CardX = -cardWidth;
		float player1CardY = -cardHeight/2;
		float player2CardX = -cardWidth/2;
		float player2CardY = cardHeight/2;
		float player3CardX = 0;
		float player3CardY = -cardHeight/2;
		
		if(game.engine.getStaying()[0] == ChipperEngine.STAYING_STAY) {
			bidBackgroundSprite.setBounds(player0CardX, player0CardY, cardWidth, cardHeight);
			bidBackgroundSprite.draw(batch);
		}
		if(game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY) {
			bidBackgroundSprite.setBounds(player1CardX, player1CardY, cardWidth, cardHeight);
			bidBackgroundSprite.draw(batch);
		}
		if(game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY) {
			bidBackgroundSprite.setBounds(player2CardX, player2CardY, cardWidth, cardHeight);
			bidBackgroundSprite.draw(batch);
		}
		if(game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY) {
			bidBackgroundSprite.setBounds(player3CardX, player3CardY, cardWidth, cardHeight);
			bidBackgroundSprite.draw(batch);
		}
		
		//Need to draw this after the background but before the actual cards
		drawCurrentPlayerArrow(cardWidth, cardHeight);
		
		Trick trick = game.engine.getCurrentTrick();
		if(trick != null) {
			if(trick.getPlayerCard(0)[0] != -1) {
				int[] card = trick.getPlayerCard(0);	
				cardSprites[card[0]][card[1]].setColor(Color.WHITE);  //Needs to remove selected tint
//				cardSprites[card[0]][card[1]].setBounds(player0CardX, player0CardY, cardWidth, cardHeight);
				cardSprites[card[0]][card[1]].draw(batch);
			}
			if(trick.getPlayerCard(1)[0] != -1) {
				int[] card = trick.getPlayerCard(1);				
				cardSprites[card[0]][card[1]].setBounds(player1CardX, player1CardY, cardWidth, cardHeight);
				cardSprites[card[0]][card[1]].draw(batch);
			}
			if(trick.getPlayerCard(2)[0] != -1) {
				int[] card = trick.getPlayerCard(2);				
				cardSprites[card[0]][card[1]].setBounds(player2CardX, player2CardY, cardWidth, cardHeight);
				cardSprites[card[0]][card[1]].draw(batch);
			}
			if(trick.getPlayerCard(3)[0] != -1) {
				int[] card = trick.getPlayerCard(3);				
				cardSprites[card[0]][card[1]].setBounds(player3CardX, player3CardY, cardWidth, cardHeight);
				cardSprites[card[0]][card[1]].draw(batch);
			}
		}
	}

	/**
	 * Draws the arrow that indicates the current player
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawCurrentPlayerArrow(float cardWidth, float cardHeight) {
		float currentPlayerX = 0;			
		float currentPlayerY = 0;
		float spriteWidth = (Gdx.graphics.getWidth()*0.075f);
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
	 * Draws the winning bid next to the player that won it
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawWinningBid(float edgePadding, float cardWidth,
			float cardHeight) {
		float bidWidth = (Gdx.graphics.getWidth()*0.075f);
		float bidValueX = 0;
		float bidValueY = 0;
		float bidSuitX = 0;
		float bidSuitY = 0;
		switch(game.engine.getWinningBidder()) {
		case 0:
			bidValueX = -bidWidth;
			bidValueY = -Gdx.graphics.getHeight()/2 + edgePadding + cardHeight;
			bidSuitX = 0;
			bidSuitY = bidValueY;			
			break;
		case 1:
			bidValueX = -Gdx.graphics.getWidth()/2 + edgePadding + cardWidth;
			bidValueY = 0;
			bidSuitX = bidValueX;
			bidSuitY = -bidWidth;
			break;
		case 2:
			bidValueX = -bidWidth;
			bidValueY = Gdx.graphics.getHeight()/2 - edgePadding - cardHeight - bidWidth;
			bidSuitX = 0;
			bidSuitY = bidValueY;
			break;
		case 3:
			bidValueX = Gdx.graphics.getWidth()/2 - edgePadding - cardWidth - bidWidth;
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
		
		bidBackgroundSprite.setBounds(bidSuitX, bidSuitY, bidWidth, bidWidth);
		bidBackgroundSprite.draw(batch);
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
	 * Draw the stay play state
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawBidStayState(float edgePadding, float cardWidth,
			float cardHeight) {
		if(game.engine.getWinningBidder() != 0) {
			
			//Draw winning bidder's suit and value
			if(game.engine.getWinningBidder() == 0) {
				drawBidSprite(game.engine.getPlayers()[0].getBidValue(), cardWidth, cardHeight, -cardWidth, -Gdx.graphics.getHeight()/2 + edgePadding + (cardHeight/2));
				drawBidSuit(game.engine.getPlayers()[0].getBidSuit(), cardWidth, cardHeight, 0, -Gdx.graphics.getHeight()/2 + edgePadding + (cardHeight/2));
			}
			if(game.engine.getWinningBidder() == 1) {
				drawBidSprite(game.engine.getPlayers()[1].getBidValue(), cardWidth, cardHeight, -Gdx.graphics.getWidth()/2 + edgePadding + (cardWidth/2), 0);
				drawBidSuit(game.engine.getPlayers()[1].getBidSuit(), cardWidth, cardHeight, -Gdx.graphics.getWidth()/2 + edgePadding + (cardWidth/2), -cardHeight);
			}
			if(game.engine.getWinningBidder() == 2) {
				drawBidSprite(game.engine.getPlayers()[2].getBidValue(), cardWidth, cardHeight, -cardWidth, Gdx.graphics.getHeight()/2 - edgePadding - cardHeight - (cardHeight/2));
				drawBidSuit(game.engine.getPlayers()[2].getBidSuit(), cardWidth, cardHeight, 0, Gdx.graphics.getHeight()/2 - edgePadding - cardHeight - (cardHeight/2));
			}
			if(game.engine.getWinningBidder() == 3) {					
				drawBidSprite(game.engine.getPlayers()[3].getBidValue(), cardWidth, cardHeight, Gdx.graphics.getWidth()/2 - edgePadding - cardWidth - (cardWidth/2), 0);
				drawBidSuit(game.engine.getPlayers()[3].getBidSuit(), cardWidth, cardHeight, Gdx.graphics.getWidth()/2 - edgePadding - cardWidth - (cardWidth/2), -cardHeight);
			}
			
			//Draw whether players are staying or not
			float stayWidth = cardWidth;
			float stayHeight = cardHeight;
			if(game.engine.getWinningBidder() != 1 && game.engine.getStaying()[1] != ChipperEngine.STAYING_WAITING) {
				float stayX = -Gdx.graphics.getWidth()/2 + edgePadding;
				bidBackgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);
//					background.draw(batch, player2HandOriginX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 2 && game.engine.getStaying()[2] != ChipperEngine.STAYING_WAITING) {
				bidBackgroundSprite.setBounds(-stayWidth/2, Gdx.graphics.getHeight()/2 - edgePadding - stayHeight, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);
//					background.draw(batch, -stayWidth/2, Gdx.graphics.getHeight()/2 - edgePadding, stayWidth, stayHeight);
				float fontY = ((Gdx.graphics.getHeight()/2) - edgePadding - 5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, -stayWidth/2, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 3 && game.engine.getStaying()[3] != ChipperEngine.STAYING_WAITING) {
				float stayX = Gdx.graphics.getWidth()/2 - edgePadding - stayWidth;
				bidBackgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);					
//					background.draw(batch, stayX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			
			if(game.engine.getCurrentStayer() == 0) {
				background.draw(batch, stay.x, stay.y, stay.width, stay.height);
				float fontY = (stay.y + stay.height-5) - ((stay.height-10-font.getBounds("Stay").height)/2);
				font.drawMultiLine(batch, "Stay", stay.x, fontY, stay.width, HAlignment.CENTER);

				background.draw(batch, stayPass.x, stayPass.y, stayPass.width, stayPass.height);
				fontY = (stayPass.y + stayPass.height-5) - ((stayPass.height-10-font.getBounds("Pass").height)/2);
				font.drawWrapped(batch, "Pass", stayPass.x, fontY, stayPass.width, HAlignment.CENTER);
			} else {
				float stayingWidth = Gdx.graphics.getWidth()*0.4f;
				float stayingHeight = Gdx.graphics.getHeight()*0.1f;
				background.draw(batch, -stayingWidth/2, -stayingHeight/2, stayingWidth, stayingHeight);
				float fontY = ((stayingHeight/2)-5) - ((stayingHeight-10-font.getBounds("Bidding").height)/2);
				font.drawMultiLine(batch, "Staying", -stayingWidth/2, fontY, stayingWidth, HAlignment.CENTER);
			}
		} else {
			//Draw whether players are staying or not
			float stayWidth = cardWidth;
			float stayHeight = cardHeight;
			if(game.engine.getWinningBidder() != 1 && game.engine.getStaying()[1] != ChipperEngine.STAYING_WAITING) {
				float stayX = -Gdx.graphics.getWidth()/2 + edgePadding;
				bidBackgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);
//					background.draw(batch, player2HandOriginX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[1] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 2 && game.engine.getStaying()[2] != ChipperEngine.STAYING_WAITING) {
				bidBackgroundSprite.setBounds(-stayWidth/2, Gdx.graphics.getHeight()/2 - edgePadding - stayHeight, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);
//					background.draw(batch, -stayWidth/2, Gdx.graphics.getHeight()/2 - edgePadding, stayWidth, stayHeight);
				float fontY = ((Gdx.graphics.getHeight()/2) - edgePadding - 5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[2] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, -stayWidth/2, fontY, stayWidth, HAlignment.CENTER);
			}
			if(game.engine.getWinningBidder() != 3 && game.engine.getStaying()[3] != ChipperEngine.STAYING_WAITING) {
				float stayX = Gdx.graphics.getWidth()/2 - edgePadding - stayWidth;
				bidBackgroundSprite.setBounds(stayX, -stayHeight/2, stayWidth, stayHeight);
				bidBackgroundSprite.draw(batch);					
//					background.draw(batch, stayX, -stayHeight/2, stayWidth, stayHeight);
				float fontY = ((-stayHeight/2) + stayHeight-5) - ((stayHeight-10-font.getBounds("Stay").height)/2);
				String stayText = game.engine.getStaying()[3] == ChipperEngine.STAYING_STAY ? "Stay" : "Out";
				font.drawMultiLine(batch, stayText, stayX, fontY, stayWidth, HAlignment.CENTER);
			}
			
			float stayingWidth = Gdx.graphics.getWidth()*0.6f;
			float stayingHeight = Gdx.graphics.getHeight()*0.2f;
			background.draw(batch, -stayingWidth/2, -stayingHeight/2, stayingWidth, stayingHeight);
			float fontY = ((stayingHeight/2)-5) - ((stayingHeight-10-font.getBounds("Bidding").height)/2);
			font.drawMultiLine(batch, "Staying", -stayingWidth/2, fontY, stayingWidth, HAlignment.CENTER);
		}
	}

	/**
	 * Draws the dealer indicator
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawDealerIndicator(float edgePadding, float cardWidth,
			float cardHeight) {
		//Draw dealer indicator
		float spriteWidth = (Gdx.graphics.getWidth()*0.075f);

		dealerSprite.setSize(spriteWidth, spriteWidth);
		dealerSprite.setOrigin(spriteWidth/2, spriteWidth/2);
		switch(game.engine.getDealer()) {
		case 0:
			//			dealerSprite.setRotation(180);
			dealerSprite.setPosition(-spriteWidth/2, -Gdx.graphics.getHeight()/2 + edgePadding + cardHeight + spriteWidth);
			break;
		case 1:
			//			dealerSprite.setRotation(90);
			dealerSprite.setPosition(-Gdx.graphics.getWidth()/2 + edgePadding + cardWidth + spriteWidth, -spriteWidth/2);
			break;
		case 2:
			//			dealerSprite.setRotation(0);
			dealerSprite.setPosition(-spriteWidth/2, Gdx.graphics.getHeight()/2 - edgePadding - cardHeight - spriteWidth - spriteWidth);
			break;
		case 3:
			//			dealerSprite.setRotation(270);
			dealerSprite.setPosition(Gdx.graphics.getWidth()/2 - edgePadding - cardWidth - spriteWidth - spriteWidth, -spriteWidth/2);
			break;
		}
		dealerSprite.draw(batch);
	}

	/**
	 * Draws the player cards on the screen
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawAllPlayerCards(float edgePadding, float cardWidth,
			float cardHeight) {
		//Draw player 1 cards (bottom)
		Hand player1Hand = game.engine.getPlayers()[0].getHand();
		playerCards = new Rectangle[player1Hand.getCards().size()];
		float player1HandOriginX = -cardWidth*((float)player1Hand.getCards().size()/2);
		float player1HandOriginY = -Gdx.graphics.getHeight()/2 + edgePadding;

		for(int card = 0;card<player1Hand.getCards().size();card++) {
			int[] currentCard = player1Hand.getCards().get(card);

			cardSprites[currentCard[0]][currentCard[1]].setSize(cardWidth, cardHeight);
			cardSprites[currentCard[0]][currentCard[1]].setPosition(player1HandOriginX+(cardWidth*card), player1HandOriginY);
			//Adjust card display to indicate selected card
			if(game.engine.getSelectedCard() == card) {
				cardSprites[currentCard[0]][currentCard[1]].setColor(0.4f, 0.4f, 1.0f, 1.0f);
			} else {
				cardSprites[currentCard[0]][currentCard[1]].setColor(1, 1, 1, 1.0f);
			}
			cardSprites[currentCard[0]][currentCard[1]].draw(batch);

			playerCards[card] = new Rectangle(player1HandOriginX+(cardWidth*card), player1HandOriginY, cardWidth, cardHeight);
		}

		//Draw player 2 cards (left)
		Hand player2Hand = game.engine.getPlayers()[1].getHand();
		float player2HandOriginX = -Gdx.graphics.getWidth()/2 + edgePadding;
		float player2HandOriginY = cardHeight*(((float)player2Hand.getCards().size()/2)-1); //Only 2 since position is from bottom left

		for(int card = 0;card<player2Hand.getCards().size();card++) {
			if(showCards) {
				int[] currentCard = player2Hand.getCards().get(card);
				cardSprites[currentCard[0]][currentCard[1]].setSize(cardWidth, cardHeight);
				cardSprites[currentCard[0]][currentCard[1]].setPosition(player2HandOriginX, player2HandOriginY-(cardHeight*card));
				cardSprites[currentCard[0]][currentCard[1]].draw(batch);
			} else {
				cardBack.setSize(cardWidth, cardHeight);
				cardBack.setPosition(player2HandOriginX, player2HandOriginY-(cardHeight*card));
				cardBack.draw(batch);
			}
		}

		//Draw player 3 cards (top)
		Hand player3Hand = game.engine.getPlayers()[2].getHand();
		float player3HandOriginX = -cardWidth*((float)player3Hand.getCards().size()/2);
		float player3handOriginY = Gdx.graphics.getHeight()/2 - cardHeight - edgePadding;

		for(int card = 0;card<player3Hand.getCards().size();card++) {
			if(showCards) {
				int[] currentCard = player3Hand.getCards().get(card);
				cardSprites[currentCard[0]][currentCard[1]].setSize(cardWidth, cardHeight);
				cardSprites[currentCard[0]][currentCard[1]].setPosition(player3HandOriginX+(cardWidth*card), player3handOriginY);
				cardSprites[currentCard[0]][currentCard[1]].draw(batch);
			} else {
				cardBack.setSize(cardWidth, cardHeight);
				cardBack.setPosition(player3HandOriginX+(cardWidth*card), player3handOriginY);
				cardBack.draw(batch);
			}
		}

		//Draw player 4 cards (right)
		Hand player4Hand = game.engine.getPlayers()[3].getHand();
		float player4HandOriginX = Gdx.graphics.getWidth()/2 - cardWidth - edgePadding;
		float player4HandOriginY = cardHeight*(((float)player4Hand.getCards().size()/2)-1); //Only 2 since position is from bottom left

		for(int card = 0;card<player4Hand.getCards().size();card++) {
			if(showCards) {
				int[] currentCard = player4Hand.getCards().get(card);
				cardSprites[currentCard[0]][currentCard[1]].setSize(cardWidth, cardHeight);
				cardSprites[currentCard[0]][currentCard[1]].setPosition(player4HandOriginX, player4HandOriginY-(cardHeight*card));
				cardSprites[currentCard[0]][currentCard[1]].draw(batch);
			} else {
				cardBack.setSize(cardWidth, cardHeight);
				cardBack.setPosition(player4HandOriginX, player4HandOriginY-(cardHeight*card));
				cardBack.draw(batch);
			}
		}
	}

	/**
	 * Draws the bid number state
	 * 
	 * @param edgePadding
	 * @param cardWidth
	 * @param cardHeight
	 */
	private void drawBidNumberState(float edgePadding, float cardWidth,
			float cardHeight) {
		float bidWidth = Gdx.graphics.getWidth()*0.6f;
		float bidHeight = Gdx.graphics.getHeight()*0.5f;
		
		if(game.engine.getCurrentBidder() == 0) {
			int highBid = Constants.BID_PASS;
			for(int i = 1;i<4;i++) {
				if(game.engine.getPlayers()[i].getBidValue() > highBid) {
					highBid = game.engine.getPlayers()[i].getBidValue();
				}
			}

			background.draw(batch, -bidWidth/2, -bidHeight/2, bidWidth, bidHeight);

			while(font.getBounds("How many do you bid?").width > bidWidth) {
				font.scale(-0.1f);
			}
			font.drawMultiLine(batch,"How many do\nyou bid?", -bidWidth/2, (bidHeight/2)-8, bidWidth, HAlignment.CENTER);

			float fontY = 0;
			if(highBid < Constants.BID_CHIPPER) {
				background.draw(batch, bidChipper.x, bidChipper.y, bidChipper.width, bidChipper.height);
				fontY = (bidChipper.y+bidChipper.height-5)-((bidChipper.height-10-font.getBounds("Chipper").height)/2);
				font.drawMultiLine(batch, "Chipper", bidChipper.x, fontY, bidChipper.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_SIX) {
				background.draw(batch, bidSix.x, bidSix.y, bidSix.width, bidSix.height);
				fontY = (bidSix.y+bidSix.height-5)-((bidSix.height-10-font.getBounds("Six").height)/2);
				font.drawMultiLine(batch, "Six", bidSix.x, fontY, bidSix.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_FIVE) {
				background.draw(batch, bidFive.x, bidFive.y, bidFive.width, bidFive.height);
				fontY = (bidFive.y+bidFive.height-5)-((bidFive.height-10-font.getBounds("Five").height)/2);
				font.drawMultiLine(batch, "Five", bidFive.x, fontY, bidFive.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_FOUR) {
				background.draw(batch, bidFour.x, bidFour.y, bidFour.width, bidFour.height);
				fontY = (bidFour.y+bidFour.height-5)-((bidFour.height-10-font.getBounds("Four").height)/2);
				font.drawMultiLine(batch, "Four", bidFour.x, fontY, bidFour.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_THREE) {
				background.draw(batch, bidThree.x, bidThree.y, bidThree.width, bidThree.height);
				fontY = (bidThree.y+bidThree.height-5)-((bidThree.height-10-font.getBounds("Three").height)/2);
				font.drawMultiLine(batch, "Three", bidThree.x, fontY, bidThree.width, HAlignment.CENTER);
			}

			if(highBid < Constants.BID_TWO) {
				background.draw(batch, bidTwo.x, bidTwo.y, bidTwo.width, bidTwo.height);
				fontY = (bidTwo.y+bidTwo.height-5)-((bidTwo.height-10-font.getBounds("Two").height)/2);
				font.drawMultiLine(batch, "Two", bidTwo.x, fontY, bidTwo.width, HAlignment.CENTER);
			}

			background.draw(batch, bidPass.x, bidPass.y, bidPass.width, bidPass.height);
			fontY = (bidPass.y+bidPass.height-5)-((bidPass.height-10-font.getBounds("Pass").height)/2);
			font.drawMultiLine(batch, "Pass", bidPass.x, fontY, bidPass.width, HAlignment.CENTER);
		} else {
			float biddingHeight = Gdx.graphics.getHeight()*0.2f;
			background.draw(batch, -bidWidth/2, -biddingHeight/2, bidWidth, biddingHeight);
			float fontY = ((biddingHeight/2)-5) - ((biddingHeight-10-font.getBounds("Bidding").height)/2);
			font.drawMultiLine(batch, "Bidding", -bidWidth/2, fontY, bidWidth, HAlignment.CENTER);
		}
		if(game.engine.getPlayers()[0].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[0].getBidValue(), cardWidth, cardHeight, -cardWidth/2, -Gdx.graphics.getHeight()/2 + edgePadding);
//				Gdx.app.log("DRAWBID", "Player 1");
		}		
		if(game.engine.getPlayers()[1].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[1].getBidValue(), cardWidth, cardHeight, -Gdx.graphics.getWidth()/2 + edgePadding, -cardHeight/2);
//				Gdx.app.log("DRAWBID", "Player 2");
		}
		if(game.engine.getPlayers()[2].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[2].getBidValue(), cardWidth, cardHeight, -cardWidth/2, Gdx.graphics.getHeight()/2 - edgePadding - cardHeight);
//				Gdx.app.log("DRAWBID", "Player 3");
		}
		if(game.engine.getPlayers()[3].getBidValue() != -1) {					
			drawBidSprite(game.engine.getPlayers()[3].getBidValue(), cardWidth, cardHeight, Gdx.graphics.getWidth()/2 - edgePadding - cardWidth, -cardHeight/2);
//				Gdx.app.log("DRAWBID", "Player 4");
		}
	}

	/**
	 * Draws the bid won state
	 */
	private void drawBidWonState(float edgePadding, float cardWidth, float cardHeight) {
		float bidWidth = Gdx.graphics.getWidth()*0.6f;
		float bidHeight = Gdx.graphics.getHeight()*0.48f;
		
		background.draw(batch, -bidWidth/2, -bidHeight/2, bidWidth, bidHeight);
		
		while(font.getBounds("In what suit?").width > bidWidth) {
			font.scale(-0.1f);
		}
		font.drawMultiLine(batch,"In what suit?", -bidWidth/2, (bidHeight/2)-8, bidWidth, HAlignment.CENTER);
		
		heartSprite.setBounds(bidHearts.x, bidHearts.y, bidHearts.width, bidHearts.height);
		heartSprite.draw(batch);

		diamondSprite.setBounds(bidDiamonds.x, bidDiamonds.y, bidDiamonds.width, bidDiamonds.height);
		diamondSprite.draw(batch);

		spadeSprite.setBounds(bidSpades.x, bidSpades.y, bidSpades.width, bidSpades.height);
		spadeSprite.draw(batch);

		clubSprite.setBounds(bidClubs.x, bidClubs.y, bidClubs.width, bidClubs.height);
		clubSprite.draw(batch);
		
		background.draw(batch, bidNoTrump.x, bidNoTrump.y, bidNoTrump.width, bidNoTrump.height);
		float fontY = (bidNoTrump.y+bidNoTrump.height-5) - ((bidNoTrump.height-10-font.getBounds("No Trump").height)/2);
		font.drawMultiLine(batch, "No Trump", bidNoTrump.x, fontY, bidNoTrump.width, HAlignment.CENTER);
		
		if(game.engine.getPlayers()[0].getBidValue() != -1) {
			drawBidSprite(game.engine.getPlayers()[0].getBidValue(), cardWidth, cardHeight, -cardWidth/2, -Gdx.graphics.getHeight()/2 + edgePadding);
//				Gdx.app.log("DRAWBID", "Player 1");
		}	
	}

	/**
	 * Draws the bid suit
	 * @param bidSuit
	 * @param cardWidth
	 * @param cardHeight
	 * @param x
	 * @param y
	 */
	public void drawBidSuit(int bidSuit, float cardWidth, float cardHeight, float x, float y) {
		bidBackgroundSprite.setBounds(x, y, cardWidth, cardHeight);
		bidBackgroundSprite.draw(batch);
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
	
	/**
	 * Draws the bid sprite with the passed bounds
	 * @param bidValue
	 * @param cardWidth
	 * @param cardHeight
	 * @param x
	 * @param y
	 */
	private void drawBidSprite(int bidValue, float cardWidth, float cardHeight,
			float x, float y) {
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

	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(w, h);
		batch = new SpriteBatch();
		
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
		bidBackgroundSprite = new Sprite(new TextureRegion(cardsheet, 7*Constants.SHEET_CARD_WIDTH, 5*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		
		arrowSprite = new Sprite(new TextureRegion(tilesheet, 0, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		dealerSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		leadSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*3, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		heartSprite = new Sprite(new TextureRegion(tilesheet, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		spadeSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		diamondSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		clubSprite = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH*3, Constants.TILE_WIDTH, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		noTrumpSprite = new Sprite(new TextureRegion(tilesheet, 0, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		
		background = new NinePatch(new TextureRegion(tilesheet, Constants.TILE_WIDTH*2, 0, Constants.TILE_WIDTH, Constants.TILE_WIDTH), 5, 5, 5, 5);
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);

		//Scale font right off the bat to how we scale it later
		while(font.getBounds("How many do you bid?").width < w*0.6f) {
			font.scale(0.1f);
		}
		while(font.getBounds("How many do you bid?").width > w*0.6f) {
			font.scale(-0.1f);
		}

		float bidButtonWidth = Gdx.graphics.getWidth()*0.5f;
		float bidButtonHeight = Gdx.graphics.getHeight()*0.05f;
		float bidButtonY = bidButtonHeight*2;
		bidChipper = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidSix = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidFive = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidFour = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidThree = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidTwo = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		bidButtonY-=bidButtonHeight;
		bidPass = new Rectangle(-bidButtonWidth/2, bidButtonY, bidButtonWidth, bidButtonHeight);
		
		float suitButtonWidth = Gdx.graphics.getWidth()*0.25f;
		float noTrumpButtonHeight = Gdx.graphics.getHeight()*0.07f;
		
		bidHearts = new Rectangle(-suitButtonWidth, 0, suitButtonWidth, suitButtonWidth);
		bidDiamonds = new Rectangle(0, 0, suitButtonWidth, suitButtonWidth);
		bidSpades = new Rectangle(-suitButtonWidth, -suitButtonWidth, suitButtonWidth, suitButtonWidth);
		bidClubs = new Rectangle(0, -suitButtonWidth, suitButtonWidth, suitButtonWidth);
		bidNoTrump = new Rectangle(-suitButtonWidth, -suitButtonWidth-noTrumpButtonHeight, suitButtonWidth*2, noTrumpButtonHeight);
		
		
		stay = new Rectangle(-bidButtonWidth/2, 0, bidButtonWidth, bidButtonHeight);
		stayPass = new Rectangle(-bidButtonWidth/2, -bidButtonHeight, bidButtonWidth, bidButtonHeight);
		
		manager = new TweenManager();
		
		Tween.registerAccessor(Sprite.class, new SpriteAccessor());
		
		Gdx.input.setInputProcessor(this);
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
	public boolean touchDown(int x, int y, int pointer, int button) {
		Vector3 pos = new Vector3(x, y, 0);
		camera.unproject(pos);

		if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_NUMBER) {
			
			int highBid = Constants.BID_PASS;
			for(int i = 1;i<4;i++) {
				if(game.engine.getPlayers()[i].getBidValue() > highBid) {
					highBid = game.engine.getPlayers()[i].getBidValue();
				}
			}
			
			if(bidChipper.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_CHIPPER);
//				Gdx.app.log("BIDCLICK", "chipper");
			} else if(bidSix.contains(pos.x, pos.y) && highBid < Constants.BID_SIX) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_SIX);
//				Gdx.app.log("BIDCLICK", "6");
			} else if(bidFive.contains(pos.x, pos.y) && highBid < Constants.BID_FIVE) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_FIVE);
//				Gdx.app.log("BIDCLICK", "5");
			} else if(bidFour.contains(pos.x, pos.y) && highBid < Constants.BID_FOUR) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_FOUR);
//				Gdx.app.log("BIDCLICK", "4");
			} else if(bidThree.contains(pos.x, pos.y) && highBid < Constants.BID_THREE) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_THREE);
//				Gdx.app.log("BIDCLICK", "3");
			} else if(bidTwo.contains(pos.x, pos.y) && highBid < Constants.BID_TWO) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_TWO);
//				Gdx.app.log("BIDCLICK", "2");
			} else if(bidPass.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidValue(Constants.BID_PASS);
//				Gdx.app.log("BIDCLICK", "pass");
			}
			game.engine.handlePlayerBid();
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_BID_WON) {
			if(bidHearts.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.HEARTS);
//				Gdx.app.log("SUITCLICK", "hearts");
			} else if(bidDiamonds.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.DIAMONDS);
//				Gdx.app.log("SUITCLICK", "diamonds");
			} else if(bidSpades.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.SPADES);
//				Gdx.app.log("SUITCLICK", "spades");
			} else if(bidClubs.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.CLUBS);
//				Gdx.app.log("SUITCLICK", "clubs");
			} else if(bidNoTrump.contains(pos.x, pos.y)) {
				game.engine.getPlayers()[0].setBidSuit(Constants.NO_TRUMP);
//				Gdx.app.log("SUITCLICK", "no trump");
			}
			game.engine.handlePlayerSuit();
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_STAY) {
			if(stay.contains(pos.x, pos.y)){
				game.engine.playerStaying(ChipperEngine.STAYING_STAY);
			} else if(stayPass.contains(pos.x, pos.y)) {
				game.engine.playerStaying(ChipperEngine.STAYING_OUT);
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_PLAY) {
			if(game.engine.getCurrentPlayer() == 0) {
				if(game.engine.getSelectedCard() != -1 && playerPlayedCard.contains(pos.x, pos.y)) {
					int[] card = game.engine.getPlayers()[0].getHand().getCards().get(game.engine.getSelectedCard());
					Tween.to(cardSprites[card[0]][card[1]], SpriteAccessor.POSITION_XY, 0.5f)
						.target(-cardWidth/2, -(cardHeight/2)-cardHeight)
						.ease(Linear.INOUT)
						.setCallbackTriggers(TweenCallback.COMPLETE)
						.setCallback(new TweenCallback() {
							@Override
							public void onEvent(int type, BaseTween<?> source) {
								game.engine.playPlayerCard(game.engine.getSelectedCard());
								game.engine.setSelectedCard(-1);
							}						
						}).start(manager);

				}
			}
		} else if(game.engine.getPlaystate() == ChipperEngine.PLAYSTATE_OVER) {
			if(nextHandButton.contains(pos.x, pos.y)) {
				game.engine.nextHand();
			}
		}
		//		Gdx.app.log("touchDown", "original: " + x + "," + y + " -- unprojected: " + pos.x + "," + pos.y);
		for(int card = 0;card < playerCards.length; card++) {
			if(playerCards[card].contains(pos.x, pos.y)) {
				//Select the card
				if(game.engine.getSelectedCard() == -1) {
					game.engine.setSelectedCard(card);
				}
				//Un-select the card
				else if(game.engine.getSelectedCard() == card) {
					game.engine.setSelectedCard(-1);
				}
				//Move the cards
				else {
					game.engine.swapPlayerCards(game.engine.getSelectedCard(), card);
					game.engine.setSelectedCard(-1);
				}
			}
		}

//		engine.nextDealer();

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
