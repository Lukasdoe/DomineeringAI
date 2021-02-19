package ai;

/*
 Vulnerable areas are all spots which are not protective areas or safe areas
 The weight is determined by the neighbourhood. If another special area is adjacent to the vulnerable area, the other
 player could place a tile in a way which would obstruct both areas. We give these vulnerable areas a weight of 2. All
 others get a weight of one.
*/
public class VulnArea extends Area {

    public VulnArea(int xOne, int yOne, int xTwo, int yTwo) {
        super(xOne, yOne, xTwo, yTwo);
    }

    public static int isVulnArea(char[][] board, int x, int y, Player player) {
        // Horizontal and vertical modifier
        int vM = (player == Player.V ? 1 : 0);
        int hM = (player == Player.H ? 1 : 0);

        int type = 0;

        if (board[x][y] == 'E' && board[x + hM][y + vM] == 'E') {
            // if other already existing areas are adjacent to the vulnerable area, it is considered a type II
            if (player == Player.V && (
                    (x - 1 >= 0 && (board[x - 1][y] == 'P' || board[x - 1][y] == 'S' || board[x - 1][y] == 'D'))
                            || (x + 1 < board.length && y < board.length
                            && (board[x + 1][y] == 'P' || board[x + 1][y] == 'S' || board[x + 1][y] == 'D'))
                            || (x - 1 >= 0 && y + 1 <= board.length
                            && (board[x - 1][y + 1] == 'P' || board[x - 1][y + 1] == 'S' || board[x - 1][y + 1] == 'D'))
                            || (x + 1 < board.length && y + 1 < board.length
                            && (board[x + 1][y + 1] == 'P' || board[x + 1][y + 1] == 'S'
                            || board[x + 1][y + 1] == 'D')))) {
                type = 2;
            } else if (player == Player.H && ((y > 0 && (board[x][y - 1] == 'P' || board[x][y - 1] == 'S'))
                    || (y + 1 < board[0].length && (board[x][y + 1] == 'P' || board[x][y + 1] == 'S'
                    || board[x][y + 1] == 'D'))
                    || (x + 1 < board.length && y > 0
                    && (board[x + 1][y - 1] == 'P' || board[x + 1][y - 1] == 'S' || board[x + 1][y - 1] == 'D'))
                    || (x + 1 < board.length && y + 1 < board[0].length
                    && (board[x + 1][y + 1] == 'P' || board[x + 1][y + 1] == 'S' || board[x + 1][y + 1] == 'D')))) {
                type = 2;
            } else {
                // Vulnerable Area Type I if no other area is adjacent to it
                type = 1;
            }
        }

        if (type > 0) {
            if (player == Player.V && (((x <= 0 || board[x - 1][y] == 'X')
                    && (x >= board.length - 1 || board[x + 1][y] == 'X'))
                    || ((x <= 0 || board[x - 1][y + 1] == 'X')
                    && (x >= board.length - 1 || board[x + 1][y + 1] == 'X')))) {
                type += 2;
            }
            if (player == Player.H && (((y <= 0 || board[x][y - 1] == 'X')
                    && (y >= board[0].length - 1 || board[x][y + 1] == 'X'))
                    || ((y <= 0 || board[x + 1][y - 1] == 'X')
                    && (y >= board[0].length - 1 || board[x + 1][y + 1] == 'X')))) {
                type += 2;
            }
        }
        return type;
    }

    public static VulnArea getVulnArea(int x, int y, Player player) {
        return new VulnArea(x, y, x + (player == Player.H ? 1 : 0), y + (player == Player.V ? 1 : 0));
    }
}
