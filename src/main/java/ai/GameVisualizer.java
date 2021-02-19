package ai;

public class GameVisualizer {

    public static void printBoard(char[][] board) {
        System.out.print(" ");
        for (int x = 0; x < board.length; x++) {
            System.out.print(" " + Integer.toHexString(x));
        }
        System.out.println();

        System.out.print(" ┏");
        for (int x = 0; x < board.length - 1; x++) {
            System.out.print("━┳");
        }
        System.out.println("━┓");

        for (int y = 0; y < board[0].length; y++) {
            System.out.print(Integer.toHexString(y) + "┃");
            for (char[] chars : board) {
                if (chars[y] != 'E') {
                    System.out.print(chars[y] + "┃");
                } else {
                    System.out.print(" ┃");
                }
            }
            System.out.println();
            if (y != board[0].length - 1) {
                System.out.print(" ┣");
                for (int x = 0; x < board.length - 1; x++) {
                    System.out.print("━╋");
                }
                System.out.println("━┫");
            }
        }
        System.out.print(" ┗");
        for (int x = 0; x < board.length - 1; x++) {
            System.out.print("━┻");
        }
        System.out.println("━┛");
    }
}
