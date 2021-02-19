package ai;

import java.util.Arrays;
import java.util.Random;

public final class BoardAnalyser {
    private final Random generator;

    public final BoardLayout vertical;
    public final BoardLayout horizontal;

    private final char[][] board;

    public BoardAnalyser(char[][] board, boolean noBounds) {
        this.generator = new Random();
        this.board = board;

        this.vertical = new BoardLayout(Player.V);
        this.horizontal = new BoardLayout(Player.H);

        // initialize the internal variables
        analyseBoard();
        if (!noBounds) {
            calcUnplayable();
            calcLowerBounds();
            calcUpperBounds();
        }
    }

    /*
     We try to play one of four openings for domineering:
     ------------------
     |              # |
     |##            # |
     |                |
     |                |
     |                |
     | #            ##|
     | #              |
     ------------------
     */
    // to be able to play an opening without the performance loss of analyzing the entire board, this is a static method
    public static Coordinate trySimpleOpening(char[][] board, Player player) {
        // Horizontal
        if (player == Player.H) {
            if (board[board.length - 1][board[0].length - 2] == 'E'
                    && board[board.length - 2][board[0].length - 2] == 'E') {
                return new Coordinate(board.length - 2, board[0].length - 2);
            }
            if (board[0][1] == 'E' && board[1][1] == 'E') {
                return new Coordinate(0, 1);
            }
        } else {
            // Vertical
            if (board[board.length - 2][0] == 'E' && board[board.length - 2][1] == 'E') {
                return new Coordinate(board.length - 2, 0);
            }
            if (board[1][board[0].length - 2] == 'E' && board[1][board[0].length - 1] == 'E') {
                return new Coordinate(1, board[0].length - 2);
            }
        }
        // no simple opening possible
        return null;
    }

    // scan board for special "areas" which contain all playable moves
    public void analyseBoard() {
        // copy board
        char[][] boardCloneVertical = new char[board.length][];
        char[][] boardCloneHorizontal = new char[board.length][];

        for (int i = 0; i < board.length; i++) {
            boardCloneVertical[i] = Arrays.copyOf(board[i], board[i].length);
            boardCloneHorizontal[i] = Arrays.copyOf(board[i], board[i].length);
        }

        /* 
        Go through board and scan for "safe areas" and "protective areas":
        
        Safe areas are spots where the current player could place his tile, but the other player can never obstruct.
        We should keep these areas as they can't be blocked by the other player.
        
        Protective areas are 2x2 areas which can be converted to "safe areas" by placing a tile on the internally saved
        "protectSpot". This move converts a protective area to a safe area. No two protective areas are allowed to be
        adjacent to each other. Otherwise the other player could destroy two protective areas by placing one tile.
        */
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (y < board[0].length - 1 && SafeArea.isSafeArea(boardCloneVertical, Player.V, x, y)) {
                    vertical.safeAreas.add(new SafeArea(x, y, x, y + 1));

                    // mark the "used spaces" in the board clone so they aren't reused for other areas
                    boardCloneVertical[x][y] = 'S';
                    boardCloneVertical[x][y + 1] = 'S';
                }
                if (x < board.length - 1 && SafeArea.isSafeArea(boardCloneHorizontal, Player.H, x, y)) {
                    horizontal.safeAreas.add(new SafeArea(x, y, x + 1, y));

                    // mark the "used spaces" in the board clone so they aren't reused for other areas
                    boardCloneHorizontal[x][y] = 'S';
                    boardCloneHorizontal[x + 1][y] = 'S';
                }

                if (y < board[0].length - 1
                        && ProtectiveArea
                        .isProtectiveArea(boardCloneVertical, Player.V, x, y, vertical.protectiveAreas)) {
                    vertical.protectiveAreas.add(new ProtectiveArea(x, y, x + 1, y + 1, board, Player.V));

                    // mark the 2x2 protective area on the board copy
                    boardCloneVertical[x][y] = 'P';
                    boardCloneVertical[x + 1][y] = 'P';
                    boardCloneVertical[x][y + 1] = 'P';
                    boardCloneVertical[x + 1][y + 1] = 'P';
                }

                if (x < board.length - 1
                        && ProtectiveArea
                        .isProtectiveArea(boardCloneHorizontal, Player.H, x, y, horizontal.protectiveAreas)) {
                    horizontal.protectiveAreas.add(new ProtectiveArea(x, y, x + 1, y + 1, board, Player.H));

                    // mark the 2x2 protective area on the board copy
                    boardCloneHorizontal[x][y] = 'P';
                    boardCloneHorizontal[x + 1][y] = 'P';
                    boardCloneHorizontal[x][y + 1] = 'P';
                    boardCloneHorizontal[x + 1][y + 1] = 'P';
                }
            }
        }

        // scan all safe areas for the so called "option areas". This means player could place the piece into one
        // square contained in the safe area and one square which is outside. This mustn't reduce the number of safe
        // moves or destroy another protective area. It is really useful to reduce the opponent's possible moves.
        for (SafeArea safeArea : vertical.safeAreas) {
            int x = safeArea.getCornerUL().getX();
            int y = safeArea.getCornerUL().getY();

            if (y + 2 < board[0].length) {
                OptionArea oaLower = OptionArea
                        .getOptionArea(boardCloneVertical, Player.V, x, y + 2);
                if (oaLower != null) {
                    vertical.optionAreas.add(oaLower);
                    safeArea.addOptionAreaLower(oaLower);
                }
            }
            if (y - 1 > 0) {
                OptionArea oaHigher = OptionArea
                        .getOptionArea(boardCloneVertical, Player.V, x, y - 1);
                if (oaHigher != null) {
                    vertical.optionAreas.add(oaHigher);
                    safeArea.addOptionAreaHigher(oaHigher);
                }
            }
        }

        for (SafeArea safeArea : horizontal.safeAreas) {
            int x = safeArea.getCornerUL().getX();
            int y = safeArea.getCornerUL().getY();

            if (x + 2 < board.length) {
                OptionArea oaLower = OptionArea
                        .getOptionArea(boardCloneHorizontal, Player.H, x + 2, y);
                if (oaLower != null) {
                    horizontal.optionAreas.add(oaLower);
                    safeArea.addOptionAreaLower(oaLower);
                }
            }
            if (x - 1 > 0) {
                OptionArea oaHigher = OptionArea
                        .getOptionArea(boardCloneHorizontal, Player.H, x - 1, y);
                if (oaHigher != null) {
                    horizontal.optionAreas.add(oaHigher);
                    safeArea.addOptionAreaHigher(oaHigher);
                }
            }
        }

        // used for temporary storage for the vulnerable area type
        int type;

        // all places which are left and aren't single 1x1 fields are vulnerable areas
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (y < board[0].length - 1 && (type = VulnArea.isVulnArea(boardCloneVertical, x, y, Player.V)) != 0) {
                    if (type == 1) {
                        vertical.vulnAreasOne.add(VulnArea.getVulnArea(x, y, Player.V));
                        boardCloneVertical[x][y] = 'D';
                        boardCloneVertical[x][y + 1] = 'D';
                    } else if (type == 2) {
                        vertical.vulnAreasTwo.add(VulnArea.getVulnArea(x, y, Player.V));
                        boardCloneVertical[x][y] = 'D';
                        boardCloneVertical[x][y + 1] = 'D';
                    } else if (type == 3) {
                        // the vulnerable areas which contain a protected square are counted twice
                        vertical.vulnAreasOne.add(VulnArea.getVulnArea(x, y, Player.V));
                        vertical.vulnAreasProtectedOne.add(VulnArea.getVulnArea(x, y, Player.V));
                        boardCloneVertical[x][y] = 'D';
                        boardCloneVertical[x][y + 1] = 'D';
                    } else if (type == 4) {
                        vertical.vulnAreasTwo.add(VulnArea.getVulnArea(x, y, Player.V));
                        vertical.vulnAreasProtectedTwo.add(VulnArea.getVulnArea(x, y, Player.V));
                        boardCloneVertical[x][y] = 'D';
                        boardCloneVertical[x][y + 1] = 'D';
                    }
                }

                if (x < board.length - 1
                        && (type = VulnArea.isVulnArea(boardCloneHorizontal, x, y, Player.H)) != 0) {
                    if (type == 1) {
                        horizontal.vulnAreasOne.add(VulnArea.getVulnArea(x, y, Player.H));
                        boardCloneHorizontal[x][y] = 'D';
                        boardCloneHorizontal[x + 1][y] = 'D';
                    } else if (type == 2) {
                        horizontal.vulnAreasTwo.add(VulnArea.getVulnArea(x, y, Player.H));
                        boardCloneHorizontal[x][y] = 'D';
                        boardCloneHorizontal[x + 1][y] = 'D';
                    } else if (type == 3) {
                        // the vulnerable areas which contain a protected square are counted twice
                        horizontal.vulnAreasOne.add(VulnArea.getVulnArea(x, y, Player.H));
                        horizontal.vulnAreasProtectedOne.add(VulnArea.getVulnArea(x, y, Player.H));
                        boardCloneHorizontal[x][y] = 'D';
                        boardCloneHorizontal[x + 1][y] = 'D';
                    } else if (type == 4) {
                        horizontal.vulnAreasTwo.add(VulnArea.getVulnArea(x, y, Player.H));
                        horizontal.vulnAreasProtectedTwo.add(VulnArea.getVulnArea(x, y, Player.H));
                        boardCloneHorizontal[x][y] = 'D';
                        boardCloneHorizontal[x + 1][y] = 'D';
                    }
                }
            }
        }

        // the last thing to do is to sum up all squares which were available from the start and also which could be
        // played by either player.
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (board[x][y] == 'E') {
                    horizontal.startAvailableSquares++;
                    vertical.startAvailableSquares++;
                }

                if ((boardCloneVertical[x][y] == 'E')
                        && ((x <= 0 || board[x - 1][y] != 'E') && (x + 1 >= board.length || board[x + 1][y] != 'E'))) {
                    horizontal.unavailableSquares++;
                }

                if ((boardCloneHorizontal[x][y] == 'E')
                        && ((y <= 0 || board[x][y - 1] != 'E') && (y + 1 >= board[0].length
                        || board[x][y + 1] != 'E'))) {
                    vertical.unavailableSquares++;
                }
            }
        }
    }

    // this lower bound denotes the minimum number of moves the current player is able to play
    // the main strategy and calculation is based on Nathan Bullock's master thesis (theorem 3.5.1)
    private void calcLowerBounds() {
        if (vertical.numProtectiveAreas() % 2 != 0) {
            ProtectiveArea convertibleArea =
                    vertical.protectiveAreas.get(generator.nextInt(vertical.numProtectiveAreas()));
            vertical.protectiveAreas.remove(convertibleArea);

            VulnArea[] newVulnAreas = convertibleArea.splitIntoVulnTwo(Player.V);

            vertical.vulnAreasTwo.add(newVulnAreas[0]);
            vertical.vulnAreasTwo.add(newVulnAreas[1]);
        }

        int addMove = (vertical.numVulnAreasTwo() % 3 != 0 && vertical.numVulnAreasOne() % 2 != 0) ? 1 : 0;

        vertical.lowerBound = (vertical.numProtectiveAreas()
                + vertical.numVulnAreasTwo() / 3
                + vertical.numVulnAreasOne() / 2
                + vertical.numSafeAreas()
                + addMove
        );

        if (horizontal.numProtectiveAreas() % 2 != 0) {
            ProtectiveArea convertibleArea =
                    horizontal.protectiveAreas.get(generator.nextInt(horizontal.numProtectiveAreas()));
            horizontal.protectiveAreas.remove(convertibleArea);

            VulnArea[] newVulnAreas = convertibleArea.splitIntoVulnTwo(Player.H);

            horizontal.vulnAreasTwo.add(newVulnAreas[0]);
            horizontal.vulnAreasTwo.add(newVulnAreas[1]);
        }

        addMove = (horizontal.numVulnAreasTwo() % 3 != 0 && horizontal.numVulnAreasOne() % 2 != 0) ? 1 : 0;

        horizontal.lowerBound = (horizontal.numProtectiveAreas()
                + horizontal.numVulnAreasTwo() / 3
                + horizontal.numVulnAreasOne() / 2
                + horizontal.numSafeAreas()
                + addMove
        );
    }

    // get the upper bound of moves which could be played by the corresponding player.
    private void calcUpperBounds() {
        // first, calculate the upper bound for vertical
        // number of playable squares after the opponent has played his lower bound of moves
        int squaresCurr = (vertical.startAvailableSquares - 2 * horizontal.lowerBound);
        // the upper bound is the number of playable squares divided by 2 -> minimum number of playable tiles
        vertical.upperBound = (squaresCurr - vertical.unavailableSquares - vertical.unplayableSquares) / 2;

        // calculate the upper bound for horizontal
        squaresCurr = (horizontal.startAvailableSquares - 2 * vertical.lowerBound);
        horizontal.upperBound = (squaresCurr - horizontal.unavailableSquares - horizontal.unplayableSquares) / 2;
    }

    private void calcUnplayable() {
        // first, calculate the unplayable squares for vertical (use the data from horizontal player)
        BoardLayout current = vertical;
        BoardLayout opponent = horizontal;

        for (int i = 0; i < 2; i++) {
            int o1 = 0;
            int o2 = 0;
            int o3 = 0;

            for (OptionArea o : opponent.optionAreas) {
                switch (o.getWeight()) {
                    case 1 -> o1++;
                    case 2 -> o2++;
                    case 3 -> o3++;
                }
            }

            int addMove1 = (opponent.numVulnAreasTwo() % 3 != 0
                    && opponent.numVulnAreasOne() % 2 != 0
                    && (opponent.numVulnAreasProtectedTwo() > 0 || opponent.numVulnAreasProtectedOne() > 0)
            ) ? -1 : 0;

            int addMove2 = 0;
            if (!(opponent.numVulnAreasTwo() % 3 != 0 && opponent.numVulnAreasOne() % 2 != 0)
                    && !(opponent.numVulnAreasTwo() % 3 == 0 && opponent.numVulnAreasOne() % 2 == 0)) {
                if (o3 % 2 == 1) {
                    addMove2 = 3;
                } else if (o2 % 2 == 1) {
                    addMove2 = 2;
                } else if (o1 % 2 == 1) addMove2 = 1;
            }

            current.unplayableSquares = (opponent.numVulnAreasProtectedTwo()
                    - (opponent.numVulnAreasTwo() / 3
                    - (opponent.numVulnAreasTwo() - opponent.numVulnAreasProtectedTwo()) / 3))
                    + (opponent.numVulnAreasProtectedOne()
                    - (opponent.numVulnAreasOne() / 2
                    - (opponent.numVulnAreasOne() - opponent.numVulnAreasProtectedOne()) / 2))
                    + 3 * (o3 / 2) + 2 * (o2 / 2) + o1 / 2 + addMove1 + addMove2;

            // then, do the same thing for horizontal
            current = horizontal;
            opponent = vertical;
        }
    }
}
