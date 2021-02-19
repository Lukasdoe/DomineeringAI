package frontend;

import ai.Coordinate;
import ai.HardMinMax;
import ai.Player;
import processing.core.PApplet;

import java.util.Arrays;

public class BoardVisualizer extends PApplet {

    char[][] board;
    int rectLength;

    HardMinMax ai;
    HardMinMax ai2;

    Player curr;
    Coordinate move;

    // The argument passed to main must match the class name
    public static void main(String[] args) {
        PApplet.main("frontend.BoardVisualizer");
    }

    // method for setting the size of the window
    public void settings() {
        size(800, 800);
    }

    // identical use to setup in Processing IDE except for size()
    public void setup() {
        background(120);
        board = new char[13][13];
        for (char[] b : board) {
            Arrays.fill(b, 'E');
        }

        rectLength = width / 13;
        frameRate(60);
        ai = new HardMinMax();
        ai2 = new HardMinMax();

        strokeWeight(2);
        curr = Player.V;
    }

    // identical use to draw in Processing IDE
    public void draw() {
        if (curr == Player.V) {
            move = ai.playMove(board, Player.V);

            // is the returned move actually valid
            if (checkInvalidMoveSimple(board, move, curr)) {
                noLoop();
            }

            makeMove(board, move, curr);

            drawBoard();

            curr = curr.getOtherPlayer();
            noLoop();
        }
    }

    @Override
    public void mouseClicked() {
        move = new Coordinate(mouseX / (width / 13), mouseY / (width / 13));
        if (!checkInvalidMoveSimple(board, move, curr)) {
            makeMove(board, move, curr);
            curr = curr.getOtherPlayer();
            loop();
        }
        drawBoard();
    }

    private void drawBoard() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (board[x][y] == 'E') {
                    fill(255);
                    rect(x * rectLength, y * rectLength, rectLength, rectLength);
                } else if (board[x][y] == 'V') {
                    fill(200, 0, 0);
                    rect(x * rectLength, y * rectLength, rectLength, rectLength);
                } else if (board[x][y] == 'H') {
                    fill(0, 0, 200);
                    rect(x * rectLength, y * rectLength, rectLength, rectLength);
                }
            }
        }
    }

    private void makeMove(char[][] board, Coordinate move, Player p) {
        board[move.getX()][move.getY()] = (p == Player.H ? 'H' : 'V');
        board[move.getX() + (p == Player.H ? 1 : 0)][move.getY() + (p == Player.H ? 0 : 1)] =
                (p == Player.H ? 'H' : 'V');
    }

    // test if a returned move is valid
    private boolean checkInvalidMoveSimple(char[][] board, Coordinate move, Player p) {
        return move.getX() < 0
                || move.getX() > 13
                || move.getY() < 0
                || move.getY() > 13
                || board[move.getX()][move.getY()] != 'E'
                || (p == Player.H && board[move.getX() + 1][move.getY()] != 'E')
                || (p == Player.V && board[move.getX()][move.getY() + 1] != 'E');
    }
}