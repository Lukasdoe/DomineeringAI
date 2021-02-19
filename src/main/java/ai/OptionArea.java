package ai;

public class OptionArea extends Area {
    private final int weight;

    public OptionArea(int startX, int startY, int endX, int endY, int type) {
        super(startX, startY, endX, endY);
        this.weight = type;
    }

    /*
    An option area is a spot below or above a safe area which can be occupied by vertical. It could potentially decrease
    the opponent's squares (squares where he could place another tile) by 0-3. We won't count the ones which only reduce
    the square count by 0. The other's could be in one of 2 possible configurations:

            S
            S
      # E   O   E   #
     +1     1      +1
     ----------------
            S
            S
      #  #  O   E   #
      -> 2
     */

    public static OptionArea getOptionArea(char[][] board, Player player, int x, int y) {
        // minimum count for optionArea
        int count = 1;

        if (player == Player.V) {
            // a 0-OptionArea
            if (board[x][y] != 'E'
                    || ((x - 1 < 0 || board[x - 1][y] != 'E') && (x + 1 >= board.length || board[x + 1][y] != 'E'))) {
                return null;
            }
            // minimum count for optionArea
            if ((x - 2 < 0 || board[x - 2][y] == 'X') && (x > 0 && board[x - 1][y] == 'E')) {
                count++;
            }
            if ((x + 2 >= board.length || board[x + 2][y] == 'X')
                    && (x < board.length - 1 && board[x + 1][y] == 'E')) {
                count++;
            }
            return new OptionArea(x, y, x, y, count);
        }
        // a 0-OptionArea
        if (board[x][y] != 'E'
                || ((y - 1 < 0 || board[x][y - 1] != 'E') && (y + 1 >= board[0].length || board[x][y + 1] != 'E'))) {
            return null;
        }
        if ((y - 2 < 0 || board[x][y - 2] == 'X') && (y > 0 && board[x][y - 1] == 'E')) {
            count++;
        }
        if ((y + 2 >= board[0].length || board[x][y + 2] == 'X')
                && (y < board[0].length - 1 && board[x][y + 1] == 'E')) {
            count++;
        }
        return new OptionArea(x, y, x, y, count);
    }

    public int getWeight() {
        return weight;
    }
}
