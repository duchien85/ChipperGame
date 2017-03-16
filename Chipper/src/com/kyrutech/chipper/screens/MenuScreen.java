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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.kyrutech.chipper.ChipperGame;
import com.kyrutech.chipper.Constants;

public class MenuScreen implements Screen, InputProcessor {

	ChipperGame game;
	
	private SpriteBatch batch;
	private Stage menuStage;
	private BitmapFont font;
	private NinePatchDrawable background, antibackground;
	private Texture tilesheet, title, kyrutech;
	private Sprite tableBackground;
	
	private OrthographicCamera camera;
	private Rectangle viewport;
	
	private ShapeRenderer renderer;

	private InputMultiplexer inputMultiplexer;
	
	public MenuScreen(ChipperGame game) {
		this.game = game;
	}
	
	@Override
	public void render(float delta) {
                          
		Gdx.gl.glClearColor(0, 0.5f, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		menuStage.act(Gdx.graphics.getDeltaTime());
		menuStage.draw();
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
        
        menuStage.setCamera(camera);
        menuStage.setViewport(w, h, true);
//        Gdx.app.log("VIEWPORT", viewport.x + ":" + viewport.y + ":" + viewport.width + ":" + viewport.height);
		
	}

	@Override
	public void show() {
		batch = new SpriteBatch();
		
		renderer = new ShapeRenderer();
		
		camera = new OrthographicCamera(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
		
		tilesheet = new Texture(Gdx.files.internal("data/tilesheet.png"));
		tilesheet.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		background = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/background.png"))), 5, 5, 5, 5));
		antibackground = new NinePatchDrawable(new NinePatch(new TextureRegion(new Texture(Gdx.files.internal("data/antibackground.png"))), 5, 5, 5, 5));
		tableBackground = new Sprite(new TextureRegion(tilesheet, Constants.TILE_WIDTH, Constants.TILE_WIDTH*2, Constants.TILE_WIDTH, Constants.TILE_WIDTH));
		title = new Texture(Gdx.files.internal("data/title.png"));
		kyrutech = new Texture(Gdx.files.internal("data/kyrutech_logo.png"));
		
		Texture fontTexture = new Texture("font/franklingothicmedium28white.png");
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("font/franklingothicmedium28white.fnt"), new TextureRegion(fontTexture), false);
		
		//Scale font right off the bat to how we scale it later
//		while(font.getBounds("How many do you bid?").width < 480*0.6f) {
//			font.scale(0.1f);
//		}
//		while(font.getBounds("How many do you bid?").width > 480*0.6f) {
//			font.scale(-0.1f);
//		}
		
		menuStage = new Stage();
		
		Table table = new Table();
		table.setFillParent(true);
		table.setBackground(new SpriteDrawable(tableBackground));
		menuStage.addActor(table);
		
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = font;
		
//		Label title = new Label("Chipper", labelStyle);
//		title.y = title.getTextBounds().height/2;
//		title.x = title.getTextBounds().width/2;
		Image titleimage = new Image(title);
//		titleimage.size(380, 190);
		table.add(titleimage).pad(10).width(400).height(200);		
		
		table.row();
		
		TextButtonStyle buttonStyle = new TextButtonStyle();
		buttonStyle.up = (Drawable) background;
		buttonStyle.down = (Drawable) antibackground;
		buttonStyle.font = font;
		
		TextButton start = new TextButton("Start Game", buttonStyle);
		start.pad(10);
		start.addListener(new InputListener() {

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				game.initializePlay();
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
			
		});
		table.add(start).pad(10).width(400).height(75);
		
		table.row();
		
		TextButton instructions = new TextButton("Instructions", buttonStyle);
		instructions.pad(10);
		instructions.addListener(new InputListener() {

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				game.viewInstructions();
			}
			
		});
		table.add(instructions).pad(10).width(400).height(75);
		
		table.row();
		
		TextButton betting = new TextButton("Bid Helper", buttonStyle);
		betting.pad(10);
		betting.addListener(new InputListener() {
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				game.viewBetting();
			}
			
		});
		table.add(betting).pad(10).width(400).height(75);
		
		table.row();
		
		Image kLogo = new Image(kyrutech);
		table.add(kLogo).pad(10);
		
		table.row();
		
		
		
		Label version = new Label("v" + ChipperGame.VERSION, labelStyle);
		table.add(version).pad(10);
		
//		Gdx.input.setInputProcessor(menuStage);
		
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(this);
		inputMultiplexer.addProcessor(menuStage);
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
			Gdx.app.exit();			
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
