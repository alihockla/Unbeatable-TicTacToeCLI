package main;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class Game {
	
	private final String ANSI_RESET = "\u001B[0m";
	private final String ANSI_BLACK = "\u001B[30m";
	private final String ANSI_RED = "\033[0;31m";
	private final String ANSI_GREEN = "\u001B[32m";
	private final String ANSI_YELLOW = "\u001B[33m";
	private final String ANSI_BLUE = "\u001B[34m";
	private final String ANSI_PURPLE = "\u001B[35m";
	private final String ANSI_CYAN = "\u001B[36m";
	private final String ANSI_WHITE = "\u001B[37m";
	
	private final String welcomeMessage = "Welcome to TicTacToe CLI!\n\n"
			+ "Use numbers 0-8 to mark your box - like a keypad (but starting with 0).\n"
			+ "0 is the first box, 1 is the second, 2 is the third,\n"
			+ "3 is the first box in the second row and so on. Good luck!\n";
	
	// Used to record players' moves
    private Map<Integer, String> moves;
    private String[][] board;
    private Player p1;
    private Player p2;
    
    private String winner;
    private boolean vsComputer;
    
    private Scanner in = new Scanner(System.in);

    public Game() {
    	System.out.println(ANSI_GREEN + welcomeMessage + ANSI_RESET);
    	getPlayerInfo();
    	init();
    }
    
    private void init() {
    	winner = "";
    	moves = new HashMap<>(9);
    	board = new String[][] {{"0", "1", "2"}, {"3", "4", "5"}, {"6", "7", "8"}};
    	displayBoard();
    	startGame();
    }
    
    /*
     * Start a game
     */
	private void startGame() {
		int rounds = 0;
        while ((winner.length() == 0 && rounds < 9)) {
        	playRound();
        	if (checkWinner()) break;
        	rounds++;
        }
        
        System.out.println(ANSI_YELLOW + winner + ANSI_RESET);
        
        in.nextLine();
		String rematch = "";
		while (rematch.equals("")) {
			System.out.println("Rematch? (y or n): ");
			rematch = in.nextLine();
			
			if (rematch.equalsIgnoreCase("y")) {
				restartGame();
			} else {
				System.out.println("Goodbye!");
				break;
			}
		}
	}

	/*
	 * Plays a round. 
	 * Player 1 goes first, checks for a winner, then player 2 or computer plays their move.
	 */
	private void playRound() {
		// Player 1 move
		yourMove(p1);
		
		if (checkWinner()) return;
		
		// Computer's move
		if (vsComputer) {
			int move = getNextMove();
//			System.out.println("Computers move: " + move);
			if (isValidMove(move))
				updateBoard(move, p2.playerCharacter);
			
		} else { // multiplayer
			// Player 2 move
			yourMove(p2);
		}
    }
	
	/*
	 * Prompts the user for their move.
	 */
	private void yourMove(Player p) {
		int playerMove = -1;
		while (true) {
			System.out.println("Your move " + p.toString() + ":");
			
			try {
				playerMove = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.print("Invalid entry. ");
				in.nextLine();
			}
			
			if (isValidMove(playerMove)) {
				updateBoard(playerMove, p.playerCharacter);
				break;
			}
		}
	}
	
	/*
	 * Returns the next move based on a strategy to play a perfect game of tic-tac-toe (to win or 
	 * at least draw). 
	 * Based on Newell and Simon's 1972 tic-tac-toe program,
	 * described here: https://en.wikipedia.org/wiki/Tic-tac-toe#Strategy.
	 * 
	 */
	private int getNextMove() {
		/*
		 * 1) Win: If the player has two in a row, they can place a third to get three in a row.
		 */
		int winStrategy = -1;
		winStrategy = checkWinOrBlockStrategy("O");
		if (winStrategy != -1) return winStrategy;
		
		/*
		 * 2) Block: If the opponent has two in a row, the player must play the
		 * third themselves to block the opponent.
		 */
		int blockStrategy = -1;
		blockStrategy = checkWinOrBlockStrategy("X");
		if (blockStrategy != -1) return blockStrategy;
		
		/* 
		 * 3) Fork: Create an opportunity where the player has two ways 
		 * to win (two non-blocked lines of 2).
		 * 
		 * If opponent takes the center space, counteract that by placing your letter in a corner. 
		 * If opponent takes a corner space, take the middle space. This will force a draw in both cases. 
		 * Winning is almost impossible unless a major mistake is made by the opponent.
		 */
		if (moves.containsKey(4)) {
			// if opponent takes 2 corners, ai should take a side (given ai already has the center)
			if (opponentForked()) {
				if (!moves.containsKey(1)) return 1;
				if (!moves.containsKey(3)) return 3;
				if (!moves.containsKey(5)) return 5;
				if (!moves.containsKey(7)) return 7;
			}
			if (!moves.containsKey(0)) return 0;
			else if (!moves.containsKey(2)) return 2;
			else if (!moves.containsKey(6)) return 6;
			else if (!moves.containsKey(8)) return 8;
		} else {
			return 4;
		}
		
		/*
		 * (Implemented above)
		 * 4) Center: A player marks the center. (If it is the first move of the game, 
		 * playing a corner move gives the second player more opportunities to make a 
		 * mistake and may therefore be the better choice; however, it makes no difference 
		 * between perfect players.)
		 */
		
		/*
		 * 5) Opposite corner: If the opponent is in the corner, the player plays the opposite corner.
		 */
		if (moves.containsKey(0) && moves.get(0) == "X" && !moves.containsKey(8)) return 8;
		if (moves.containsKey(2) && moves.get(2) == "X" && !moves.containsKey(6)) return 6;
		if (moves.containsKey(6) && moves.get(6) == "X" && !moves.containsKey(2)) return 2;
		if (moves.containsKey(8) && moves.get(8) == "X" && !moves.containsKey(8)) return 0;
		
		/*
		 * 6) Empty corner: The player plays in a corner square.
		 */
		if (!moves.containsKey(0)) return 0;
		if (!moves.containsKey(2)) return 2;
		if (!moves.containsKey(6)) return 6;
		if (!moves.containsKey(8)) return 8;
		
		/*
		 * 7) Empty side: The player plays in a middle square on any of the 4 sides.
		 */
		if (!moves.containsKey(1)) return 1;
		if (!moves.containsKey(3)) return 3;
		if (!moves.containsKey(5)) return 5;
		if (!moves.containsKey(7)) return 7;
		
		return -1;
	}

	/*
	 * Check if opponent has 2 corners
	 */
	private boolean opponentForked() {
		if ((moves.containsKey(2) && moves.containsKey(6)) || (moves.containsKey(2) && moves.containsKey(8)) ||
				(moves.containsKey(2) && moves.containsKey(0))) {
			return moves.get(2) == "X";
		} else if ((moves.containsKey(0) && moves.containsKey(6)) || (moves.containsKey(0) && moves.containsKey(8)) ||
				(moves.containsKey(2) && moves.containsKey(0))) {
			return moves.get(0) == "X";
		} else if ((moves.containsKey(8) && moves.containsKey(6)) || (moves.containsKey(0) && moves.containsKey(8)) ||
				(moves.containsKey(2) && moves.containsKey(8))) {
			return moves.get(8) == "X";
		}
		
		return false;
	}

	/*
	 * Checks for 3 of the same characters (X or O) in a row, if so there is a winner.
	 * If all moves have been made and no winner then there is a draw.
	 */
	private boolean checkWinner() {
		// check horizontal
		for (int i = 0; i < 8; i += 3) {
			if (moves.containsKey(i) && moves.containsKey(i+1) && moves.containsKey(i+2)) {
				if (moves.get(i).equals(moves.get(i+1)) && moves.get(i+1).equals(moves.get(i+2))) {
					if (moves.get(i) == "X")
						winner = p1.playerName + " wins!";
					else
						winner = p2.playerName + " wins!";
					return true;
				}
			}
		}
		
		// check vertical
		for (int i = 0; i < 3; i++) {
			if (moves.containsKey(i) && moves.containsKey(i+3) && moves.containsKey(i+6)) {
				if (moves.get(i).equals(moves.get(i+3)) && moves.get(i+3).equals(moves.get(i+6))) {
					if (moves.get(i) == "X")
						winner = p1.playerName + " wins!";
					else
						winner = p2.playerName + " wins!";
					return true;
				}
			}
		}
		
		// check diagonal
		if (moves.containsKey(0) && moves.containsKey(4) && moves.containsKey(8)) {
			if (moves.get(0).equals(moves.get(4)) && moves.get(4).equals(moves.get(8))) {
				if (moves.get(0) == "X")
					winner = p1.playerName + " wins!";
				else
					winner = p2.playerName + " wins!";
				return true;
			}
		}
		if (moves.containsKey(2) && moves.containsKey(4) && moves.containsKey(6)) {
			if (moves.get(2).equals(moves.get(4)) && moves.get(4).equals(moves.get(6))) {
				if (moves.get(2) == "X")
					winner = p1.playerName + " wins!";
				else
					winner = p2.playerName + " wins!";
				return true;
			}
		}
		
		// check draw
		if (moves.size() == 9) {
			winner = "It's a draw!";
			return true;
		}
		
		return false;
	}

	/*
	 * Checks for a Win or Block scenario:
	 * Win: If the computer has two in a row, we can place a third to get three in a row.
	 * Block: If the opponent has two in a row, the computer must play the third themselves to block the opponent.
	 */
	private int checkWinOrBlockStrategy(String character) {
	/************************************************************************************************/
		// check horizontal possibilities
		// row 0
		if (moves.get(0) == character && moves.get(1) == character && !moves.containsKey(2)) {
			return 2;
		}
		if (moves.get(0) == character && moves.get(2) == character && !moves.containsKey(1)) {
			return 1;
		}
		if (moves.get(1) == character && moves.get(2) == character && !moves.containsKey(0)) {
			return 0;
		}
		// row 1
		if (moves.get(3) == character && moves.get(4) == character && !moves.containsKey(5)) {
			return 5;
		}
		if (moves.get(3) == character && moves.get(5) == character && !moves.containsKey(4)) {
			return 4;
		}
		if (moves.get(4) == character && moves.get(5) == character && !moves.containsKey(3)) {
			return 3;
		}
		// row 2
		if (moves.get(6) == character && moves.get(7) == character && !moves.containsKey(8)) {
			return 8;
		}
		if (moves.get(6) == character && moves.get(8) == character && !moves.containsKey(7)) {
			return 7;
		}
		if (moves.get(7) == character && moves.get(8) == character && !moves.containsKey(6)) {
			return 6;
		}
	/************************************************************************************************/
		// check vertical possibilities
		// col 0
		if (moves.get(0) == character && moves.get(3) == character && !moves.containsKey(6)) {
			return 6;
		}
		if (moves.get(0) == character && moves.get(6) == character && !moves.containsKey(3)) {
			return 3;
		}
		if (moves.get(6) == character && moves.get(3) == character && !moves.containsKey(0)) {
			return 0;
		}
		// col 1
		if (moves.get(1) == character && moves.get(4) == character && !moves.containsKey(7)) {
			return 7;
		}
		if (moves.get(1) == character && moves.get(7) == character && !moves.containsKey(4)) {
			return 4;
		}
		if (moves.get(4) == character && moves.get(7) == character && !moves.containsKey(1)) {
			return 1;
		}
		// col 2
		if (moves.get(2) == character && moves.get(5) == character && !moves.containsKey(8)) {
			return 8;
		}
		if (moves.get(2) == character && moves.get(8) == character && !moves.containsKey(5)) {
			return 5;
		}
		if (moves.get(5) == character && moves.get(8) == character && !moves.containsKey(2)) {
			return 2;
		}
	/************************************************************************************************/

		// check diagonal possibilities
		if (moves.get(0) == character && moves.get(4) == character && !moves.containsKey(8))
			return 8;
		if (moves.get(0) == character && moves.get(8) == character && !moves.containsKey(4))
			return 4;
		if (moves.get(4) == character && moves.get(8) == character && !moves.containsKey(0))
			return 0;

		if (moves.get(2) == character && moves.get(4) == character && !moves.containsKey(6))
			return 6;
		if (moves.get(2) == character && moves.get(6) == character && !moves.containsKey(4))
			return 4;
		if (moves.get(4) == character && moves.get(6) == character && !moves.containsKey(2))
			return 2;

//		System.out.println("No 2-in-a-row scenario.");
		return -1;
	}

	/*
	 * Updates the board with the intended move (provided by the player or computer) 
	 */
	private void updateBoard(int move, String player) {
		// keep track of all moves
		moves.put(move, player);
		
		int pos = move;
		if (pos >= 0 && pos <= 2) {
			board[0][pos] = player;
		} else if (pos >= 3 && pos <= 5) {
			pos -= 3;
			board[1][pos] = player;
		} else {
			pos -= 6;
			board[2][pos] = player;
		}
		displayBoard();
	}

	/*
	 * Checks user input for duplicate moves or an invalid input. 
	 * Valid inputs are digits between 0 and 8 inclusive.
	 */
	private boolean isValidMove(int input) {
		if (moves.containsKey(input)) {
			System.out.println("Invalid move already taken");
			return false;
		}
				
		if (input < 0 || input > 8) {
			System.out.println("Please enter a digit between 0 and 8");
			return false;
		}
		
		return true;
	}

	/*
	 * Gets player name and gamemode (multiplayer or vs computer).
	 */
    private void getPlayerInfo() {
//    	String pOneName = "";
    	System.out.println("Enter Player 1 name: ");
    	p1 = new Player(in.nextLine(), "X");
    	
    	String inpt = "";
    	System.out.println("\nEnter Player 2 name or type c to play against computer: ");
    	inpt = in.nextLine();
    	String pTwoName = "";
    	if (inpt.equals("c")) {
    		vsComputer = true;
    		pTwoName = "Computer";
    	} else {
    		pTwoName = inpt;
    	}
    	p2 = new Player(pTwoName, "O");
    	
		System.out.println("\n" + ANSI_BLUE + p1.toString() + ANSI_RESET + " vs " + ANSI_PURPLE + p2.toString() + ANSI_RESET + "!!\n");
	}

    private void restartGame() {
    	init();
    }

	private static String[][] copyMatrix(String[][] matrix) {
		String[][] newMatrix = new String[matrix.length][];
		for(int i = 0; i < matrix.length; i++)
		{
			String[] aMatrix = matrix[i];
			int aLength = aMatrix.length;
			newMatrix[i] = new String[aLength];
			System.arraycopy(aMatrix, 0, newMatrix[i], 0, aLength);
		}
		
		return newMatrix;
	}

	private void displayBoard() {
		System.out.println("   Board");
		System.out.println(ANSI_WHITE + " ---------" + ANSI_RESET);
		for (int row = 0; row < 3; row++) {
			System.out.print(ANSI_WHITE + "|  " + ANSI_RESET);
			for (int col = 0; col < 3; col++) {
				if (board[row][col] != "X" && board[row][col] != "O")
					System.out.print(ANSI_GREEN + board[row][col] + " " + ANSI_RESET);
				else if (board[row][col] == "X")
					System.out.print(ANSI_BLUE + board[row][col] + " " + ANSI_RESET);
				else
					System.out.print(ANSI_PURPLE + board[row][col] + " " + ANSI_RESET);
			}
			System.out.println(ANSI_WHITE + " |" + ANSI_RESET);
		}
		System.out.println(ANSI_WHITE + " ---------" + ANSI_RESET);
	}
	
	public static void main(String[] args) {
		new Game();
	}
}
