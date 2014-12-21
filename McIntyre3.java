import java.util.Random;

// McIntyre, Lloyd
// Project 3: Advanced
// CS 110-3 FA 14


/**
 * @author Lloyd E. McIntyre IV
 */
public class McIntyre3 extends ConnectThree {
	
	/**
	 * @param args Does nothing
	 */
	public static void main(String[] args) {
		launch();
	}

	/* (non-Javadoc)
	 * @see ConnectThree#initialization()
	 */
	@Override
	protected void initialization() {
		registerAi(new Tron());
	}
	
	/* (non-Javadoc)
	 * @see ConnectThree#getWinner(ConnectThree.Player[][], int, int)
	 */
	protected Player getWinner(Player[][] board, int UNUSED, int INGORE) {
		for(int r = 0; r < GRID_HEIGHT; r++) {
			for(int c = 0; c < GRID_WIDTH; c++) {
				// Calculates amount of availible spaces to the left and right, set to a max of 2
				int allowedRight = ( (GRID_WIDTH - 1) - c) >= 1 ? ( ( (GRID_WIDTH - 1) - c ) >= 2 ? 2 : 1 ) : 0;
				int allowedLeft = c >= 2 ? 2 : (c == 1 ? 1 : 0 );
				int allowedDown = ((GRID_HEIGHT - 1) - r) >= 1 ? (((GRID_HEIGHT - 1) - r) >= 2 ? 2 : 1) : 0 ;
				int allowedUp = r >= 1 ? (r >= 2 ? 2 : 1) : 0;
				
				// Check down
				for(int i = (allowedDown >= 2 ? 2 : -1); i >= 0; i--) {
					if(board[r][c] != Player.NONE && board[r][c] == board[r+1][c] && board[r][c] == board[r+2][c]) {
						return board[r][c];
					}
				}
				
				// Check left and right, incrementing for each piece it finds in a row
				
				int count = 1;
				
				for(int i = 1; i <= allowedLeft; i++) {
					if(board[r][c-i] != board[r][c])
						break;
					if(board[r][c] != Player.NONE && board[r][c] == board[r][c-i]) {
						count++;
					}	
				}

				for(int i = 1; i <= allowedRight; i++) {
					if(board[r][c+i] != board[r][c])
						break;
					
					if(board[r][c] != Player.NONE && board[r][c+i] == board[r][c]) {
						count++;
					}
				}
				
				// Return the winner if there is one
				if(count >= 3) {
					return board[r][c];
				}
				
				
				
				// Check forward diagonal
				int frontCount = 1;
				if(allowedUp >= 1 && allowedRight >= 1) {
					for(int i = 1; i <= (allowedRight <= allowedUp ? allowedRight : allowedUp); i++ ) {
						if(board[r-i][c+i] == Player.NONE)
							break;
						
						if(board[r][c] != Player.NONE && board[r-i][c+i] == board[r][c]) {
							frontCount++;
						}
					}
				}
				
				// Check backward diagonal
				int backCount = 1;
				if(allowedUp >= 1 && allowedLeft >= 1) {
					for(int i = 1; i <= (allowedLeft <= allowedUp ? allowedLeft : allowedUp); i++) {
						if(board[r-i][c-i] == Player.NONE)
							break;
						
						if(board[r][c] != Player.NONE && board[r-i][c-i] == board[r][c]) {
							backCount++;
						}
					}
				}
				
				// Return winner if it finds one
				if(frontCount >= 3 || backCount >= 3)
					return board[r][c];
				
			}
		}
		
		// This means nobody won yet
		return Player.NONE;
	}

	/**
	 * @author Lloyd E. McIntyre IV
	 *
	 */
	public class Tron extends AI {
		final int DEPTH_MAX = 8;
		Random rand = new Random();
		int bestChoice = 1;
		Player player;
		Player opponent;
		
		@Override
		public int getBestColumn(Player[][] board, Player player) {	
			this.player = player;
			this.opponent = (player == Player.PLAYERX ? Player.PLAYERO : Player.PLAYERX);
			
			// Don't actually do anything with i, this just starts the recursion
			int i = minimax(DEPTH_MAX, board, true);
			return bestChoice;
			
		}
		
		/**
		 * @param depth The amount of times the tree will grow.
		 * @param board The current game board state.
		 * @param maximizingPlayer If the next leaf should be a minimizer or a maximizer. Should start true.
		 * @return Returns the score of the board at the deepest level and works back up.
		 */
		private int minimax(int depth, Player[][] board, Boolean maximizingPlayer) {
			Player[][] newBoard = board;
			
			int[][] moveList = getLegalMoves(newBoard);
			
			if(depth == 0) {
				return score(newBoard);
			}
			
			if(isDraw(newBoard)) {
				return 0;
			}
			
			if(getWinner(newBoard,0,0) != Player.NONE) {
				return score(newBoard);
			}
			
			if(maximizingPlayer) {
				int bestValue = Integer.MIN_VALUE;
				for(int move = 0; move < GRID_WIDTH; move++) {
					
					// If invalid, skip this iteration
					if(moveList[move][0] == -1 || moveList[move][1] == -1) {
						continue;
					} else {
						// Change the board, call a new minimax, switch the board back for the next iteration
						newBoard[ moveList[move][0] ][ moveList[move][1] ] = player;
						int value = minimax(depth - 1, newBoard, false);
						newBoard[ moveList[move][0] ][ moveList[move][1] ] = Player.NONE;
						
						// Manage the best value and select best choice if root node
						if(value > bestValue) {
							bestValue = value;
							if(depth == DEPTH_MAX)
								bestChoice = move;
						}
					}
				}
				
				return bestValue;
				
			} else {
				
				int bestValue = Integer.MAX_VALUE;
				
				for(int move = 0; move < GRID_WIDTH; move++) {
					
					if(moveList[move][0] == -1 || moveList[move][1] == -1) {	
						continue;
					} else {
						// Change the board, call a new minimax, switch the board back for the next iteration
						newBoard[ moveList[move][0] ][ moveList[move][1] ] = opponent;
						int value = minimax(depth - 1, newBoard, true);
						newBoard[ moveList[move][0] ][ moveList[move][1] ] = Player.NONE;
						
						// Manage the best value and select best choice if root node
						if(value < bestValue) {
							bestValue = value;
							if(depth == DEPTH_MAX)
								bestChoice = move;
						}
					}
					
				}
				return bestValue;
			}
			
		}
		
		/**
		 * @param board The board you want to get the legal moves of
		 * @return A 2d array of legal positions
		 */
		private int[][] getLegalMoves(Player[][] board) {
			int[][] moves = {{-1,-1},
							{-1,-1},
							{-1,-1},
							{-1,-1}};
			
			for(int c = 0; c < GRID_WIDTH; c++) {
				int r = top(board, c);
				
				if(r >= 0) {
					moves[c][0] = r;
					moves[c][1] = c;
				}
			}
			
			return moves;
		}
		
		/**
		 * @param board The board you want to check
		 * @return True or false if the board is a draw or not
		 */
		private boolean isDraw(Player[][] board) {
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == Player.NONE)
						return false;
				}
			}
			
			return true;
		}
		
		/**
		 * @param board The board you want to score
		 * @return The heuristic value of the board determined by counting the amount of potential plays for each player
		 */
		public int score(Player[][] board) {
			int score = 0;
			int oneSegmentX = 0;
			int twoSegmentX = 0;
			int threeSegmentX = 0;
			
			int oneSegmentO = 0;
			int twoSegmentO = 0;
			int threeSegmentO = 0;
		
			
			// Player X
			// Count number of horizontal segments of one
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == player && board[r][c+1] == Player.NONE && board[r][c+2] == Player.NONE) {
						oneSegmentX++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == player && board[r][c+2] == Player.NONE) {
						oneSegmentX++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == Player.NONE && board[r][c+2] == player) {
						oneSegmentX++;
					}
				}
			}
			
			//  Count number of horizontal segments of two
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == player && board[r][c+1] == player && board[r][c+2] == Player.NONE) {
						twoSegmentX++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == player && board[r][c+2] == player) {
						twoSegmentX++;
					} else if(board[r][c] == player && board[r][c+1] == Player.NONE && board[r][c+2] == player) {
						twoSegmentX++;
					}
				}
			}
			
			// Count number of horizontal segments of three
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == player && board[r][c+1] == player && board[r][c+2] == player) {
						threeSegmentX++;
					}
				}
			}
			
			// Count number of vertical segments of one
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == player && board[r-1][c] == Player.NONE && board[r-2][c] == Player.NONE) {
						oneSegmentX++;
					}
				}
			}
			
			// Count number of vertical segments of two
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == player && board[r-1][c] == player && board[r-2][c] == Player.NONE) {
						twoSegmentX++;
					}
				}
			}
			
			// Count number of vertical segments of three
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == player && board[r-1][c] == player && board[r-2][c] == player) {
						threeSegmentX++;
					}
				}
			}
			
			
			
			
			
			
			
			// Player O
			// Count number of horizontal segments of one
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == opponent && board[r][c+1] == Player.NONE && board[r][c+2] == Player.NONE) {
						oneSegmentO++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == opponent && board[r][c+2] == Player.NONE) {
						oneSegmentO++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == Player.NONE && board[r][c+2] == opponent) {
						oneSegmentO++;
					}
				}
			}
			
			// Count number of horizontal segments of two
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == opponent && board[r][c+1] == opponent && board[r][c+2] == Player.NONE) {
						twoSegmentO++;
					} else if(board[r][c] == Player.NONE && board[r][c+1] == opponent && board[r][c+2] == opponent) {
						twoSegmentO++;
					} else if(board[r][c] == opponent && board[r][c+1] == Player.NONE && board[r][c+2] == opponent) {
						twoSegmentO++;
					}
				}
			}
			
			// Count number of horizontal segments of three
			for(int r = 0; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH-2; c++) {
					if(board[r][c] == opponent && board[r][c+1] == opponent && board[r][c+2] == opponent) {
						threeSegmentO++;
					}
				}
			}
			
			// Count number of vertical segments of one
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == opponent && board[r-1][c] == Player.NONE && board[r-2][c] == Player.NONE) {
						oneSegmentO++;
					}
				}
			}
			
			// Count number of vertical segments of two
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == opponent && board[r-1][c] == opponent && board[r-2][c] == Player.NONE) {
						twoSegmentO++;
					}
				}
			}
			
			// Count number of vertical segments of three
			for(int r = 2; r < GRID_HEIGHT; r++) {
				for(int c = 0; c < GRID_WIDTH; c++) {
					if(board[r][c] == opponent && board[r-1][c] == opponent && board[r-2][c] == opponent) {
						threeSegmentO++;
					}
				}
			}
			
			
			score = 4*oneSegmentX + 8*twoSegmentX + 100000*threeSegmentX + (-4*oneSegmentO) + (-8*twoSegmentO) + (-100000*threeSegmentO);
			System.out.println("Score: " + score);
			return score;
		}
	}

}

	