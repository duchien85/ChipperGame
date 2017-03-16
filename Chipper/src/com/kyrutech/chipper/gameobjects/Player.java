package com.kyrutech.chipper.gameobjects;

import java.util.ArrayList;

import com.kyrutech.chipper.Constants;

public class Player {

	private int id = 0;
	private int score = 0;
	private ArrayList<String> scoreHistory = new ArrayList<String>();
	private Hand hand = new Hand();
	private int bidValue = -1;
	private int bidSuit = -1;
	
	private ArrayList<Trick> wonTricks = new ArrayList<Trick>();
	
	public Player(int id) {
		this.setId(id);
	}
	
	/**
	 * Returns full score history score
	 * @return
	 */
	public int getFullHistoryScore() {
		return getHistoryScoreToPoint(scoreHistory.size());
	}
	
	/**
	 * Returns score history up to a specific point
	 * @param numOfScores
	 * @return
	 */
	public int getHistoryScoreToPoint(int numOfScores) {
		int score = 0;
		
		for(int i = 0;i<numOfScores;i++) {
			String thisScore = scoreHistory.get(i);
			
			if("S".equals(thisScore)) {
				score -= 6;
				if(score < 0) {
					score = 0;
				}
			} else if("SS".equals(thisScore)) {
				score -= 12;
				if(score < 0) {
					score = 0;
				}
			} else if("P".equals(thisScore)) {
				//PASS so do nothing
			} else if("C".equals(thisScore)) {
				score+=21; //Chipper, should be a win anyways
			} else {
				score += Integer.parseInt(thisScore);
			}
		}
		
		return score;
	}
	
	/**
	 * Add a new score to history
	 * @param newScore
	 */
	public void updateScoreHistory(String newScore) {
		scoreHistory.add(newScore);
	}
	
	/**
	 * Resets the bids
	 */
	public void resetBid() {
		setBidValue(-1);
		setBidSuit(-1);
	}
	
	/**
	 * Empties the wonTricks arraylist
	 */
	public void resetWonTricks() {
		wonTricks = new ArrayList<Trick>();
	}
	
	/**
	 * Add a trick won
	 * @param won
	 */
	public void addWonTrick(Trick won) {
		wonTricks.add(won);
	}
	
	/**
	 * Adds passed card to hand
	 * @param card
	 */
	public void addCardToHand(int[] card) {
		hand.addCard(card);
	}
	
	/**
	 * Resets the hand to empty
	 */
	public void clearHand() {
		hand = new Hand();
	}
	
	/**
	 * Increases score passed amount
	 * @param increase
	 */
	public void increaseScore(int increase) {
		score += increase;		
	}
	
	/**
	 * Decreases score passed amount
	 * @param decrease
	 */
	public void decreaseScore(int decrease) {
		score -= decrease;
		if(score < 0) {
			score = 0;
		}
	}
	
	/**
	 * Interprets the AIAgent bid value
	 * @param bidValue
	 */
	public void setBidValueInterpreted(float bidValue) {
		if(bidValue < 2) {
			this.bidValue = Constants.BID_PASS;
		} else if(bidValue < 3) {
			this.bidValue = Constants.BID_TWO;
		} else if(bidValue < 4) {
			this.bidValue = Constants.BID_THREE;
		} else if(bidValue < 5) {
			this.bidValue = Constants.BID_FOUR;
		} else if(bidValue < 6) {
			this.bidValue = Constants.BID_FIVE;		
		} else {
			this.bidValue = Constants.BID_CHIPPER;
		}
	}
	
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public Hand getHand() {
		return hand;
	}
	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void setBidValue(int bidValue) {
		this.bidValue = bidValue;
	}

	public int getBidValue() {
		return bidValue;
	}

	public void setBidSuit(int bidSuit) {
		this.bidSuit = bidSuit;
	}

	public int getBidSuit() {
		return bidSuit;
	}

	public void setWonTricks(ArrayList<Trick> wonTricks) {
		this.wonTricks = wonTricks;
	}

	public ArrayList<Trick> getWonTricks() {
		return wonTricks;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void newHand() {
		// TODO Auto-generated method stub
		
	}

	public void setScoreHistory(ArrayList<String> scoreHistory) {
		this.scoreHistory = scoreHistory;
	}

	public ArrayList<String> getScoreHistory() {
		return scoreHistory;
	}

}
