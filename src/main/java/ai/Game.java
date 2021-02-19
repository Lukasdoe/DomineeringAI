package ai;

import java.util.Arrays;

public class Game {
    private static final int BOARD_LENGTH = 13;
    private final AI verticalAI;
    private final AI horizontalAI;
    private final boolean visual;
    private Player winner;

    public Game(AI verticalAI, AI horizontalAI) {
        this.verticalAI = verticalAI;
        this.horizontalAI = horizontalAI;
        this.visual = false;
    }

    // a visual game prints the current state of the board to the console after each move
    public Game(AI verticalAI, AI horizontalAI, boolean visual) {
        this.verticalAI = verticalAI;
        this.horizontalAI = horizontalAI;
        this.visual = visual;
    }

    public void runGame() {
        // start by initializing a new game, this way, runGame() could potentially be run more than once
        char[][] board = generateEmptyBoard();
        Coordinate move;

        // starting player -> always the vertical player for our game version
        Player currentPlayer = Player.V;

        if (visual) GameVisualizer.printBoard(board);
        while (true) {
            // is there another valid move possible for the current player
            if (!canPlay(board, currentPlayer)) {
                if (visual) System.out.println("" + currentPlayer + " can't place another piece, he lost!");
                break;
            }
            if (visual) System.out.println("" + currentPlayer + "'s move:");
            // ask the AI for a new move (coordinates for a new piece)
            if (currentPlayer == Player.V) {
                move = verticalAI.playMove(board, currentPlayer);
            } else {
                move = horizontalAI.playMove(board, currentPlayer);
            }

            if (visual) System.out.println("Places a piece on " + move);

            // is the returned move actually valid
            if (checkInvalidMoveSimple(board, move, currentPlayer)) {
                if (visual) System.out.println("!!!! INVALID MOVE BY " + currentPlayer + " !!!!");
                break;
            }

            makeMove(board, move, currentPlayer);
            if (visual) GameVisualizer.printBoard(board);

            // change the play for the next round
            currentPlayer = currentPlayer.getOtherPlayer();
        }
        winner = currentPlayer.getOtherPlayer();
    }

    // generate an empty board filled with 'E'
    private char[][] generateEmptyBoard() {
        char[][] board = new char[BOARD_LENGTH][BOARD_LENGTH];
        for (char[] column : board) {
            Arrays.fill(column, 'E');
        }
        return board;
    }

    // fulfill a returned move if it is valid
    private void makeMove(char[][] board, Coordinate move, Player p) {
        board[move.getX()][move.getY()] = (p == Player.H ? 'H' : 'V');
        board[move.getX() + (p == Player.H ? 1 : 0)][move.getY() + (p == Player.H ? 0 : 1)] =
                (p == Player.H ? 'H' : 'V');
    }

    // test if a returned move is valid
    private boolean checkInvalidMoveSimple(char[][] board, Coordinate move, Player p) {
        return move.getX() < 0
                || move.getX() >= BOARD_LENGTH
                || move.getY() < 0
                || move.getY() >= BOARD_LENGTH
                || (p == Player.H && move.getX() + 1 >= BOARD_LENGTH)
                || (p == Player.V && move.getY() + 1 >= BOARD_LENGTH)
                || board[move.getX()][move.getY()] != 'E'
                || (p == Player.H && board[move.getX() + 1][move.getY()] != 'E')
                || (p == Player.V && board[move.getX()][move.getY() + 1] != 'E');
    }

    // is there any move left for the current player? If not, the other player has one!
    private boolean canPlay(char[][] board, Player p) {
        if (p == Player.H) {
            for (int y = 0; y < board[0].length; y++) {
                for (int x = 0; x < board.length - 1; x++) {
                    if (board[x][y] == 'E' && board[x + 1][y] == 'E') {
                        return true;
                    }
                }
            }
        } else {
            for (char[] column : board) {
                for (int y = 0; y < column.length - 1; y++) {
                    if (column[y] == 'E' && column[y + 1] == 'E') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public AI getWinner() {
        return winner == Player.V ? verticalAI : horizontalAI;
    }
}