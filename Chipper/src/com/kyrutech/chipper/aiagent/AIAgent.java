package com.kyrutech.chipper.aiagent;

import java.text.NumberFormat;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.kyrutech.chipper.CardPlayer;
import com.kyrutech.chipper.ChipperEngine;
import com.kyrutech.chipper.Constants;
import com.kyrutech.chipper.gameobjects.Hand;
import com.kyrutech.chipper.gameobjects.Player;
import com.kyrutech.chipper.gameobjects.Trick;

public class AIAgent implements CardPlayer {

	ChipperEngine engine;
	Player player;
	
	float intendedBid = 0;
	int intendedSuit = 0;
	
	ArrayList<int[]> playedCards = new ArrayList<int[]>();
	
	boolean[][] seenCards = new boolean[4][6]; 
	
	boolean debug = false;
	
	public AIAgent(ChipperEngine e, Player p) {
		engine = e;
		player = p;
	}
	
	/**
	 * Checks seen cards to see if card has been seen
	 * @param card
	 * @return
	 */
	private boolean isSeenCard(int[] card) {
		int suitIndex = -1;
		int cardIndex = -1;
		
		suitIndex = card[0];
		switch(card[1]) {
		case Constants.ACE:
			cardIndex = 0;
			break;
		case Constants.KING:
			cardIndex = 1;
			break;
		case Constants.QUEEN:
			cardIndex = 2;
			break;
		case Constants.JACK:
			cardIndex = 3;
			break;
		case Constants.TEN:
			cardIndex = 4;
			break;
		case Constants.NINE:
			cardIndex = 5;
			break;
		}
		
		return seenCards[suitIndex][cardIndex];
	}
	
	/**
	 * Resets to no seen cards
	 */
	public void resetSeenCards() {
		for(int s = 0;s<seenCards.length;s++) {
			for(int v = 0;v<seenCards[s].length;v++) {
				seenCards[s][v] = false;
			}
		}
	}
	
	/**
	 * Adds the passed card to the seen cards array
	 * @param card
	 */
	public void addSeenCard(int[] card) {
		int suitIndex = -1;
		int cardIndex = -1;
		
		suitIndex = card[0];
		switch(card[1]) {
		case Constants.ACE:
			cardIndex = 0;
			break;
		case Constants.KING:
			cardIndex = 1;
			break;
		case Constants.QUEEN:
			cardIndex = 2;
			break;
		case Constants.JACK:
			cardIndex = 3;
			break;
		case Constants.TEN:
			cardIndex = 4;
			break;
		case Constants.NINE:
			cardIndex = 5;
			break;
		}
		seenCards[suitIndex][cardIndex] = true;
	}
	
	/**
	 * Determines if the player should stay
	 */
	public boolean staying(int bid, int suit) {
		boolean stay = false;
		
		//Automatically stays on NO TRUMP or CHIPPER
		if(suit == Constants.NO_TRUMP) {
			return true;
		}
		if(bid == Constants.BID_CHIPPER) {
			return true;
		}
		
		boolean[] trump = trumpInHand(player.getHand(), suit);
		if(trump[0]) {  //Always stay if we have the right bower
			return true;
		}
		
		float stayable = 0;
		for(boolean t : trump) {
			if(t) stayable++;
		}
		
		for(int[] card : player.getHand().getCards()) {
			if(card[0] != suit && card[1] == Constants.ACE) {
				stayable++;
			}
		}
		
		if(bid == Constants.BID_TWO) {
			if(stayable >= 2) {
				return true;
			}
		} else if(bid == Constants.BID_THREE) {
			if(stayable >= 2) {
				return true;
			}
		} else if(bid == Constants.BID_FOUR) {
			if(stayable >= 2) {
				return true;
			}
		} else if(bid == Constants.BID_FIVE) {
			if(stayable >= 3) {
				return true;
			}
		} else if(bid == Constants.BID_SIX) {
			if(stayable >= 3) {
				return true;
			}
		}
		
		
		return stay;
	}
	
	/**
	 * Returns a boolean array indicating what trump is in the players hand
	 * @param hand
	 * @param suit
	 * @return
	 */
	private static boolean[] trumpInHand(Hand hand, int suit) {
		boolean[] trump = new boolean[7];
		for(int[] card : hand.getCards()) {
			if(card[0] == suit) {
				switch(card[1]) { 
				case Constants.JACK:
					trump[0] = true;
					break;
				case Constants.ACE:
					trump[2] = true;
					break;
				case Constants.KING:
					trump[3] = true;
					break;
				case Constants.QUEEN:
					trump[4] = true;
					break;
				case Constants.TEN:
					trump[5] = true;
					break;
				case Constants.NINE:
					trump[6] = true;
					break;
				}
			}
			//Left
			if(card[1] == Constants.JACK) {
				if(suit== Constants.HEARTS && card[0] == Constants.DIAMONDS) {
					trump[1] = true;
				} else if(suit == Constants.DIAMONDS && card[0] == Constants.HEARTS) {
					trump[1] = true;
				} else if(suit == Constants.SPADES && card[0] == Constants.CLUBS) {
					trump[1] = true;
				} else if(suit == Constants.CLUBS && card[0] == Constants.SPADES) {
					trump[1] = true;
				}
			}
		}
		
		return trump;
	}
	
	/**
	 * Reviews cards and determines what to bid for this AI
	 */
	public void determineBid() {
		Hand hand = player.getHand();
		
		float[] bids = AIAgent.getBids(hand);


		float[] finalBid = new float[2];
		for(int i = 0;i<5;i++) {
			StringBuilder sb = new StringBuilder();
			switch(i) {
			case Constants.HEARTS:
				sb.append("Hearts:");
				break;
			case Constants.DIAMONDS:
				sb.append("Diamonds:");
				break;
			case Constants.CLUBS:
				sb.append("Clubs:");
				break;
			case Constants.SPADES:
				sb.append("Spades:");
				break;
			case Constants.NO_TRUMP:
				sb.append("No Trump:");
				break;
			}
			sb.append(NumberFormat.getInstance().format(bids[i]));

//			Gdx.app.log("BIDDING", sb.toString());
			if(bids[i] > finalBid[1]) {
				finalBid = new float[] {i, bids[i]};
			}
 		}
		
		intendedSuit = (int) finalBid[0];
		intendedBid = finalBid[1];

	}
	
	/**
	 * Reviews cards and determines what to bid
	 */
	public static float[] getBids(Hand passedHand) {
		Hand hand = passedHand;

		float[] bids = new float[5];

		//Cycle through suits
		for(int i = 0;i<4;i++) {
			boolean trump[] = trumpInHand(hand, i);
			boolean aceheart = false;
			boolean kingheart = false;
			boolean acediamond = false;
			boolean kingdiamond = false;
			boolean acespade = false;
			boolean kingspade = false;
			boolean aceclub = false;
			boolean kingclub = false;
			//Check cards
			for(int[] card : hand.getCards()) {
				if(card[1] == Constants.ACE && card[0] != i) {
					switch(card[0]) {
					case Constants.HEARTS:
						aceheart = true;
						break;
					case Constants.DIAMONDS:
						acediamond = true;
						break;
					case Constants.SPADES:
						acespade = true;
						break;
					case Constants.CLUBS:
						aceclub = true;
						break;
					}
				}
				if(card[1] == Constants.KING && card[0] != i) {
					switch(card[0]) {
					case Constants.HEARTS:
						kingheart = true;
						break;
					case Constants.DIAMONDS:
						kingdiamond = true;
						break;
					case Constants.SPADES:
						kingspade = true;
						break;
					case Constants.CLUBS:
						kingclub = true;
						break;
					}
				}
			}
			float bonus = 0.0f;
			float bid = 0.0f;
			if(trump[0]) {
				bid+=1;
				bonus+=0.30;
			}
			if(trump[1]) {
				bid+=0.75+bonus;
				bonus+=0.20;
			}
			if(!trump[0] && !trump[1]) {  //Neither jack, bad bid
				bid-=1;
			}
			if(trump[2]) {
				bid+=0.60+bonus;
				bonus+=0.15;
			}
			if(trump[3]) {
				bid+=0.50+bonus;
				bonus+=0.10;
			}
			if(trump[4]) {
				bid+=0.40+bonus;
				bonus+=0.10;
			}
			if(trump[5]) {
				bid+=0.30+bonus;
				bonus+=0.10;
			}
			if(trump[6]) {
				bid+=0.20+bonus;
			}

			bonus = 0.0f;
			if(aceheart) {
				bid+=1;
				bonus=0.4f;
			}
			if(kingheart) {
				bid+=0.2f+bonus;
			}
			bonus = 0.0f;
			if(acediamond) {
				bid+=1;
				bonus=0.4f;
			}
			if(kingdiamond) {
				bid+=0.2f+bonus;
			}
			bonus = 0.0f;
			if(acespade) {
				bid+=1;
				bonus=0.4f;
			}
			if(kingspade) {
				bid+=0.2f+bonus;					
			}
			bonus = 0.0f;
			if(aceclub) {
				bid+=1;
				bonus=0.4f;
			}
			if(kingclub) {
				bid+=0.2f+bonus;
			}
			bids[i] = bid;
		}

		ArrayList<float[]> noTrump = new ArrayList<float[]>();
		boolean[][] suits = new boolean[4][6];
		for(int[] card : hand.getCards()) {  //Determine which cards for which suits we have
			switch(card[1]) {
			case Constants.ACE:
				suits[card[0]][0] = true;
				break;
			case Constants.KING:
				suits[card[0]][1] = true;
				break;
			case Constants.QUEEN:
				suits[card[0]][2] = true;
				break;
			case Constants.JACK:
				suits[card[0]][3] = true;
				break;
			case Constants.TEN:
				suits[card[0]][4] = true;
				break;
			case Constants.NINE:
				suits[card[0]][5] = true;
				break;
			}
		}
		for(int i = 0;i<4;i++) {
			float bid = 0.0f;
			float bonus = 0.0f;
			if(suits[i][0]) {  //Ace
				bid+=1;
				bonus+=0.4;
				if(suits[i][1]) {  //King
					bid+=1;
					bonus+=0.2; //Cut in half
				}
				if(suits[i][2]) { //Queen
					bid+=0.2+bonus; //Cut in half
				}
				if(suits[i][3]) {  //Jack
					bid+=0.1+bonus;
				}
				if(suits[i][4]) { //Ten
					bid+=0.0+bonus;
				}
				if(suits[i][5]) { //Nine
					bid+=0.0+bonus;
				}
				noTrump.add(new float[] {i, bid});
			}
		}

		if(noTrump.size() > 1) { 
			float total = 0;
			if(noTrump.size() == 2) { //If only 2 Aces, pull 1 off the bid total
				total = -1;
			}
			for(float[] bid: noTrump) { //Add up all the no trump bid values
				total += bid[1];
			}
			if(total >= 3) { //Must be at least 3 to bid no trump
				bids[4] = total;
			}
		}

//		for(int i = 0;i<5;i++) {
//			StringBuilder sb = new StringBuilder();
//			switch(i) {
//			case Constants.HEARTS:
//				sb.append("Hearts:");
//				break;
//			case Constants.DIAMONDS:
//				sb.append("Diamonds:");
//				break;
//			case Constants.CLUBS:
//				sb.append("Clubs:");
//				break;
//			case Constants.SPADES:
//				sb.append("Spades:");
//				break;
//			case Constants.NO_TRUMP:
//				sb.append("No Trump:");
//				break;
//			}
//			sb.append(NumberFormat.getInstance().format(bids[i]));
//
//			Gdx.app.log("BIDDING", sb.toString());
// 		}
				
		return bids;

	}
	
	/**
	 * Returns the bid number
	 * @return
	 */
	public float getBid() {
		return intendedBid;
	}
	
	/**
	 * Returns the bid suit
	 * @return
	 */
	public int getSuit() {
		return intendedSuit;
	}
	
	/**
	 * Returns the hand index of the card that the player should lead
	 * 
	 * @return
	 */
	public int getCardToLead() {
		int cardIndex = -1;
		int bidSuit = engine.getWinningBidderPlayer().getBidSuit();
		
		if(bidSuit == Constants.HEARTS) {
			cardIndex = getLeadCardIndex(bidSuit);
		} else if(bidSuit == Constants.DIAMONDS) {
			cardIndex = getLeadCardIndex(bidSuit);
		} else if(bidSuit == Constants.CLUBS) {
			cardIndex = getLeadCardIndex(bidSuit);
		} else if(bidSuit == Constants.SPADES) {
			cardIndex = getLeadCardIndex(bidSuit);
		} else if(bidSuit == Constants.NO_TRUMP) {
			int[] noTrump = ChipperEngine.getNoTrumpTrump();
			//Do we have an Ace?
			int aceHearts = getCardIndexInHand(Constants.HEARTS, ChipperEngine.TRUMP_ACE);
			int aceDiamonds = getCardIndexInHand(Constants.DIAMONDS, ChipperEngine.TRUMP_ACE);
			int aceClubs = getCardIndexInHand(Constants.CLUBS, ChipperEngine.TRUMP_ACE);
			int aceSpades = getCardIndexInHand(Constants.SPADES, ChipperEngine.TRUMP_ACE);
			if(aceHearts != -1) {//YES - Play It
				debug("Play Ace");
				cardIndex = aceHearts;
			} else if(aceDiamonds != -1) {
				debug("Play Ace");
				cardIndex = aceDiamonds;
			} else if(aceClubs != -1) {
				debug("Play Ace");
				cardIndex = aceClubs;
			} else if(aceSpades != -1) {
				debug("Play Ace");
				cardIndex = aceSpades;
			} else {
				debug("No Ace");
				//NO - Do we have a high card?
				int highCardIndex = -1;
				for(int[] card : player.getHand().getCards()) {
					int noTrumpIndex = -1;
					for(int i = 0;i<noTrump.length;i++) {
						if(card[1] == noTrump[i]) {
							noTrumpIndex = i;
						}						
					}
					boolean highest = true;
					for(int i = 0;i<noTrumpIndex;i++) {
						if(!isSeenCard(new int[] {card[0], noTrump[i]})) {
							highest = false;
						}
					}
					if(highest) {
						highCardIndex = player.getHand().getCards().indexOf(card);
					}
				}				
				if(highCardIndex != -1) {
					debug("Found a high card, play it");
					//YES - Play it
					cardIndex = highCardIndex;
				} else {
					debug("No high card");
					//NO - Do we have any doubles?
					int doubleSuit = getDoubleSuit(player.getHand().getCards());
					if(doubleSuit != -1) {
						debug("Found a double, play lower part - " + doubleSuit);
						//YES - Play low card of double
						ArrayList<int[]> cardsOfSuit = new ArrayList<int[]>();
						for(int[] card : player.getHand().getCards()) {
							if(card[0] == doubleSuit) {
								cardsOfSuit.add(card);
							}
						}						
						cardIndex = getLowestValueCardIndex(cardsOfSuit);
					} else {
						debug("No double, play lowest value card");
						//NO - Play lowest card
						cardIndex = getLowestValueCardIndex();
					}
				}
			}
		}
		try {
			playedCards.add(player.getHand().getCards().get(cardIndex));
		} catch(ArrayIndexOutOfBoundsException e) {
			for(int[] card : player.getHand().getCards()) {
//				Gdx.app.log("PLAYERCARDS", engine.getCardString(card));
			}
			e.printStackTrace();
		}
		
		return cardIndex;
	}

	/**
	 * Gets the index of the card to lead for suited s
	 * 
	 * @param bidSuit
	 * @return
	 */
	private int getLeadCardIndex(int bidSuit) {
		int cardIndex = -1;
		ArrayList<int[]> trumpList = null;
		
		if(bidSuit == Constants.HEARTS) {
			trumpList = ChipperEngine.getHeartsTrump();
		} else if(bidSuit == Constants.DIAMONDS) {
			trumpList = ChipperEngine.getDiamondsTrump();
		} else if(bidSuit == Constants.SPADES) {
			trumpList = ChipperEngine.getSpadesTrump();
		} else if(bidSuit == Constants.CLUBS) {
			trumpList = ChipperEngine.getClubsTrump();
		}
		if(playedCards.size() == 0) {			//Played Cards = 0
			debug("No played cards");
			//Do we have the right?
			int rightIndex = getCardIndexInHand(bidSuit, ChipperEngine.TRUMP_RIGHT);
			if(rightIndex != -1) {	//YES - Play It
				debug("Play the right");
				cardIndex = rightIndex;
			} else { 	//NO - Do we have an Ace?
				debug("No right bower");
				int aceHearts = bidSuit == Constants.HEARTS ? -1 : getCardIndex(new int[] {Constants.HEARTS, Constants.ACE}, player.getHand().getCards());
				int aceDiamond = bidSuit == Constants.DIAMONDS ? -1 : getCardIndex(new int[] {Constants.DIAMONDS, Constants.ACE}, player.getHand().getCards());
				int aceClubs = bidSuit == Constants.CLUBS ? -1 : getCardIndex(new int[] {Constants.CLUBS, Constants.ACE}, player.getHand().getCards());
				int aceSpades = bidSuit == Constants.SPADES? -1 : getCardIndex(new int[] {Constants.SPADES, Constants.ACE}, player.getHand().getCards());
				if(aceDiamond != -1) { //YES - Play it
					debug("Play Ace");
					cardIndex = aceDiamond;
				} else if(aceHearts != -1) {
					debug("Play Ace");
					cardIndex = aceHearts;
				} else if(aceClubs != -1) {
					debug("Play Ace");
					cardIndex = aceClubs;
				} else if(aceSpades != -1) {
					debug("Play Ace");
					cardIndex = aceSpades;
				} else { //NO - Play second highest trump we have
					debug("No Ace");
					ArrayList<int[]> trumpInHand = getTrumpCardsInHand(player.getHand().getCards(), trumpList);
					if(trumpInHand.size() < 2) { //If less then 2 trump in hand and no Aces I don't know why we are here, play random card
						debug("Play random card in hand");
						cardIndex = (int) (Math.random()*player.getHand().getCards().size());
					} else {
						debug("More than 2 trump in hand, play 2nd highest");
						//Find the highest trump and remove it
						int highestIndex = findHighestTrumpInCards(trumpInHand, trumpList);
						trumpInHand.remove(highestIndex);		
						//Find remaining highest trump and play it
						int nextHighestIndex = findHighestTrumpInCards(trumpInHand, trumpList);
						cardIndex = player.getHand().getCards().indexOf(trumpInHand.get(nextHighestIndex));
					}
				}
			}
		//Else
		} else {
			//Are we winning bidder?
			if(engine.getWinningBidder() == player.getId()) {
				debug("We are winning bidder");
				//YES - Have we made our bid?
				if(player.getWonTricks().size() >= player.getBidValue()) {
					debug("We have made our bid");
					//YES - Do we have a high trump to play?
					ArrayList<int[]> trumpInHand = this.getTrumpCardsInHand(player.getHand().getCards(), trumpList);
					int highestIndex = findHighestTrumpInCards(trumpInHand, trumpList);
					if(highestIndex != -1 && isHighestUnplayedTrump(trumpInHand.get(highestIndex), trumpList)) {
						debug("Play high trump");
						//YES - Play it
						cardIndex = player.getHand().getCards().indexOf(trumpInHand.get(highestIndex));
					} else {
						debug("No high trump");
						//NO - Do we have an Ace?
						int aceHearts = bidSuit == Constants.HEARTS ? -1 : getCardIndex(new int[] {Constants.HEARTS, Constants.ACE}, player.getHand().getCards());
						int aceDiamond = bidSuit == Constants.DIAMONDS ? -1 : getCardIndex(new int[] {Constants.DIAMONDS, Constants.ACE}, player.getHand().getCards());
						int aceClubs = bidSuit == Constants.CLUBS ? -1 : getCardIndex(new int[] {Constants.CLUBS, Constants.ACE}, player.getHand().getCards());
						int aceSpades = bidSuit == Constants.SPADES? -1 : getCardIndex(new int[] {Constants.SPADES, Constants.ACE}, player.getHand().getCards());
						if(aceDiamond != -1) { //YES - Play it
							debug("Play Ace");
							cardIndex = aceDiamond;
						} else if(aceHearts != -1) {
							debug("Play Ace");
							cardIndex = aceHearts;
						} else if(aceClubs != -1) {
							debug("Play Ace");
							cardIndex = aceClubs;
						} else if(aceSpades != -1) {
							debug("Play Ace");
							cardIndex = aceSpades;
						} else {
							debug("Play highest non trump card");
							//NO - Play highest other card
							cardIndex = getHighestNonTrump(player.getHand().getCards(), trumpList);								
						}
					}
				} else {
					debug("We have not yet made our bid");
					//NO - Do we have a high trump to play?
					ArrayList<int[]> trumpInHand = this.getTrumpCardsInHand(player.getHand().getCards(), trumpList);
					int highestIndex = findHighestTrumpInCards(trumpInHand, trumpList);
					if(highestIndex != -1 && isHighestUnplayedTrump(trumpInHand.get(highestIndex), trumpList)) {
						debug("Play high trump");
						//YES - Play it
						cardIndex = player.getHand().getCards().indexOf(trumpInHand.get(highestIndex));
					} else {
						debug("No high trump");
						//NO - Do we have an Ace?								
						int aceHearts = bidSuit == Constants.HEARTS ? -1 : getCardIndex(new int[] {Constants.HEARTS, Constants.ACE}, player.getHand().getCards());
						int aceDiamond = bidSuit == Constants.DIAMONDS ? -1 : getCardIndex(new int[] {Constants.DIAMONDS, Constants.ACE}, player.getHand().getCards());
						int aceClubs = bidSuit == Constants.CLUBS ? -1 : getCardIndex(new int[] {Constants.CLUBS, Constants.ACE}, player.getHand().getCards());
						int aceSpades = bidSuit == Constants.SPADES? -1 : getCardIndex(new int[] {Constants.SPADES, Constants.ACE}, player.getHand().getCards());
						if(aceDiamond != -1) { //YES - Play it
							debug("Play Ace");
							cardIndex = aceDiamond;
						} else if(aceHearts != -1) {
							debug("Play Ace");
							cardIndex = aceHearts;
						} else if(aceClubs != -1) {
							debug("Play Ace");
							cardIndex = aceClubs;
						} else if(aceSpades != -1) {
							debug("Play Ace");
							cardIndex = aceSpades;
						} else {
							//NO - Do we have more then 1 trump?
							if(trumpInHand.size() > 1) {
								debug("Multiple trump in hand, play lowest");
								//YES - Play lower
								cardIndex = this.getLowestTrumpCardIndex(trumpInHand, trumpList);
							} else {					
								debug("1 or fewer trump");
								debug("Play highest non trump card");
								//NO - Play highest other card
								cardIndex = getHighestNonTrump(player.getHand().getCards(), trumpList);
							}
						}
					}
				}
			} else {
				debug("We are not winning bidder");
				//NO - Do we have a high trump to play?
				ArrayList<int[]> trumpInHand = this.getTrumpCardsInHand(player.getHand().getCards(), trumpList);
				int highestIndex = findHighestTrumpInCards(trumpInHand, trumpList);
				if(highestIndex != -1 && isHighestUnplayedTrump(trumpInHand.get(highestIndex), trumpList)) {
					debug("Play high trump");
					//YES - Play it
					cardIndex = player.getHand().getCards().indexOf(trumpInHand.get(highestIndex));
				} else {
					debug("No high trump");
					//NO - Do we have an Ace?
					int aceHearts = bidSuit == Constants.HEARTS ? -1 : getCardIndex(new int[] {Constants.HEARTS, Constants.ACE}, player.getHand().getCards());
					int aceDiamond = bidSuit == Constants.DIAMONDS ? -1 : getCardIndex(new int[] {Constants.DIAMONDS, Constants.ACE}, player.getHand().getCards());
					int aceClubs = bidSuit == Constants.CLUBS ? -1 : getCardIndex(new int[] {Constants.CLUBS, Constants.ACE}, player.getHand().getCards());
					int aceSpades = bidSuit == Constants.SPADES? -1 : getCardIndex(new int[] {Constants.SPADES, Constants.ACE}, player.getHand().getCards());
					if(aceDiamond != -1) { //YES - Play it
						debug("Play Ace");
						cardIndex = aceDiamond;
					} else if(aceHearts != -1) {
						debug("Play Ace");
						cardIndex = aceHearts;
					} else if(aceClubs != -1) {
						debug("Play Ace");
						cardIndex = aceClubs;
					} else if(aceSpades != -1) {
						debug("Play Ace");
						cardIndex = aceSpades;
					} else {
						debug("Play highest non trump card");
						//NO - Play highest other card
						cardIndex = getHighestNonTrump(player.getHand().getCards(), trumpList);								
					}
				}
			}
		}
		return cardIndex;
	}

	/**
	 * Returns the first suit it finds doubles for
	 * @param cards
	 * @return
	 */
	private int getDoubleSuit(ArrayList<int[]> cards) {
		int[] suits = new int[4];
		
		for(int[] card : cards) {
			suits[card[0]]++;
		}
		
		for(int i = 0;i<suits.length;i++) {
			if(suits[i] > 1) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Gets highest value non trump card in hand
	 * @param cards
	 * @param trumpList
	 * @return
	 */
	private int getHighestNonTrump(ArrayList<int[]> cards, ArrayList<int[]> trumpList) {
		int highestNoTrumpIndex = 0;
		int handIndex = -1;
		int[] noTrump = ChipperEngine.getNoTrumpTrump();
		for(int[] card : cards) {
			if(trumpList.indexOf(card) == -1) {
				int noTrumpIndex = -1;
				for(int i = 0;i<noTrump.length;i++) {
					if(card[1] == noTrump[i]) {
						noTrumpIndex = i;
					}
				}
				if(noTrumpIndex < highestNoTrumpIndex || handIndex == -1) {
					highestNoTrumpIndex = noTrumpIndex;
					handIndex = cards.indexOf(card);
				}						
			}
		}
		return handIndex;
	}

	/**
	 * Checks seen cards to see if this is the highest trump available
	 * @param card
	 * @param heartsTrump
	 * @return
	 */
	private boolean isHighestUnplayedTrump(int[] card, ArrayList<int[]> trumpList) {
		int cardIndex = getCardIndex(card, trumpList);
		boolean highest = true;
		for(int i = 0;i<cardIndex;i++) {
			if(!isSeenCard(trumpList.get(i))) {
				highest = false;
			}
		}		
		return highest;
	}

	/**
	 * Finds the best trump card in list of passed cards
	 * @param trumpInHand
	 * @param trumpList
	 * @return
	 */
	private int findHighestTrumpInCards(ArrayList<int[]> trumpInHand, ArrayList<int[]> trumpList) {
		int highestIndex = -1;
		for(int[] card : trumpInHand) {
			if(highestIndex == -1) {
				highestIndex = getCardIndex(card, trumpInHand);
			} else {
				if(getCardIndex(card, trumpList) < getCardIndex(trumpInHand.get(highestIndex), trumpList)) {
					highestIndex = trumpInHand.indexOf(card);
				}
			}
		}
		return highestIndex;
	}
	
	/**
	 * Checks to see if we have the card in the suit passed in our hand and returns the index
	 * @param bidSuit
	 * @param trumpRight
	 * @return
	 */
	private int getCardIndexInHand(int bidSuit, int cardInTrump) {
		int index = -1;
		int[] cardToFind = null;
		
		if(bidSuit == Constants.HEARTS) {
			cardToFind = ChipperEngine.getHeartsTrump().get(cardInTrump);
		} else if(bidSuit == Constants.DIAMONDS) {
			cardToFind = ChipperEngine.getDiamondsTrump().get(cardInTrump);
		} else if(bidSuit == Constants.CLUBS) {
			cardToFind = ChipperEngine.getClubsTrump().get(cardInTrump);
		} else if(bidSuit == Constants.SPADES) {
			cardToFind = ChipperEngine.getSpadesTrump().get(cardInTrump);
		}
		
		for(int card[] : player.getHand().getCards()) {
			if(card[0] == cardToFind[0] && card[1] == cardToFind[1]) {
				index = player.getHand().getCards().indexOf(card);
			}
		}
		
		return index;
	}

	/**
	 * Returns the current index of the card to play for this player
	 * @return
	 */
	public int getCardToPlay() {
		int returnIndex = -1;
		Trick currentTrick = engine.getCurrentTrick();
		int bidSuit = engine.getWinningBidderPlayer().getBidSuit();
		
		int[] winningCard = currentTrick.getWinningCard(bidSuit);
		int[] leadCard = currentTrick.getPlayerCard(currentTrick.getLeadPlayer());
		
		if(bidSuit == Constants.HEARTS) {
			returnIndex = getCardToPlayIndex(bidSuit, winningCard, leadCard);
		} else if(bidSuit == Constants.DIAMONDS) {
			returnIndex = getCardToPlayIndex(bidSuit, winningCard, leadCard);
		} else if(bidSuit == Constants.CLUBS) {
			returnIndex = getCardToPlayIndex(bidSuit, winningCard, leadCard);
		} else if(bidSuit == Constants.SPADES) {
			returnIndex = getCardToPlayIndex(bidSuit, winningCard, leadCard);
		} else {  //No trump
			//See if we can follow suit
			ArrayList<int[]> suitInHand = getCardsMatchingSuitInHand(winningCard);
			if(suitInHand.size() == 0) { //Nothing to follow suit, find a card
				returnIndex = getLowestValueCardIndex();
			} else if(suitInHand.size() == 1) { //Only 1 to follow suit, play it
				returnIndex = player.getHand().getCards().indexOf(suitInHand.get(0));
			} else { //Multiple cards in suit
				returnIndex = getNonTrumpPlayableIndex(winningCard, suitInHand);
			}
		}
		
		playedCards.add(player.getHand().getCards().get(returnIndex));
		return returnIndex;
	}

	
	/**
	 * Gets the index of the card to play for suited bids
	 * @param bidSuit
	 * @param winningCard
	 * @param leadCard 
	 * @return
	 */
	private int getCardToPlayIndex(int bidSuit, int[] winningCard, int[] leadCard) {
		int returnIndex = -1;
		ArrayList<int[]> trumpList = null;
		if(bidSuit == Constants.HEARTS) {
			trumpList = ChipperEngine.getHeartsTrump();
		} else if(bidSuit == Constants.DIAMONDS) {
			trumpList = ChipperEngine.getDiamondsTrump();
		} else if(bidSuit == Constants.CLUBS) {
			trumpList = ChipperEngine.getClubsTrump();
		} else if(bidSuit == Constants.SPADES) {
			trumpList = ChipperEngine.getSpadesTrump();
		}
		if(getCardIndex(leadCard, trumpList) > -1) {
			//Trump
			ArrayList<int[]> trumpCards = getTrumpCardsInHand(player.getHand().getCards(), trumpList);

			if(trumpCards.size() == 0) { //Can't play trump, find lowest value card to play					
				returnIndex = getLowestValueCardIndex();						
			} else if(trumpCards.size() == 1) { //Only 1 trump, must play it
				returnIndex = getCardIndex(trumpCards.get(0), player.getHand().getCards());
			} else { //Multiple trump
				int winIndex = getCardIndex(winningCard, trumpList);
				boolean canTake = false;
				for(int[] card : trumpCards) {
					if(winIndex > getCardIndex(card, trumpList)) { //Can we take the trick?
						canTake = true;
					}
				}
				if(canTake) { //Find lowest card that can take the trick and play it
					returnIndex = getLowestWinningTrumpIndex(trumpCards, winIndex, trumpList);
				} else { //Find lowest trump and play it
					returnIndex = getLowestTrumpCardIndex(trumpCards, trumpList);
				}
			}
		} else {
			//Non trump
			//See if we can follow suit
			ArrayList<int[]> suitInHand = getCardsMatchingSuitInHand(leadCard, trumpList);
			if(suitInHand.size() == 0) { //Nothing to follow suit, find a card
				//See if we have trump to play
				ArrayList<int[]> trumpInHand = getTrumpCardsInHand(player.getHand().getCards(), trumpList);
				if(trumpInHand.size() == 0) { 
					returnIndex = getLowestValueCardIndex();
				} else if(trumpInHand.size() == 1) { //Only one trump, does it currently win hand
					int winIndex = getCardIndex(winningCard, trumpList);
					if(winIndex == -1 || winIndex > getCardIndex(trumpInHand.get(0), trumpList)) { //Wins hand
						returnIndex = getCardIndex(trumpInHand.get(0), player.getHand().getCards());	
					} else {
						returnIndex = getLowestValueCardIndex();
					}					
				} else { //Find lowest trump and play it
					int winIndex = getCardIndex(winningCard, trumpList);
					if(winIndex == -1) { //Winning card isn't trump, play lowest trump
						returnIndex = getLowestTrumpCardIndex(trumpInHand, trumpList);
					} else { //Winning card is a trump, can we beat it?
						boolean canTake = false;
						for(int[] card : trumpInHand) {
							if(winIndex > getCardIndex(card, trumpList)) {
								canTake = true;
							}
						}
						if(canTake) { //Find lowest card that can take the trick and play it
							returnIndex = getLowestWinningTrumpIndex(trumpInHand, winIndex, trumpList);
						} else { //Find lowest card and play it
							returnIndex = getLowestValueCardIndex();
						}
					}
				}
			} else if(suitInHand.size() == 1) { //Only 1 to follow suit, play it
				returnIndex = getCardIndex(suitInHand.get(0), player.getHand().getCards());
			} else { //Multiple cards in suit
				returnIndex = getNonTrumpPlayableIndex(leadCard, suitInHand);
			}
		}
		return returnIndex;
	}

	/**
	 * Returns list of cards that match suit of passed card, for no trump
	 * @param winningCard
	 * @return
	 */
	private ArrayList<int[]> getCardsMatchingSuitInHand(int[] winningCard) {
		ArrayList<int[]> suitInHand = new ArrayList<int[]>();
		for(int[] card : player.getHand().getCards()) {
			if(winningCard[0] == card[0]) {
				suitInHand.add(card);
			}
		}
		return suitInHand;
	}
	
	/**
	 * Returns list of cards that match suit of passed card
	 * Check against trumpList to be sure we aren't trying to play the left
	 * @param winningCard
	 * @param trumpList
	 * @return
	 */
	private ArrayList<int[]> getCardsMatchingSuitInHand(int[] winningCard, ArrayList<int[]> trumpList) {
		ArrayList<int[]> suitInHand = new ArrayList<int[]>();
		for(int[] card : player.getHand().getCards()) {
			if(winningCard[0] == card[0] && getCardIndex(card, trumpList) == -1) {
				suitInHand.add(card);
			}
		}
		return suitInHand;
	}

	/**
	 * Find card to play following suit from multiple options.  
	 * 
	 * @param winningCard
	 * @param suitInHand
	 * @return
	 */
	private int getNonTrumpPlayableIndex(int[] winningCard, ArrayList<int[]> suitInHand) {
		boolean canTake = false;
		int winningIndex = -1;
		int[] noTrump = ChipperEngine.getNoTrumpTrump();
		for(int i = 0;i<noTrump.length;i++) {   //Get value of winning card
			if(noTrump[i] == winningCard[1]) {
				winningIndex = i;
			}
		}
		for(int[] card : suitInHand) {  //Check if we have a card of greater value
			for(int i = 0;i<noTrump.length;i++) {
				if(noTrump[i] == card[1]) {
					if(winningIndex > i) {
						canTake = true;
					}
				}
			}
		}

		int noTrumpIndex = 99;
		int suitInHandIndex = -1;
		if(canTake) { //Find highest card that will take the trick and play it
			for(int [] card : suitInHand) {  
				for(int i = 0;i<noTrump.length;i++) {
					if(noTrump[i] == card[1]) {
						if(i < winningIndex && i < noTrumpIndex) {
							noTrumpIndex = i;
							suitInHandIndex = suitInHand.indexOf(card);
						}
					}
				}
			}
			
		} else {  //Find lowest suit card and play it
			noTrumpIndex = -1; //Needs to be reset low to find lowest card
			for(int [] card : suitInHand) {
				for(int i = 0;i<noTrump.length;i++) {
					if(noTrump[i] == card[1]) {
						if(i > noTrumpIndex) {
							noTrumpIndex = i;
							suitInHandIndex = suitInHand.indexOf(card);
						}
					}
				}
			}
		}
		return player.getHand().getCards().indexOf(suitInHand.get(suitInHandIndex));
	}

	/**
	 * Returns a list of trump card in the passed hand
	 * @param cards
	 * @param trumpList
	 * @return
	 */
	private ArrayList<int[]> getTrumpCardsInHand(ArrayList<int[]> cards, ArrayList<int[]> trumpList) {

		ArrayList<int[]> trumpCards = new ArrayList<int[]>();
		for(int[] card : cards) {
			if(getCardIndex(card, trumpList) > -1) {
				trumpCards.add(card);
			}
		}
		return trumpCards;
	}

	/**
	 * Returns index of the lowest trump that is bidder then the winIndex
	 * @param cardList
	 * @param winIndex
	 * @param trumpList
	 * @return
	 */
	private int getLowestWinningTrumpIndex(ArrayList<int[]> cardList, int winIndex, ArrayList<int[]> trumpList) {
		int trumpIndex = -1;
		int possibleIndex = -1;
		for(int [] card : cardList) {  
			if(getCardIndex(card, trumpList) > trumpIndex && getCardIndex(card, trumpList) < winIndex) {
				trumpIndex = getCardIndex(card, trumpList);
				possibleIndex = cardList.indexOf(card);
			}
		}
		return player.getHand().getCards().indexOf(cardList.get(possibleIndex));
	}

	/**
	 * Returns the index of the lowest trump in passed list
	 * @param trumpInHand
	 * @param trumpList
	 * @return
	 */
	private int getLowestTrumpCardIndex(ArrayList<int[]> cardList, ArrayList<int[]> trumpList) {
		int trumpIndex = -1;
		int possibleIndex = -1;
		for(int[] card : cardList) {
			if(getCardIndex(card, trumpList) > trumpIndex) {
				trumpIndex = getCardIndex(card, trumpList);
				possibleIndex = cardList.indexOf(card);
			}
		}
		return player.getHand().getCards().indexOf(cardList.get(possibleIndex));
	}

	/**
	 * Finds the lowest value card and returns it's index in the players hand
	 * Cards of the same value will result in the first card in the hand of the value being returned.
	 * @return
	 */
	private int getLowestValueCardIndex() {
		int lowestIndex = -1;
		int handIndex = -1;
		int[] noTrump = ChipperEngine.getNoTrumpTrump();
		for(int[] card : player.getHand().getCards()) {
			int noTrumpIndex = -1;
			for(int i = 0;i<noTrump.length;i++) {
				if(card[1] == noTrump[i]) {
					noTrumpIndex = i;
				}
			}
			if(noTrumpIndex > lowestIndex) {
				lowestIndex = noTrumpIndex;
				handIndex = player.getHand().getCards().indexOf(card);
			}						
		}
		return handIndex;
	}
	
	/**
	 * Finds the lowest value card in cards passed and returns it's index in the players hand.
	 * Lowest value has highest index.
	 * @return
	 */
	private int getLowestValueCardIndex(ArrayList<int[]> cards) {
		int lowestIndex = -1;
		int handIndex = -1;
		int[] noTrump = ChipperEngine.getNoTrumpTrump();
		for(int[] card : cards) {
			int noTrumpIndex = -1;
			for(int i = 0;i<noTrump.length;i++) {
				if(card[1] == noTrump[i]) {
					noTrumpIndex = i;
				}
			}
			if(noTrumpIndex > lowestIndex) {
				lowestIndex = noTrumpIndex;
				handIndex = getCardIndex(card, player.getHand().getCards());
			}						
		}
		return handIndex;
	}

	/**
	 * Returns the index of a passed card in the card list
	 * For when indexOf won't work
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
	
	/**
	 * Print out debug message
	 * @param message
	 */
	private void debug(String message) {
		if(debug) {
			Gdx.app.log("AIAGENT", message);
		}
	}
	
}
