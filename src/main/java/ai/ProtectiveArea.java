package ai;

import java.util.ArrayList;

public class ProtectiveArea extends Area {

    private final VulnArea protectSpot;

    public ProtectiveArea(int startX, int startY, int endX, int endY, char[][] board, Player p) {
        super(startX, startY, endX, endY);

        // switch for the player to position the protect spot correctly (horizontally or vertically)
        if (p == Player.V) {
            if (endX + 1 >= board.length || (board[endX + 1][startY] != 'E' && board[endX + 1][endY] != 'E')) {
                protectSpot = new VulnArea(startX, startY, startX, startY + 1);
            } else {
                protectSpot = new VulnArea(endX, startY, endX, startY + 1);
            }
        } else if (endY + 1 >= board[0].length || (board[startX][endY + 1] != 'E' && board[endX][endY + 1] != 'E')) {
            protectSpot = new VulnArea(startX, startY, startX + 1, startY);
        } else {
            protectSpot = new VulnArea(startX, endY, endX, endY);
        }
    }

    /*
    The input coordinates describe a protective area if both sides (left and right for Player.V and up and down for Player.H)
    are contained with other tiles or the borders. These conditions are tested in the overlong expressions below.
     */
    public static boolean isProtectiveArea(char[][] board, Player p, int x, int y,
                                           ArrayList<ProtectiveArea> addedAreas) {
        if (p == Player.V) {
            return (isNotOccupied(board, p, x, y)
                    && noAdjacentProtectiveArea(p, x, y, addedAreas)

                    && (((x - 1 < 0)
                    || ((board[x - 1][y] == 'X')
                    && (board[x - 1][y + 1] == 'X')))

                    || ((x + 2 >= board.length)
                    || ((board[x + 2][y] == 'X')
                    && (board[x + 2][y + 1] == 'X')))));
        } else {
            return (isNotOccupied(board, p, x, y)
                    && noAdjacentProtectiveArea(p, x, y, addedAreas)

                    && (((y - 1 < 0)
                    || ((board[x][y - 1] == 'X')
                    && (board[x + 1][y - 1] == 'X')))

                    || ((y + 2 >= board[0].length)
                    || ((board[x][y + 2] == 'X')
                    && (board[x + 1][y + 2] == 'X')))));
        }
    }

    private static boolean noAdjacentProtectiveArea(Player p, int x, int y, ArrayList<ProtectiveArea> addedAreas) {
        // an area is considered adjacent if the other player can occupy both areas by placing one piece on the board
        if (p == Player.V) {
            for (ProtectiveArea protectiveArea : addedAreas) {
                if (protectiveArea.getCornerUL().getY() >= y - 1 && protectiveArea.getCornerLR().getY() <= y + 2) {
                    if (x - 1 == protectiveArea.getCornerLR().getX() || x + 2 == protectiveArea.getCornerUL().getX()) {
                        return false;
                    }
                }
            }
        } else {
            for (ProtectiveArea protectiveArea : addedAreas) {
                if (protectiveArea.getCornerUL().getX() >= x - 1 && protectiveArea.getCornerLR().getX() <= x + 2) {
                    if (y - 1 == protectiveArea.getCornerLR().getY() || y + 2 == protectiveArea.getCornerUL().getY()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     Is the given x/y - coordinate already occupied:

     - part of the area is outside of the board
     - the area is not empty

     */
    public static boolean isNotOccupied(char[][] board, Player player, int x, int y) {
        return board[x][y] == 'E'
                && (!(x + 1 >= board.length) && board[x + 1][y] == 'E')
                && (!(y + 1 >= board[0].length) && board[x][y + 1] == 'E')
                && board[x + 1][y + 1] == 'E';
//        return (board[x][y] == 'E' || board[x][y] == 'P')
//                && (!(x + 1 >= board.length) && (board[x + 1][y] == 'E' || board[x + 1][y] == 'P'))
//                && (!(y + 1 >= board[0].length) && (board[x][y + 1] == 'E' || board[x][y + 1] == 'P'))
//                && (board[x + 1][y + 1] == 'E' || board[x + 1][y + 1] == 'P');
    }

    // Split the protective area into two vulnerable areas
    public VulnArea[] splitIntoVulnTwo(Player player) {
        if (player == Player.H) {
            if (protectSpot.getCornerLR().getY() == cornerLR.getY()) {
                return new VulnArea[]{
                        protectSpot, new VulnArea(
                        cornerUL.getX(), cornerUL.getY(), cornerLR.getX(), cornerUL.getY()
                )
                };
            } else {
                return new VulnArea[]{
                        protectSpot, new VulnArea(
                        cornerUL.getX(), cornerLR.getY(), cornerLR.getX(), cornerLR.getY()
                )
                };
            }
        } else if (protectSpot.getCornerLR().getX() == cornerLR.getX()) {
            return new VulnArea[]{
                    protectSpot, new VulnArea(
                    cornerUL.getX(), cornerUL.getY(), cornerUL.getX(), cornerLR.getY()
            )
            };
        } else {
            return new VulnArea[]{
                    protectSpot, new VulnArea(
                    cornerLR.getX(), cornerUL.getY(), cornerLR.getX(), cornerLR.getY()
            )
            };
        }
    }

    // the protect spot is the half of the protective area which should be occupied first in order to convert the
    // protective area to a safe area!
    public VulnArea getProtectSpot() {
        return protectSpot;
    }
}
