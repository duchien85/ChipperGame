package com.kyrutech.chipper.gameobjects;

import java.util.ArrayList;

public class Hand {

	//int[0] = suit
	//int[1] = value
	private ArrayList<int[]> cards = new ArrayList<int[]>();
	
	public Hand() {
		
	}
	
	/**
	 * Adds passed card to cards in hand
	 * @param card
	 */
	public void addCard(int[] card) {
		cards.add(card);
	}

	/**
	 * Swaps the location of two cards
	 * @param selectedCard
	 * @param card
	 */
	public void swapCards(int selectedCard, int card) {
		int[] card1 = cards.get(selectedCard);
		int[] card2 = cards.get(card);
		
		cards.set(selectedCard, card2);
		cards.set(card, card1);
		
	}

	
	public ArrayList<int[]> getCards() {
		return cards;
	}

	public void setCards(ArrayList<int[]> cards) {
		this.cards = cards;
	}

}
