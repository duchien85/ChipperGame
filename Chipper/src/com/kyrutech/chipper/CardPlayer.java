package com.kyrutech.chipper;

public interface CardPlayer {
	
	/**
	 * Determines if the player should stay
	 */
	public abstract boolean staying(int bid, int suit);
	
	/**
	 * Reviews cards and determines what to bet
	 */
	public abstract void determineBid();
	
	/**
	 * Returns the hand index of the card that the player should lead
	 * 
	 * @return
	 */
	public abstract int getCardToLead();
	
	/**
	 * Returns the current index of the card to play for this player
	 * @return
	 */
	public abstract int getCardToPlay();
	
}
