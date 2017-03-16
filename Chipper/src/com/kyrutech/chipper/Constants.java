package com.kyrutech.chipper;

public class Constants {

	public static final int SHEET_CARD_WIDTH = 72;
	public static final int SHEET_CARD_HEIGHT = 96;
	
	public static final int TILE_WIDTH = 128;
	
	public static final float VIRTUAL_WIDTH = 480;
	public static final float VIRTUAL_HEIGHT = 800;
	public static final float ASPECT_RATIO = (float)VIRTUAL_WIDTH/(float)VIRTUAL_HEIGHT;
	
	//Suits
	public static final int CLUBS = 0;
	public static final int SPADES = 1;
	public static final int HEARTS = 2;
	public static final int DIAMONDS = 3;
	public static final int NO_TRUMP = 4;
	
	//Card Values
	public static final int ACE = 0;
	public static final int TWO = 1;
	public static final int THREE = 2;
	public static final int FOUR = 3;
	public static final int FIVE = 4;
	public static final int SIX = 5;
	public static final int SEVEN = 6;
	public static final int EIGHT = 7;
	public static final int NINE = 8;
	public static final int TEN = 9;
	public static final int JACK = 10;
	public static final int QUEEN = 11;
	public static final int KING = 12;
	
	
	public static final int[] JOKER_A = {0, 4};
	public static final int[] JOKER_B = {1, 4};
	
	public static final int[] BLUE_BACK = {2, 4};
	public static final int[] RED_BACK = {3, 4};
	
	public static final int BID_PASS = 0;
	public static final int BID_TWO = 2;
	public static final int BID_THREE = 3;
	public static final int BID_FOUR = 4;
	public static final int BID_FIVE = 5;
	public static final int BID_SIX = 6;
	public static final int BID_CHIPPER = 7;
	
}
