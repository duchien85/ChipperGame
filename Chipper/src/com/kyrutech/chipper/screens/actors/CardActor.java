package com.kyrutech.chipper.screens.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kyrutech.chipper.ChipperEngine;

public class CardActor extends Actor {

	ChipperEngine engine;
	TextureRegion region;
	
	boolean selected = false;
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public CardActor(TextureRegion region, ChipperEngine engine) {
		this.engine = engine;
		this.region = region;
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
	}

	@Override
	public Color getColor() {	
		if(isSelected()) {
			return new Color(0.4f, 0.4f, 1.0f, 1.0f); //Highlight color
		} 
		return Color.WHITE;
	}

}
