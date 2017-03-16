package com.kyrutech.chipper;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Chipper";
		cfg.useGL20 = false;
		cfg.width = 768;
		cfg.height = 1024;
		
		new LwjglApplication(new ChipperGame(), cfg);
	}
}
