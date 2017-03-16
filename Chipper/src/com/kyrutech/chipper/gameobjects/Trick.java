package com.kyrutech.chipper.gameobjects;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.kyrutech.chipper.ChipperEngine;
import com.kyrutech.chipper.Constants;

public class Trick {
	
	int leadPlayer;
	
	int[] player0 = new int[] {-1, -1};
	int[] player1 = new int[] {-1, -1};
	int[] player2 = new int[] {-1, -1};
	int[] player3 = new int[] {-1, -1};
	
	boolean staying[] = new boolean[4];
	
	boolean debug = false;
	
	public Trick(int leadPlayer, int[] playerStay) {
		this.leadPlayer = leadPlayer;
		staying[0] = playerStay[0] == ChipperEngine.STAYING_STAY;
		staying[1] = playerStay[1] == ChipperEngine.STAYING_STAY;
		staying[2] = playerStay[2] == ChipperEngine.STAYING_STAY;
		staying[3] = playerStay[3] == ChipperEngine.STAYING_STAY;
	}
	
	/**
	 * Return current winning card, bidded suit is passed
	 * @param suit
	 * @return
	 */
	public int[] getWinningCard(int suit) {
		int[] winningCard = getPlayerCard(leadPlayer);

		if(player0[0] != -1 && betterCard(winningCard, player0, suit)) {
			winningCard = player0;
		}

		if(player1[0] != -1 && betterCard(winningCard, player1, suit)) {
			winningCard = player1;
		}			

		if(player2[0] != -1 && betterCard(winningCard, player2, suit)) {
			winningCard = player2;
		}

		if(player3[0] != -1 && betterCard(winningCard, player3, suit)) {
			winningCard = player3;
		}

		return winningCard;
	}
	
	/**
	 * Return current winning player, bidded suit is passed
	 * @param suit
	 * @return
	 */
	public int getWinningPlayer(int suit) {
		int winningPlayer = leadPlayer;
		int[] winningCard = getPlayerCard(leadPlayer);
		debug("Suit : " + suit);
		debug("Lead : " + leadPlayer);

		if(player0[0] != -1 && betterCard(winningCard, player0, suit)) {
			debug("Player 0 better card");
			winningPlayer = 0;
			winningCard = player0;
		}

		if(player1[0] != -1 && betterCard(winningCard, player1, suit)) {
			debug("Player 1 better card");
			winningPlayer = 1;
			winningCard = player1;
		}			

		if(player2[0] != -1 && betterCard(winningCard, player2, suit)) {
			debug("Player 2 better card");
			winningPlayer = 2;
			winningCard = player2;
		}

		if(player3[0] != -1 && betterCard(winningCard, player3, suit)) {
			debug("Player 3 better card");
			winningPlayer = 3;
			winningCard = player3;
		}

		return winningPlayer;
	}
	
	/**
	 * Returns true if the newCard is better, false if the initialCard is better
	 * 
	 * @param initialCard
	 * @param newCard
	 * @param bidSuit
	 * @return
	 */
	public boolean betterCard(int[] initialCard, int[] newCard, int bidSuit) {
		int initialIndex = -1;
		int newIndex = -1;
		
		int[] noTrump = ChipperEngine.getNoTrumpTrump();
		ArrayList<int[]> heartsTrump = ChipperEngine.getHeartsTrump();
		ArrayList<int[]> diamondsTrump = ChipperEngine.getDiamondsTrump();
		ArrayList<int[]> clubsTrump = ChipperEngine.getClubsTrump();
		ArrayList<int[]> spadesTrump = ChipperEngine.getSpadesTrump();
		
		if(bidSuit == Constants.NO_TRUMP) {
			if(initialCard[0] == newCard[0]) { //Same Suit
				
				for(int i = 0;i<noTrump.length;i++) {
					if(noTrump[i] == initialCard[1]) {
						initialIndex = i;
					}
					if(noTrump[i] == newCard[1]) {
						newIndex = i;
					}
				}
				return newIndex < initialIndex;
			} else {
				return false;
			}
		} else {
			switch(bidSuit) {
			case Constants.HEARTS:
				initialIndex = getCardIndex(initialCard, heartsTrump);
				newIndex = getCardIndex(newCard, heartsTrump);
				break;
			case Constants.DIAMONDS:
				initialIndex = getCardIndex(initialCard,diamondsTrump);
				newIndex = getCardIndex(newCard,diamondsTrump);
				break;
			case Constants.CLUBS:
				initialIndex = getCardIndex(initialCard,clubsTrump);
				newIndex = getCardIndex(newCard,clubsTrump);
				break;
			case Constants.SPADES:
				initialIndex = getCardIndex(initialCard,spadesTrump);
				newIndex = getCardIndex(newCard,spadesTrump);
				break;
			}
			debug("Indexes: " + initialIndex + " - " + newIndex);
			if(initialIndex != -1 && newIndex != -1) {  //Both trump, highest wins
				return newIndex < initialIndex;
			} else if(initialIndex != -1 && newIndex == -1) { //Only initial trump, wins
				return false;
			} else if(initialIndex == -1 && newIndex != -1) { //Only new trump, wins
				return true;
			} else { //Neither trump
				if(initialCard[0] == newCard[0]) { //Same Suit
					for(int i = 0;i<noTrump.length;i++) {
						if(noTrump[i] == initialCard[1]) {
							initialIndex = i;
						}
						if(noTrump[i] == newCard[1]) {
							newIndex = i;
						}
					}
					return newIndex < initialIndex;
				} else {
					return false;
				}
			}
		}
	}
	
	/**
	 * Returns the card for the passed player
	 * 
	 * @param player
	 * @return
	 */
	public int[] getPlayerCard(int player) {
		switch(player) {
		case 0:
			return player0;
		case 1:
			return player1;
		case 2:
			return player2;
		case 3:
			return player3;
		}
		return null;
	}
	
	/**
	 * Sets the card played by the passed player
	 * @param player
	 * @param card
	 */
	public void setPlayerCard(int player, int[] card) {
		switch(player) {
		case 0:
			player0 = card;
			break;
		case 1:
			player1 = card;
			break;
		case 2:
			player2 = card;
			break;
		case 3:
			player3 = card;
			break;
		}
	}
	
	/**
	 * Checks if a card has been played by all players
	 * @return
	 */
	public boolean completedTrick() {
		boolean completed = true;
		if(staying[0] && player0[0] == -1) {
			completed = false;
		}
		if(staying[1] && player1[0] == -1) {
			completed = false;
		}
		if(staying[2] && player2[0] == -1) {
			completed = false;
		}
		if(staying[3] && player3[0] == -1) {
			completed = false;
		}
		return completed;
	}

	public int getLeadPlayer() {
		return leadPlayer;
	}

	public void setLeadPlayer(int leadPlayer) {
		this.leadPlayer = leadPlayer;
	}
	
	public void debug(String message) {
		if(debug) {
			Gdx.app.log("TRICK", message);
		}
	}
	
	/**
	 * Returns the index of a passed card in the card list
	 * @param card
	 * @param cardList
	 * @return
	 */
	public int getCardIndex(int[] card, ArrayList<int[]> cardList) {
		int index = -1;
		for(int i = 0;i<cardList.size();i++) {
			int[] listCard = cardList.get(i);
			if(card[0] == listCard[0] && card[1] == listCard[1]) {
				index = i;
			}
		}
		return index;
	}
}
