package com.kyrutech.chipper;

import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.Gdx;
import com.kyrutech.chipper.aiagent.AIAgent;
import com.kyrutech.chipper.gameobjects.Player;
import com.kyrutech.chipper.gameobjects.Trick;

public class ChipperEngine {

	private ArrayList<int[]> deck = new ArrayList<int[]>();
	private Player players[] = new Player[4];

	private int dealer = 0;
	private int playLead = 0;
	private int currentPlayer = 0;
	private int currentBidder = 1;
	private int currentStayer = 0;
	private int winningBidder = -1;
	
	private int selectedCard = -1;

	//player0 = user
	private AIAgent player1;
	private AIAgent player2;
	private AIAgent player3;

	public static final int WINNING_SCORE = 21;
	
	public static final int PLAYSTATE_DEAL = 0;
	public static final int PLAYSTATE_BID_NUMBER = 1;
	public static final int PLAYSTATE_BID_WON = 2;
	public static final int PLAYSTATE_STAY = 3;
	public static final int PLAYSTATE_PLAY = 4;
	public static final int PLAYSTATE_OVER = 5;
	public static final int PLAYSTATE_GAMEOVER = 6;
	public static final int PLAYSTATE_NO_BID = 7;

	private int playstate = PLAYSTATE_DEAL;

	public static final int GAMESTATE_SETUP = 0;
	public static final int GAMESTATE_PLAY = 1;
	public static final int GAMESTATE_POST = 2;

	private int gamestate = GAMESTATE_SETUP;

	private float bidTimer = 0;
	private float stayTimer = 0;
	private float playTimer = 0;

	private int staying[] = new int[4];

	public static final int STAYING_WAITING = 0;
	public static final int STAYING_STAY = 1;
	public static final int STAYING_OUT = 2;

	private Trick currentTrick;
	private Trick lastTrick;

	public static final int TRUMP_RIGHT = 0;
	public static final int TRUMP_LEFT = 1;
	public static final int TRUMP_ACE = 2;
	public static final int TRUMP_KING = 3;
	public static final int TRUMP_QUEEN = 4;
	public static final int TRUMP_TEN = 5;
	public static final int TRUMP_NINE = 6;
	
	private boolean animating = false;
	private int animateTrickCard = -1;  //Set to the player whose card played should be animated (-1 = none)
	private int animateTakenTrick = -1; //Set to player that took the trick for animation (-1 = none);

	private boolean quickPlay = false; //go quickly through an AI only hand
	
	/**
	 * Game updates
	 */
	public void update(float delta) {
		if(getPlaystate() == PLAYSTATE_DEAL) {
			shuffleDeck();
			dealCards();
		} else if(getPlaystate() == PLAYSTATE_BID_NUMBER) {
			bidTimer += delta;
			if(bidTimer > 1) {
				switch(currentBidder) {
				case 1: //Player 2
					if(players[1].getBidValue() == -1) {
						player1.determineBid();
						players[1].setBidValueInterpreted(player1.getBid());
						players[1].setBidSuit(player1.getSuit());
						if((winningBidder != -1 && players[winningBidder].getBidValue() < players[1].getBidValue()) || //If bigger then previous bid
								(winningBidder == -1 && players[1].getBidValue() > Constants.BID_PASS)) { //Or no bid and there is a non-pass bid
							winningBidder = 1;
						} else {
							players[1].setBidValue(Constants.BID_PASS);
						}
						currentBidder = 2;
						//					Gdx.app.log("BID", "Player 2");
					}
					break;
				case 2: //Player 3
					if(players[2].getBidValue() == -1) {
						player2.determineBid();
						players[2].setBidValueInterpreted(player2.getBid());
						players[2].setBidSuit(player2.getSuit());
						if((winningBidder != -1 && players[winningBidder].getBidValue() < players[2].getBidValue()) ||
								(winningBidder == -1 && players[2].getBidValue() > Constants.BID_PASS)) {
							winningBidder = 2;
						} else {
							players[2].setBidValue(Constants.BID_PASS);
						}
						currentBidder = 3;
						//					Gdx.app.log("BID", "Player 3");
					}
					break;
				case 3: //Player 4
					if(players[3].getBidValue() == -1) {
						player3.determineBid();				
						players[3].setBidValueInterpreted(player3.getBid());
						players[3].setBidSuit(player3.getSuit());
						if((winningBidder != -1 && players[winningBidder].getBidValue() < players[3].getBidValue()) ||
								(winningBidder == -1 && players[3].getBidValue() > Constants.BID_PASS)) {
							winningBidder = 3;
						} else {
							players[3].setBidValue(Constants.BID_PASS);
						}
						currentBidder = 0;
						//					Gdx.app.log("BID", "Player 4");
					}
					break;
				}
				if(players[currentBidder].getBidValue() != -1) {					
					if(winningBidder == 0) { //Player
						staying[winningBidder] = STAYING_STAY;
						setPlaystate(PLAYSTATE_BID_WON);
						currentStayer = 1;
					} else if(winningBidder == -1) { //Nobody bid
						setPlaystate(PLAYSTATE_NO_BID);
//						nextHand();
					} else {
						staying[winningBidder] = STAYING_STAY;
						setPlaystate(PLAYSTATE_STAY);					
						currentStayer = (winningBidder + 1) % 4;										
					}				
				}
				
				//If a chipper bid came through, end bidding and move on
				if(winningBidder != -1 && players[winningBidder].getBidValue() == Constants.BID_CHIPPER) {
					if(winningBidder == 0) {
						setPlaystate(PLAYSTATE_BID_WON);
						currentStayer = 1;
					} else {
						setPlaystate(PLAYSTATE_STAY);					
						currentStayer = (winningBidder + 1) % 4;
					}
				}
				
				bidTimer = 0;
			}
		} else if(getPlaystate() == PLAYSTATE_BID_WON) {

		} else if(getPlaystate() == PLAYSTATE_STAY) {
			if(players[winningBidder].getBidSuit() == Constants.NO_TRUMP || players[winningBidder].getBidValue() == Constants.BID_CHIPPER) {
				staying[0] = STAYING_STAY;
				staying[1] = STAYING_STAY;
				staying[2] = STAYING_STAY;
				staying[3] = STAYING_STAY;

				if(players[winningBidder].getBidSuit() == Constants.NO_TRUMP) {
					if(winningBidder == 0) {
						setCurrentPlayer(3);
						setPlayLead(3);
					} else {
						setCurrentPlayer(winningBidder-1);
						setPlayLead(winningBidder-1);
					}
				} else {
					setCurrentPlayer(winningBidder);
					setPlayLead(winningBidder);
				}
				setPlaystate(PLAYSTATE_PLAY);
//				Gdx.app.log("PLAYSTATE_STAY", winningBidder + ":" + players[winningBidder].getBidSuit() + "-" + players[winningBidder].getBidValue());
				return;				
			}
			stayTimer += delta;
			if(stayTimer > 1) {
				if(staying[currentStayer] == STAYING_WAITING) {
					switch(currentStayer) {
					case 1:
						if(player1.staying(players[winningBidder].getBidValue(), players[winningBidder].getBidSuit())) {
							staying[1] = STAYING_STAY;
						} else {
							staying[1] = STAYING_OUT;
						}
						currentStayer = 2;
						break;
					case 2:
						if(player2.staying(players[winningBidder].getBidValue(), players[winningBidder].getBidSuit())) {
							staying[2] = STAYING_STAY;
						} else {
							staying[2] = STAYING_OUT;
						}
						currentStayer = 3;
						break;
					case 3:
						if(player3.staying(players[winningBidder].getBidValue(), players[winningBidder].getBidSuit())) {
							staying[3] = STAYING_STAY;
						} else {
							staying[3] = STAYING_OUT;
						}
						currentStayer = 0;
						break;
					}
				}
				if(staying[currentStayer] != STAYING_WAITING) {
					setCurrentPlayer(winningBidder);
					setPlayLead(winningBidder);
					setPlaystate(PLAYSTATE_PLAY);
//					Gdx.app.log("PLAYSTATE_STAY", winningBidder + ":" + players[winningBidder].getBidSuit() + "-" + players[winningBidder].getBidValue());
				}
				stayTimer = 0;
			}
		} else if(getPlaystate() == PLAYSTATE_PLAY) {
			if(countStayingPlayers() > 1) {
				if(currentTrick == null) {
					currentTrick = new Trick(playLead, staying);
				}
				if(!animating) {
					playTimer += delta;
					if(isQuickPlay() && playTimer > 0.1) {
						if(currentTrick.completedTrick()) {
							//Determine the winner and reset for new trick
							int winner = currentTrick.getWinningPlayer(players[winningBidder].getBidSuit());
							players[winner].addWonTrick(currentTrick);
														
							setLastTrick(currentTrick);
							setAnimateTakenTrick(winner);
							setAnimating(true);

//							Gdx.app.log("TRICK FINISHED", "Lead: " + currentTrick.getLeadPlayer());
//							Gdx.app.log("TRICK FINISHED", "Winner: " + winner);

							if(players[winner].getHand().getCards().size() == 0) {
								//Hand over
								handleScores();
								setPlaystate(PLAYSTATE_OVER);
								for(int i = 0;i<players.length;i++) {
//									Gdx.app.log("PLAYSTATE_PLAY", i + ":" + players[i].getWonTricks().size());
								}
								setQuickPlay(false);
							} else {
								//More to play	
								setPlayLead(winner);
								currentTrick = new Trick(winner, staying);		
								currentPlayer = winner;
							}
						} else {


							int cardToPlay = -1;

							//Figure out the index of the card to play
							switch(currentPlayer) {
							case 1:
								if(currentPlayer == playLead) {
									cardToPlay = player1.getCardToLead();
								} else {
									cardToPlay = player1.getCardToPlay();
								}
								break;
							case 2:						
								if(currentPlayer == playLead) {
									cardToPlay = player2.getCardToLead();
								} else {
									cardToPlay = player2.getCardToPlay();
								}
								break;
							case 3:
								if(currentPlayer == playLead) {
									cardToPlay = player3.getCardToLead();
								} else {
									cardToPlay = player3.getCardToPlay();
								}
								break;
							}
							if(currentPlayer != 0) {
								//Play the card, remove from players hand, and set next player
//								Gdx.app.log("AI PLAYING CARD", currentPlayer + ":" + getCardString(players[currentPlayer].getHand().getCards().get(cardToPlay)));

								currentTrick.setPlayerCard(currentPlayer, players[currentPlayer].getHand().getCards().get(cardToPlay));

								player1.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));
								player2.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));
								player3.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));

								players[currentPlayer].getHand().getCards().remove(cardToPlay);
								this.setAnimateTrickCard(currentPlayer);
								this.setAnimating(true);

								currentPlayer = nextStayingPlayer(currentPlayer);
							}	
						}
						playTimer = 0;	
					} else {
						if(playTimer > 1) {
							if(currentTrick.completedTrick()) {
								//Determine the winner and reset for new trick
								int winner = currentTrick.getWinningPlayer(players[winningBidder].getBidSuit());
								players[winner].addWonTrick(currentTrick);
															
								setLastTrick(currentTrick);
								setAnimateTakenTrick(winner);
								setAnimating(true);
	
	//							Gdx.app.log("TRICK FINISHED", "Lead: " + currentTrick.getLeadPlayer());
	//							Gdx.app.log("TRICK FINISHED", "Winner: " + winner);
	
								if(players[winner].getHand().getCards().size() == 0) {
									//Hand over
									handleScores();
									setPlaystate(PLAYSTATE_OVER);
									for(int i = 0;i<players.length;i++) {
//										Gdx.app.log("PLAYSTATE_PLAY", i + ":" + players[i].getWonTricks().size());
									}
									setQuickPlay(false);
								} else {
									//More to play	
									setPlayLead(winner);
									currentTrick = new Trick(winner, staying);		
									currentPlayer = winner;
								}
							} else {
	
	
								int cardToPlay = -1;
	
								//Figure out the index of the card to play
								switch(currentPlayer) {
								case 1:
									if(currentPlayer == playLead) {
										cardToPlay = player1.getCardToLead();
									} else {
										cardToPlay = player1.getCardToPlay();
									}
									break;
								case 2:						
									if(currentPlayer == playLead) {
										cardToPlay = player2.getCardToLead();
									} else {
										cardToPlay = player2.getCardToPlay();
									}
									break;
								case 3:
									if(currentPlayer == playLead) {
										cardToPlay = player3.getCardToLead();
									} else {
										cardToPlay = player3.getCardToPlay();
									}
									break;
								}
								if(currentPlayer != 0) {
									//Play the card, remove from players hand, and set next player
	//								Gdx.app.log("AI PLAYING CARD", currentPlayer + ":" + getCardString(players[currentPlayer].getHand().getCards().get(cardToPlay)));
	
									currentTrick.setPlayerCard(currentPlayer, players[currentPlayer].getHand().getCards().get(cardToPlay));
	
									player1.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));
									player2.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));
									player3.addSeenCard(players[currentPlayer].getHand().getCards().get(cardToPlay));
	
									players[currentPlayer].getHand().getCards().remove(cardToPlay);
									this.setAnimateTrickCard(currentPlayer);
									this.setAnimating(true);
	
									currentPlayer = nextStayingPlayer(currentPlayer);
								}	
							}
							playTimer = 0;	
						}
					}
				}
			} else {
				//Only one staying player, award them all points
				for(int i = 0;i<4;i++) {
					if(staying[i] == STAYING_STAY) {
						players[i].addWonTrick(new Trick(i, staying));
						players[i].addWonTrick(new Trick(i, staying));
						players[i].addWonTrick(new Trick(i, staying));
						players[i].addWonTrick(new Trick(i, staying));
						players[i].addWonTrick(new Trick(i, staying));
						players[i].addWonTrick(new Trick(i, staying));
					}
				}
				handleScores();				
				setPlaystate(PLAYSTATE_OVER);

			}
		} else if(getPlaystate() == PLAYSTATE_OVER) {
			if(players[0].getScore() >= WINNING_SCORE ||
					players[1].getScore() >= WINNING_SCORE ||
					players[2].getScore() >= WINNING_SCORE ||
					players[3].getScore() >= WINNING_SCORE) {				
				setPlaystate(PLAYSTATE_GAMEOVER);
			}
		} else if(getPlaystate() == PLAYSTATE_NO_BID) {
			
		}
	}


	/**
	 * Players the selected card from the users hand and moves to the next player
	 * @param selectedCard
	 */
	public void playPlayerCard(int selectedCard) {
		//Gdx.app.log("USER PLAYING CARD", 0 + ":" + getCardString(players[0].getHand().getCards().get(selectedCard)));
		if(selectedCard != -1) {
			currentTrick.setPlayerCard(0, players[0].getHand().getCards().get(selectedCard));
			players[0].getHand().getCards().remove(selectedCard);
			currentPlayer = nextStayingPlayer(0);
		}
	}

	/**
	 * Returns count of staying players
	 * @return
	 */
	private int countStayingPlayers() {
		int playerCount = 0;
		for(int i = 0;i<staying.length;i++) {
			if(staying[i] == STAYING_STAY) {
				playerCount++;
			}
		}
		return playerCount;
	}

	/**
	 * Returns the next staying player
	 * @param i
	 * @return
	 */
	private int nextStayingPlayer(int currentPlayer) {
		int nextPlayer = currentPlayer;
		boolean foundNext = false;
		while(!foundNext) {
			nextPlayer++;
			if(nextPlayer > 3) {
				nextPlayer = 0;
			}
			if(staying[nextPlayer] == STAYING_STAY) {
				foundNext = true;
			}
		}
		return nextPlayer;
	}

	/**
	 * Returns the player that won the bidding
	 * @return
	 */
	public Player getWinningBidderPlayer() {
		return players[winningBidder];
	}

	/**
	 * 	Moves to the next dealer
	 */
	public void nextDealer() {
		dealer = (dealer + 1) % 4;
		currentBidder = (dealer + 1) % 4;
	}	
	
	/**
	 * Initializes the game
	 */
	public void initialize() {
		for(int player = 0;player<4;player++) {
			players[player] = new Player(player);
		}
		for(int suit = 0;suit<4;suit++) {
			deck.add(new int[]{suit, Constants.NINE});
			deck.add(new int[]{suit, Constants.TEN});
			deck.add(new int[]{suit, Constants.JACK});
			deck.add(new int[]{suit, Constants.QUEEN});
			deck.add(new int[]{suit, Constants.KING});
			deck.add(new int[]{suit, Constants.ACE});
		}

		player1 = new AIAgent(this, players[1]);
		player2 = new AIAgent(this, players[2]);
		player3 = new AIAgent(this, players[3]);


	}

	/**
	 * Deals cards out to players
	 */
	public void dealCards() {
		for(int card = 0;card < 6; card++) {
			players[0].addCardToHand(deck.get((card*4) + 0));
			players[1].addCardToHand(deck.get((card*4) + 1));
			players[2].addCardToHand(deck.get((card*4) + 2));
			players[3].addCardToHand(deck.get((card*4) + 3));
		}
		setPlaystate(PLAYSTATE_BID_NUMBER);
	}

	/**
	 * Shuffles the deck of cards
	 */
	public void shuffleDeck() {
		Collections.shuffle(deck);
	}

	/**
	 * Returns string representation of passed card
	 * @param card
	 * @return
	 */
	public String getCardString(int[] card) {
		StringBuffer sb = new StringBuffer();
		switch(card[1]) {
		case Constants.NINE:
			sb.append("9");
			break;
		case Constants.TEN:
			sb.append("10");
			break;
		case Constants.JACK:
			sb.append("Jack");
			break;
		case Constants.QUEEN:
			sb.append("Queen");
			break;
		case Constants.KING:
			sb.append("King");
			break;
		case Constants.ACE:
			sb.append("Ace");
			break;
		}

		sb.append(" of ");

		switch(card[0]) {
		case Constants.CLUBS:
			sb.append("Clubs");
			break;
		case Constants.DIAMONDS:
			sb.append("Diamonds");
			break;
		case Constants.HEARTS:
			sb.append("Hearts");
			break;
		case Constants.SPADES:
			sb.append("Spades");
			break;
		}

		return sb.toString();
	}

	/**
	 * Swaps the position of two cards in the players hand
	 * @param selectedCard
	 * @param card
	 */
	public void swapPlayerCards(int selectedCard, int card) {
		players[0].getHand().swapCards(selectedCard, card);
	}

	/**
	 * Handles after the player bids, anything not a pass is assumed to be current winning bid
	 */
	public void handlePlayerBid() {
		if(players[0].getBidValue() != Constants.BID_PASS) {
			winningBidder = 0;
		}
		currentBidder = 1;
	}


	/**
	 * Handles after the player indicates a suit
	 */
	public void handlePlayerSuit() {
		setPlaystate(PLAYSTATE_STAY);
	}


	/**
	 * Sets player staying status
	 * @param stayingValue
	 */
	public void playerStaying(int stayingValue) {
		staying[0] = stayingValue;
		currentStayer = 1;
	}

	public ArrayList<int[]> getDeck() {
		return deck;
	}

	public void setDeck(ArrayList<int[]> deck) {
		this.deck = deck;
	}

	public Player[] getPlayers() {
		return players;
	}

	public void setPlayers(Player players[]) {
		this.players = players;
	}

	public static void main(String args[]) {
		ChipperEngine engine = new ChipperEngine();
		engine.initialize();
		engine.shuffleDeck();
		engine.dealCards();

		for(int player = 0;player<4;player++) {
			for(int card = 0;card<6;card++) {
				System.out.println(player + ":" + engine.getCardString(engine.getPlayers()[player].getHand().getCards().get(card)));
			}
		}

	}

	public void setDealer(int dealer) {
		this.dealer = dealer;
	}

	public int getDealer() {
		return dealer;
	}

	public void setGamestate(int gamestate) {
		this.gamestate = gamestate;
	}

	public int getGamestate() {
		return gamestate;
	}

	public void setPlaystate(int playstate) {
		switch(playstate) {
		case PLAYSTATE_DEAL:
//			Gdx.app.log("PLAYSTATE", "deal");
			break;
		case PLAYSTATE_BID_NUMBER:
//			Gdx.app.log("PLAYSTATE", "number");
			break;
		case PLAYSTATE_BID_WON:
//			Gdx.app.log("PLAYSTATE", "won");
			break;
		case PLAYSTATE_STAY:
//			Gdx.app.log("PLAYSTATE", "stay");
			break;
		case PLAYSTATE_PLAY:
//			Gdx.app.log("PLAYSTATE", "play");
			break;
		case PLAYSTATE_OVER:
//			Gdx.app.log("PLAYSTATE", "over");
			break;
		}

		this.playstate = playstate;
	}

	public int getPlaystate() {
		return playstate;
	}

	public void setWinningBidder(int winningBid) {
		this.winningBidder = winningBid;
	}

	public int getWinningBidder() {
		return winningBidder;
	}


	public int getCurrentBidder() {
		return currentBidder;
	}


	public void setCurrentBidder(int currentBidder) {
		this.currentBidder = currentBidder;
	}


	public int[] getStaying() {
		return staying;
	}


	public int getCurrentStayer() {
		return currentStayer;
	}


	public void setPlayLead(int playLead) {
		this.playLead = playLead;
	}


	public int getPlayLead() {
		return playLead;
	}


	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}


	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentTrick(Trick currentTrick) {
		this.currentTrick = currentTrick;
	}

	public Trick getCurrentTrick() {
		return currentTrick;
	}

	/**
	 * Returns order for no trump
	 * @return
	 */
	public static int[] getNoTrumpTrump() {
		int[] noTrumpTrump = new int[6];

		noTrumpTrump[0] = Constants.ACE;
		noTrumpTrump[1] = Constants.KING;
		noTrumpTrump[2] = Constants.QUEEN;
		noTrumpTrump[3] = Constants.JACK;
		noTrumpTrump[4] = Constants.TEN;
		noTrumpTrump[5] = Constants.NINE;

		return noTrumpTrump;
	}

	/**
	 * Returns order for hearts trump
	 * @return
	 */
	public static ArrayList<int[]> getHeartsTrump() {
		//Hearts
		ArrayList<int[]> heartsTrump = new ArrayList<int[]>();
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.JACK});
		heartsTrump.add(new int[]{Constants.DIAMONDS, Constants.JACK});
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.ACE});
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.KING});
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.QUEEN});
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.TEN});
		heartsTrump.add(new int[]{Constants.HEARTS, Constants.NINE});
		return heartsTrump;
	}
	/**
	 * Returns order for diamonds trump
	 * @return
	 */
	public static ArrayList<int[]> getDiamondsTrump() {
		//Diamonds
		ArrayList<int[]> diamondsTrump = new ArrayList<int[]>();
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.JACK});
		diamondsTrump.add(new int[]{Constants.HEARTS, Constants.JACK});
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.ACE});
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.KING});
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.QUEEN});
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.TEN});
		diamondsTrump.add(new int[]{Constants.DIAMONDS, Constants.NINE});
		return diamondsTrump;
	}
	/**
	 * Returns order for clubs trump
	 * @return
	 */
	public static ArrayList<int[]> getClubsTrump() {
		//Clubs
		ArrayList<int[]> clubsTrump = new ArrayList<int[]>();
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.JACK});
		clubsTrump.add(new int[]{Constants.SPADES, Constants.JACK});
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.ACE});
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.KING});
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.QUEEN});
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.TEN});
		clubsTrump.add(new int[]{Constants.CLUBS, Constants.NINE});
		return clubsTrump;
	}
	/**
	 * Returns order for spades trump
	 * @return
	 */
	public static ArrayList<int[]> getSpadesTrump() {
		//Spades
		ArrayList<int[]> spadesTrump = new ArrayList<int[]>();
		spadesTrump.add(new int[]{Constants.SPADES, Constants.JACK});
		spadesTrump.add(new int[]{Constants.CLUBS, Constants.JACK});
		spadesTrump.add(new int[]{Constants.SPADES, Constants.ACE});
		spadesTrump.add(new int[]{Constants.SPADES, Constants.KING});
		spadesTrump.add(new int[]{Constants.SPADES, Constants.QUEEN});
		spadesTrump.add(new int[]{Constants.SPADES, Constants.TEN});
		spadesTrump.add(new int[]{Constants.SPADES, Constants.NINE});
		return spadesTrump;
	}

	/**
	 * Moves the game onto the new hand
	 */
	public void nextHand() {
		//Add tricks to scores and reset player for new hand
		for(int player = 0;player<4;player++) {
			players[player].resetWonTricks();
			players[player].resetBid();
			players[player].clearHand();
			staying[player] = STAYING_WAITING;
	
		}
		
		player1.resetSeenCards();
		player2.resetSeenCards();
		player3.resetSeenCards();
		
		nextDealer();
		setWinningBidder(-1);	
		setCurrentTrick(null);
		setPlaystate(PLAYSTATE_DEAL);		
	}

	/**
	 * Handles scores
	 */
	public void handleScores() {
		for(int player = 0;player<4;player++) {
			if(staying[player] == STAYING_STAY) {
				int tricks = players[player].getWonTricks().size();
				if(tricks == 0) {  //No tricks, set
					players[player].decreaseScore(6);
					players[player].updateScoreHistory("S");
				} else {
					if(winningBidder == player) { //Bidder
						if(players[player].getBidValue() == Constants.BID_CHIPPER) { //Bid chipper
							if(tricks == 6) {
								players[player].increaseScore(21); 
								players[player].updateScoreHistory("C");
							} else {
								players[player].decreaseScore(12); //Double set for failed chipper
								players[player].updateScoreHistory("SS");
							}
						} else if(tricks >= players[player].getBidValue()) { //Made bid
							players[player].increaseScore(tricks);
							players[player].updateScoreHistory(Integer.toString(tricks));
						} else { //Did not make bid
							players[player].decreaseScore(6);
							players[player].updateScoreHistory("S");
						}
					} else { //Non-bidder
						players[player].increaseScore(tricks);
						players[player].updateScoreHistory(Integer.toString(tricks));
					}
				}			
			} else if(staying[player] == STAYING_OUT) {
				players[player].updateScoreHistory("P");
			}
		}
	}


	public int getSelectedCard() {
		return selectedCard;
	}


	public void setSelectedCard(int selectedCard) {
		this.selectedCard = selectedCard;
	}


	public void setAnimating(boolean animating) {
		this.animating = animating;
	}


	public boolean isAnimating() {
		return animating;
	}


	public void setAnimateTrickCard(int animateTrickCard) {
		this.animateTrickCard = animateTrickCard;
	}


	public int getAnimateTrickCard() {
		return animateTrickCard;
	}


	public void setAnimateTakenTrick(int animateTakenTrick) {
		this.animateTakenTrick = animateTakenTrick;
	}


	public int getAnimateTakenTrick() {
		return animateTakenTrick;
	}


	public void setLastTrick(Trick lastTrick) {
		this.lastTrick = lastTrick;
	}


	public Trick getLastTrick() {
		return lastTrick;
	}

	/**
	 * Can the player follow suit
	 * @param player
	 * @return
	 */
	public boolean canFollowSuit(int player) {
		ArrayList<int[]> cards = players[0].getHand().getCards();
		int bidSuit = players[winningBidder].getBidSuit();
		int leadSuit = currentTrick.getPlayerCard(currentTrick.getLeadPlayer())[0];
		
		if(bidSuit != Constants.NO_TRUMP) { //Suited player
			ArrayList<int[]> trumpList = new ArrayList<int[]>();
			switch(bidSuit) {
			case Constants.HEARTS:
				trumpList = ChipperEngine.getHeartsTrump();
				break;
			case Constants.DIAMONDS:
				trumpList = ChipperEngine.getDiamondsTrump();
				break;
			case Constants.CLUBS:
				trumpList = ChipperEngine.getClubsTrump();
				break;
			case Constants.SPADES:
				trumpList = ChipperEngine.getSpadesTrump();
				break;
			}
			if(getCardIndex(currentTrick.getPlayerCard(currentTrick.getLeadPlayer()), trumpList) > -1) {  //If the lead suit was the bid suit, check trump cards
				ArrayList<int[]> trumpInHand = getTrumpCardsInHand(cards, trumpList);
				return trumpInHand.size() > 0;
			} else { //If the lead was not the bid suit, check suit cards (minus trump list for left check)
				ArrayList<int[]> suitInHand = getCardsMatchingSuitInHand(leadSuit, trumpList, cards);
				return suitInHand.size() > 0;
			}
			
		} else { //No Trump play, find all cards matching lead suit
			ArrayList<int[]> suitInHand = this.getCardsMatchingSuitInHand(leadSuit, cards);
			return suitInHand.size() > 0;
		}		
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
	 * Returns list of cards that match suit of passed card, for no trump
	 * @param winningCard
	 * @return
	 */
	private ArrayList<int[]> getCardsMatchingSuitInHand(int suit, ArrayList<int[]> hand) {
		ArrayList<int[]> suitInHand = new ArrayList<int[]>();
		for(int[] card : hand) {
			if(suit == card[0]) {
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
	private ArrayList<int[]> getCardsMatchingSuitInHand(int suit, ArrayList<int[]> trumpList, ArrayList<int[]> hand) {
		ArrayList<int[]> suitInHand = new ArrayList<int[]>();
		for(int[] card : hand) {
			if(suit == card[0] && getCardIndex(card, trumpList) == -1) {
				suitInHand.add(card);
			}
		}
		return suitInHand;
	}


	/**
	 * Returns true if the passed card will follow suit
	 * @param card
	 * @return
	 */
	public boolean isFollowingSuit(int[] card) {
		int bidSuit = players[winningBidder].getBidSuit();
		int leadSuit = currentTrick.getPlayerCard(currentTrick.getLeadPlayer())[0];
		
		if(bidSuit != Constants.NO_TRUMP) { //Suited player
			ArrayList<int[]> trumpList = new ArrayList<int[]>();
			switch(bidSuit) {
			case Constants.HEARTS:
				trumpList = ChipperEngine.getHeartsTrump();
				break;
			case Constants.DIAMONDS:
				trumpList = ChipperEngine.getDiamondsTrump();
				break;
			case Constants.CLUBS:
				trumpList = ChipperEngine.getClubsTrump();
				break;
			case Constants.SPADES:
				trumpList = ChipperEngine.getSpadesTrump();
				break;
			}
			if(getCardIndex(currentTrick.getPlayerCard(currentTrick.getLeadPlayer()), trumpList) > -1) {  //If the lead card was the bid suit, check against the trump list
				return getCardIndex(card, trumpList) > -1;
			} else { //If the lead was not the bid suit, check against the leadSuit
				return leadSuit == card[0];
			}
			
		} else { //No Trump play, check against lead suit
			return leadSuit == card[0];
		}	
	}


	public void setQuickPlay(boolean quickPlay) {
		this.quickPlay = quickPlay;
	}


	public boolean isQuickPlay() {
		return quickPlay;
	}
}
