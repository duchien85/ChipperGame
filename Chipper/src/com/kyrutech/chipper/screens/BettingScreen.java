package com.kyrutech.chipper.screens;

import java.text.DecimalFormat;

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
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;
import com.kyrutech.chipper.aiagent.AIAgent;
import com.kyrutech.chipper.gameobjects.Hand;

public class BettingScreen implements Screen, InputProcessor {
	
	private static final int CARD_SELECT_STATE = 0;
	private static final int CALCULATION_STATE = 1;
	private static final int RESULT_STATE = 2;
	
	private int state = CARD_SELECT_STATE;
	
	ChipperGame game;
	
	private OrthographicCamera camera;
	private Rectangle viewport;
	private SpriteBatch batch;
	private Texture cardsheet, tilesheet;
	private Sprite cardSprites[][] = new Sprite[4][13];
	private boolean cardSelected[][] = new boolean[4][13];
	private Sprite cardBack, tableBackground;
	private NinePatch background, antibackground;
	
	private Rectangle getBidButton;
	private boolean getBidPressed = false;
	private boolean selectMoreCards = false;
	
	private Rectangle backButton;
	private boolean backPressed = false;
	
	private BitmapFont font;
	private float textHeight;
	
	private float edgePadding, cardWidth, cardHeight;
	private float cardPadding;
	
	private int selectedCards = 0;
	
	
	
	private float bids[];
	
	public BettingScreen(ChipperGame game) {
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
		
		if(state == CARD_SELECT_STATE) {			
			
			font.setColor(Color.WHITE);
			font.drawMultiLine(batch, "Select " + (6-selectedCards) + " More Cards", -Constants.VIRTUAL_WIDTH/2, (Constants.VIRTUAL_HEIGHT/2)-edgePadding, Constants.VIRTUAL_WIDTH, HAlignment.CENTER);
			
			//Draw cards
			float cardPadding = (Constants.VIRTUAL_WIDTH - (edgePadding*2) - (cardWidth*6))/5;
			float cardX, cardY;
			
			for(int i = 0;i<4;i++) {
				cardX = (-Constants.VIRTUAL_WIDTH/2) + edgePadding;
				cardY = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - textHeight - edgePadding - cardHeight - (cardHeight*i) - (edgePadding*i);
				
				cardSprites[i][Constants.NINE].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.NINE]) {
					cardSprites[i][Constants.NINE].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.NINE].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.NINE].draw(batch);
				cardX = cardX + cardWidth + cardPadding;
				
				cardSprites[i][Constants.TEN].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.TEN]) {
					cardSprites[i][Constants.TEN].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.TEN].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.TEN].draw(batch);
				cardX = cardX + cardWidth + cardPadding;
				
				cardSprites[i][Constants.JACK].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.JACK]) {
					cardSprites[i][Constants.JACK].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.JACK].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.JACK].draw(batch);					
				cardX = cardX + cardWidth + cardPadding;
				
				cardSprites[i][Constants.QUEEN].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.QUEEN]) {
					cardSprites[i][Constants.QUEEN].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.QUEEN].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.QUEEN].draw(batch);					
				cardX = cardX + cardWidth + cardPadding;
				
				cardSprites[i][Constants.KING].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.KING]) {
					cardSprites[i][Constants.KING].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.KING].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.KING].draw(batch);					
				cardX = cardX + cardWidth + cardPadding;
				
				cardSprites[i][Constants.ACE].setBounds(cardX, cardY, cardWidth, cardHeight);
				if(cardSelected[i][Constants.ACE]) {
					cardSprites[i][Constants.ACE].setColor(0.4f, 0.4f, 1.0f, 1.0f);
				} else {
					cardSprites[i][Constants.ACE].setColor(Color.WHITE);
				}
				cardSprites[i][Constants.ACE].draw(batch);					
				cardX = cardX + cardWidth + cardPadding;
								
			}
			
			//Draw Get Bet button
			if(getBidPressed) {
				antibackground.draw(batch, getBidButton.x, getBidButton.y, getBidButton.width, getBidButton.height);
			} else {
				background.draw(batch, getBidButton.x, getBidButton.y, getBidButton.width, getBidButton.height);
			}
			font.drawMultiLine(batch, "Get Bids", getBidButton.x, getBidButton.y+(getBidButton.height/2)+(textHeight/2), getBidButton.width, HAlignment.CENTER);
			
			//Draw back button
			if(backPressed) {
				antibackground.draw(batch, backButton.x, backButton.y, backButton.width, backButton.height);
			} else {
				background.draw(batch, backButton.x, backButton.y, backButton.width, backButton.height);
			}
			font.drawMultiLine(batch, "Back", backButton.x, backButton.y+(backButton.height/2)+(textHeight/2), backButton.width, HAlignment.CENTER);
			
			//Draw bet lines
			float bidsX = edgePadding;
			float bidsY = getBidButton.y + getBidButton.height;
			float bidsWidth = (Constants.VIRTUAL_WIDTH/2) - (edgePadding*2);
			
			DecimalFormat dm = new DecimalFormat("0.00");
			
			font.drawMultiLine(batch, "Bid Estimates", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			bidsY-=textHeight + edgePadding;
			
			//Clubs
			font.drawMultiLine(batch, "Clubs", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			font.drawMultiLine(batch, dm.format(bids[Constants.CLUBS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
			bidsY-=textHeight + edgePadding;
			//Spades
			font.drawMultiLine(batch, "Spades", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			font.drawMultiLine(batch, dm.format(bids[Constants.SPADES]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
			bidsY-=textHeight + edgePadding;
			//Hearts
			font.drawMultiLine(batch, "Hearts", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			font.drawMultiLine(batch, dm.format(bids[Constants.HEARTS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
			bidsY-=textHeight + edgePadding;
			//Diamonds
			font.drawMultiLine(batch, "Diamonds", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			font.drawMultiLine(batch, dm.format(bids[Constants.DIAMONDS]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
			bidsY-=textHeight + edgePadding;
			//No Trump
			font.drawMultiLine(batch, "No Trump", bidsX, bidsY, bidsWidth, HAlignment.LEFT);
			font.drawMultiLine(batch, dm.format(bids[Constants.NO_TRUMP]), bidsX, bidsY, bidsWidth, HAlignment.RIGHT);
			bidsY-=textHeight + edgePadding;
			
			if(selectMoreCards) {
				drawSelectMoreCards();
			}
			
		} else if(state == CALCULATION_STATE) {
			
		} else if(state == RESULT_STATE) {
			
		}
		
		batch.end();

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
	public void show() {
		float w = Constants.VIRTUAL_WIDTH;
		float h = Constants.VIRTUAL_HEIGHT;
		
		edgePadding = (h*0.0125f);
		cardWidth = (w*0.15f);
		cardHeight = (h*0.12f);
		cardPadding = (Constants.VIRTUAL_WIDTH - (edgePadding*2) - (cardWidth*6))/5;
		
		bids = new float[] {0,0,0,0,0};
		selectedCards = 0;
		
		camera = new OrthographicCamera(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
		batch = new SpriteBatch();
		
		cardsheet = new Texture(Gdx.files.internal("data/cardsheet.png"));
		cardsheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
//		TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);

		for(int suit = 0;suit<4;suit++) {
			for(int value = 0;value<13;value++) {
				cardSprites[suit][value] = new Sprite(new TextureRegion(cardsheet, value*Constants.SHEET_CARD_WIDTH, suit*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
				cardSelected[suit][value] = false;
			}
		}
		
		background = new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 5, 5, 5, 5);
		antibackground = new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/antibackground.png"))), 5, 5, 5, 5);
		
		cardBack = new Sprite(new TextureRegion(cardsheet, 2*Constants.SHEET_CARD_WIDTH, 4*Constants.SHEET_CARD_HEIGHT, Constants.SHEET_CARD_WIDTH, Constants.SHEET_CARD_HEIGHT));
		tableBackground = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);
		textHeight = font.getBounds("HEIGHT").height;
		
		float gbry = (Constants.VIRTUAL_HEIGHT/2) - textHeight  - (cardHeight*5) - (edgePadding*6);
		getBidButton = new Rectangle((-Constants.VIRTUAL_WIDTH/2) + edgePadding, gbry, (Constants.VIRTUAL_WIDTH/2)-edgePadding-edgePadding, cardHeight);		
		
		backButton = new Rectangle(-(Constants.VIRTUAL_WIDTH/2) + edgePadding, gbry - getBidButton.height - edgePadding, (Constants.VIRTUAL_WIDTH/2)-edgePadding-edgePadding, cardHeight);
		
		Gdx.input.setInputProcessor(this);
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
		
		if(selectMoreCards) { //Just return immediately, we don't need to handle buttons in this situation
			return true;
		}
		
		if(getBidButton.contains(pos.x, pos.y)) {
			getBidPressed = true;
		}
		
		if(backButton.contains(pos.x, pos.y)) {
			backPressed = true;
		}
		
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		Vector3 pos = new Vector3(screenX, screenY, 0);
//		camera.unproject(pos);
		camera.unproject(pos, viewport.x, viewport.y, viewport.width, viewport.height);
		
		//Reset pressed flags
		getBidPressed = false;
		backPressed = false;
		
		if(selectMoreCards) {
			selectMoreCards = false;
			return true;
		}
		
		//Check if we are in a card
		int cardSuit = getPressedSuit(pos.y);
		int cardValue = getPressedValue(pos.x);
//		Gdx.app.log("CARDPRESSCHECK", "cardSuit: " + cardSuit + " cardValue: " + cardValue);
		
		
		if(cardSuit != -1 && cardValue != -1) {
			if(cardSelected[cardSuit][cardValue]) {
				cardSelected[cardSuit][cardValue] = false;
				selectedCards--;
			} else {
				if(selectedCards < 6) {
					cardSelected[cardSuit][cardValue] = true;
					selectedCards++;
				}
			}
		}
		
		//Handle get bet button
		if(getBidButton.contains(pos.x, pos.y)) {
			if(selectedCards == 6) {
				Hand betHand = new Hand();
				for(int i = 0;i<4;i++) {
					for(int j = 0;j<13;j++) {
						if(cardSelected[i][j]) {
							int[] card = {i, j};
							betHand.addCard(card);
						}
					}
				}
				bids = AIAgent.getBids(betHand);
			} else {
				//Indicate we need to select more cards
				selectMoreCards = true;
			}
		}
		
		//Handle back button
		if(backButton.contains(pos.x, pos.y)) {
			game.returnToMenu();
		}
		
		return true;
	}

	/**
	 * Draws the follow suit dialog
	 */
	private void drawSelectMoreCards() {
		float selectWidth = Constants.VIRTUAL_WIDTH*0.7f;
		float selectHeight = Constants.VIRTUAL_HEIGHT*0.2f;
		background.draw(batch, -selectWidth/2, -selectHeight/2, selectWidth, selectHeight);

		float textY = (selectHeight/2) - ((selectHeight - textHeight)/2);
		font.drawMultiLine(batch, "Select More Cards", -selectWidth/2, textY, selectWidth, HAlignment.CENTER);
	}
	
	/**
	 * Returns card value corresponding to the X position pressed
	 * @param x
	 * @return
	 */
	private int getPressedValue(float x) {
		int value = -1;
		
		float nineLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding;
		float tenLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding + cardWidth + cardPadding;
		float jackLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding + (cardWidth*2) + (cardPadding*2);
		float queenLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding + (cardWidth*3) + (cardPadding*3);
		float kingLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding + (cardWidth*4) + (cardPadding*4);
		float aceLeft = (-Constants.VIRTUAL_WIDTH/2) + edgePadding + (cardWidth*5) + (cardPadding*5);
		
		if(x > nineLeft && x < nineLeft + cardWidth) {
			value = Constants.NINE;
		}
		if(x > tenLeft && x < tenLeft + cardWidth) {
			value = Constants.TEN;
		}
		if(x > jackLeft && x < jackLeft + cardWidth) {
			value = Constants.JACK;
		}
		if(x > queenLeft && x < queenLeft + cardWidth) {
			value = Constants.QUEEN;
		}
		if(x > kingLeft && x < kingLeft + cardWidth) {
			value = Constants.KING;
		}
		if(x > aceLeft && x < aceLeft + cardWidth) {
			value = Constants.ACE;
		}
		return value;
	}

	/**
	 * Returns suit corresponding the Y position pressed 
	 * @param y
	 * @return
	 */
	private int getPressedSuit(float y) {
		int suit = -1;
		
		float clubsTop = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - textHeight - edgePadding - (cardHeight*0) - (edgePadding*0);
		float spadesTop = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - textHeight - edgePadding - (cardHeight*1) - (edgePadding*1);
		float heartsTop = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - textHeight - edgePadding - (cardHeight*2) - (edgePadding*2);
		float diamondsTop = (Constants.VIRTUAL_HEIGHT/2) - edgePadding - textHeight - edgePadding - (cardHeight*3) - (edgePadding*3);
		
		if(y < clubsTop && y > clubsTop - cardHeight) {
			suit = Constants.CLUBS;
		} 
		if(y < spadesTop && y > spadesTop - cardHeight) {
			suit = Constants.SPADES;
		}
		if(y < heartsTop && y > heartsTop - cardHeight) {
			suit = Constants.HEARTS;
		}
		if(y < diamondsTop && y > diamondsTop - cardHeight) {
			suit = Constants.DIAMONDS;
		}
		
		return suit;
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
