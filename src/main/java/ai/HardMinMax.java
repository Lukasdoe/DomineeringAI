package ai;

import java.util.Arrays;

public class HardMinMax extends AI {
    public float[] factors = null;

    // store the already calculated scores for each board configuration for the ultimate performance boost
    static BoardStorage scoreMapHorizontalStarter = new BoardStorage();
    static BoardStorage scoreMapVerticalStarter = new BoardStorage();

    @Override
    public synchronized Coordinate playMove(char[][] board, Player player) {
        // play an opening -> for better performance (an empty board is pretty expensive to calculate)
        Coordinate opening = BoardAnalyser.trySimpleOpening(board, player);
        if (opening != null) {
            return opening;
        }

        // for medium and hard mode, we use our minimax-algorithm
        return findBestMove(anonymizeBoard(board), player);
    }

    /*
        Because of the nature of our board layout, we only take into consideration whether a square is occupied (X) or
        empty (E). This greatly increases the performance!
     */
    private char[][] anonymizeBoard(char[][] board) {
        char[][] boardCopy = new char[board.length][board[0].length];
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                boardCopy[x][y] = (board[x][y] == 'V' || board[x][y] == 'H' ? 'X' : 'E');
            }
        }
        return boardCopy;
    }

    // tests how far the game has already commenced and adjusts the depth limit accordingly
    private int depthForBoardState(char[][] board) {
        int blocked = 0;
        for (char[] row : board) {
            for (char c : row) {
                if (c != 'E') blocked++;
            }
        }

        // for the dynamic depth adjustment, I used a sigmoid-like curve which was fitted by probing values in geogebra
        return (int) (20 / (1 + Math.pow(1.035, (-blocked + 95)))) + 1;
    }

    /*
        Recursively tests all possible moves (really slow for early board stages)

        The approach is based on the concept of a minimax tree. The root configuration is the current board. Each branch
        is a possible move for either us or the opponent. As soon as one of the players looses or the depth limit is
        reached, the current board configuration is evaluated. Each player (stage in the tree) tries to play the best
        strategy. This means is the "score" is how well we (Player A) are doing, than Player B tries to play the move
        which has the lowest score associated with it. Player A tries to play the move with the maximum score and thus
        the resulting tree is composed of alternating minimum and maximum phases.
     */
    private Coordinate findBestMove(char[][] board, Player player) {
        Coordinate[] possibleMoves = generateNextPossibleMoves(board, player, null, false);
        Coordinate currentBestMove = null;

        // this "currentBestScore" should be updated as soon as a score higher than the lowest possible score is found
        float currentBestScore = Float.NEGATIVE_INFINITY;

        int maxDepth = depthForBoardState(board);

        // just try these possibleMoves in their natural order
        for (Coordinate move : possibleMoves) {
            applyMove(board, move, player);
            float nextBestScore = minimaxAlphaBeta(
                    board,
                    player.getOtherPlayer(),
                    player,
                    maxDepth,
                    currentBestScore,
                    Float.POSITIVE_INFINITY
            );
            undoMove(board, move, player);
            // because the current player is always the maximizing player and we can't prune, we have to go through
            // each entry and always update the current maximum score and the associated move
            if (nextBestScore > currentBestScore) {
                currentBestScore = nextBestScore;
                currentBestMove = move;
            }
        }
        if (currentBestMove == null) {
            // if all fails and all scores are somehow equal to Integer.MIN_VALUE, we take the first item from the
            // generated possible moves and return it.
            return generateNextPossibleMoves(board, player, null, true)[0];
        }
        return currentBestMove;
    }

    /*
        The key in efficient solution finding is reducing the unnecessary calculations in our tree. The "scoreSituation"
        method always tests if winning is still possible and otherwise returns a low score.

        Alpha-Beta-Pruning:
            -> alpha: currently the highest score (reference for the maximising player)
            -> beta: currently the lowest score (reference for the minimising player)

            Case 1:
            -------
            We are currently the maximizing player. The previous player (minimizing) gave us his updated "beta"-score.
            Only if our score is lower than the given beta score, our branch is of interest, otherwise there exists
            another branch with a lower score and because the previous player was minimizing, he would then take the other
            branch. This means that if our current score is higher than the beta score, we can break the loop and return.

            Case 2:
            -------
            We are the minimizing player. The previous player was maximizing and gave the current maximum score in the
            alpha parameter. If our "best score" (the minimum) drops below the alpha score, we can break the search and
            return, because even though this situation would be better for us, the previous player wouldn't take our
            branch into consideration.

        This special implementation of the minimax algorithm which negates the next call with changed alpha and beta
        values is described in the doctoral thesis of Prof. dr. H.J. van den Herik: "Memory versus Search in Games".
        It also involves storing the values which were calculated for even higher performance. Some more ideas came
        from the already often cited master thesis of Nathan Bullock about the game domineering. Both papers didn't
        contain actual code or the code was not reviewed by me.
     */
    private float minimaxAlphaBeta(char[][] board, Player currentPlayer, Player startingPlayer, int depth,
                                   float alpha, float beta) {
        float oldAlpha = alpha;
        float oldBeta = beta;

        // first, try to load the score from the scoreMap
        BoardStorage.StateInfo saveState = loadScore(board, startingPlayer);

        if (saveState != null && saveState.depth >= depth) {
            if (saveState.type == '-' && saveState.score > alpha) {
                alpha = saveState.score;
            } else if (saveState.type == '+' && saveState.score < beta) {
                beta = saveState.score;
            }

            if (saveState.type == '=' || alpha >= beta) {
                return saveState.score;
            }
        }

        // The new BoardAnalyser object is used by both the scoring function and the possible moves generator. For
        // performance improvement, it is only created once.
        BoardAnalyser bA = new BoardAnalyser(board, false);
        float score = scoreSituation(
                depth,
                (startingPlayer == Player.V) ? bA.vertical : bA.horizontal,
                (startingPlayer == Player.V) ? bA.horizontal : bA.vertical
        );

        if (score != Float.NEGATIVE_INFINITY) {
            return score;
        }

        Coordinate[] possibleMoves = generateNextPossibleMoves(board, currentPlayer, bA, false);
        float nextBestScore;

        // if true -> current player tries to maximize the score
        boolean max = (currentPlayer == startingPlayer);

        // never go down / up with the score, but set the given best to be the lowest score for the "current best"
        float currentBestScore = max ? alpha : beta;

        // goes through all possible moves which are basically just placing tiles in all calculated board cover
        // regions. These regions are generated using a BoardAnalyser object.
        for (Coordinate move : possibleMoves) {
            applyMove(board, move, currentPlayer);
            nextBestScore = minimaxAlphaBeta(
                    board,
                    currentPlayer.getOtherPlayer(),
                    startingPlayer,
                    depth - 1,
                    max ? currentBestScore : alpha,
                    max ? beta : currentBestScore
            );
            undoMove(board, move, currentPlayer);

            // either we try to maximize the score, then update if the new score is higher than the current best
            // or we try to minimize the score and therefore only update if the new score is lower than the current best
            if ((max && nextBestScore > currentBestScore) || (!max && nextBestScore < currentBestScore)) {
                currentBestScore = nextBestScore;
            }

            // this is the important alpha-beta-pruning improvement over the classic minimax-algorithm. The details
            // of the implementations are written in the comment above this method.
            if ((max && currentBestScore >= beta) || (!max && currentBestScore <= alpha)) {
                break;
            }
        }

        BoardStorage.StateInfo boardState = new BoardStorage.StateInfo();
        boardState.score = currentBestScore;
        boardState.depth = depth;

        if (currentBestScore <= oldAlpha) {
            boardState.type = '+';
        } else if (currentBestScore >= oldBeta) {
            boardState.type = '-';
        } else {
            boardState.type = '=';
        }

        saveScore(board, boardState, startingPlayer);
        return currentBestScore;
    }

    private BoardStorage.StateInfo loadScore(char[][] board, Player starter) {
        return (starter == Player.V ? scoreMapVerticalStarter.get(board) : scoreMapHorizontalStarter.get(board));
    }

    private void saveScore(char[][] board, BoardStorage.StateInfo state, Player starter) {
        if (starter == Player.V) {
            scoreMapVerticalStarter.put(board, state);
        } else {
            scoreMapHorizontalStarter.put(board, state);
        }
    }

    /*
        This "exit-function" calculates the current score if a tree leaf is reached. The current method call
        represents a tree leaf if either the maximum recursion depth is reached or either one of the players can't
        play another tile or the current player already won the game because he could always place more tiles than
        the other player.

        Most switches contained in this function are based on the calculated board cover (BoardAnalyser) and the
        findings of Nathan Bullock's master thesis "Domineering: Solving Large Combinatorial Search Spaces".

        The current approach is to assign weights to each parameter we have available and figure out the optimal values.
     */
    private float scoreSituation(int currentDepth, BoardLayout starter, BoardLayout opponent) {
        if (currentDepth <= 0
                || starter.lowerBound <= 0                              // we already lost
                || opponent.lowerBound <= 0                             // the opponent already lost
                || starter.lowerBound > opponent.upperBound             // win for the starting player (NB)
                || opponent.lowerBound >= starter.upperBound            // win for the opponent (NB)
        ) {
            /*
            Available values and proposed weights / factors

            (these are just first guesses and still have to be refined)
            Basically we "punish" the ai / give worse scores for good situations of the opponent than for good
            situations for the current player.

            The concrete values were determined using an evolutional approach where two instances of
            the HardMinMax each with different factors played against each other over multiple round, with the winner
            proceeding.
             */

            if (factors == null) {
                factors = new float[]{
                        6.141892f,     // lower bound
                        3.323705f,
                        1.5304062f,     // upper bound
                        2.5675583f,
                        10.425653f,     // safe areas
                        -15.922241f,
                        2.0729046f,      // vuln areas
                        -3.497818f,
                        -3.3107972f,    // protective areas
                        -11.012934f,
                        -0.85778457f,    // unavailable areas
                        5.7875576f,
                        0.80485183f,     // unplayable areas
                        1.9488539f
                };
            }

            return (starter.lowerBound * factors[0]
                    + opponent.lowerBound * factors[1]
                    + starter.upperBound * factors[2]
                    + opponent.upperBound * factors[3]
                    + starter.numSafeAreas() * factors[4]
                    + opponent.numSafeAreas() * factors[5]
                    + (starter.numVulnAreasOne() + starter.numVulnAreasTwo()) * factors[6]
                    + (opponent.numVulnAreasOne() + opponent.numVulnAreasTwo()) * factors[7]
                    + starter.numProtectiveAreas() * factors[8]
                    + opponent.numProtectiveAreas() * factors[9]
                    + starter.unavailableSquares * factors[10]
                    + opponent.unavailableSquares * factors[11]
                    + starter.unplayableSquares * factors[12]
                    + opponent.unplayableSquares * factors[13]
            );
        }
        // if the recursion should not be stopped, return a score that would never be calculated
        // I don't know if NEGATIVE_INFINITY is better than MIN_VALUE
        return Float.NEGATIVE_INFINITY;
    }

    // returns the entered board configuration with the given move applied
    private void applyMove(char[][] board, Coordinate move, Player player) {
        // set the first square occupied
        board[move.getX()][move.getY()] = 'X';

        // based on the player, set the second square occupied
        if (player == Player.V) {
            board[move.getX()][move.getY() + 1] = 'X';
        } else {
            board[move.getX() + 1][move.getY()] = 'X';
        }
    }

    private void undoMove(char[][] board, Coordinate move, Player player) {
        // set the first square unoccupied
        board[move.getX()][move.getY()] = 'E';

        // based on the player, set the second square unoccupied
        if (player == Player.V) {
            board[move.getX()][move.getY() + 1] = 'E';
        } else {
            board[move.getX() + 1][move.getY()] = 'E';
        }
    }

    // runs an entered board analyzer or creates a new one. Returns the concatenated board cover areas.
    private Coordinate[] generateNextPossibleMoves(char[][] board, Player player, BoardAnalyser bA, boolean include) {
        if (bA == null) {
            bA = new BoardAnalyser(board, true);
        }
        // select the required BoardLayout object
        BoardLayout bL = (player == Player.V ? bA.vertical : bA.horizontal);

        // generate a new Array with approximately the maximum size required
        Coordinate[] outputMoves = new Coordinate[2 * bL.numProtectiveAreas()
                + bL.numVulnAreasOne()
                + bL.numVulnAreasTwo()
                + bL.numSafeAreas() * 3];
        /*
        The areas are concatenated to be filled in the following order:

        1. Protect spots of protective areas -> placing a tile here converts the protective area to a safe area
        2. Vulnerable areas type II
        3. Vulnerable areas type I
        4. The part of protective areas which is not the protect spot
        5. Safe areas: first the option area possibilities and then the normal safe areas
         */
        int index = 0;
        for (ProtectiveArea area : bL.protectiveAreas) {
            outputMoves[index++] = area.getProtectSpot().getCornerUL();
        }
        for (VulnArea area : bL.vulnAreasTwo) {
            outputMoves[index++] = area.getCornerUL();
        }
        for (VulnArea area : bL.vulnAreasOne) {
            outputMoves[index++] = area.getCornerUL();
        }
        for (ProtectiveArea area : bL.protectiveAreas) {
            VulnArea[] split = area.splitIntoVulnTwo(player);
            // always only add the part of the protective are which is NOT the protect spot
            if (include) {
                outputMoves[index++] = split[0].getCornerUL().equals(area.getProtectSpot().getCornerUL())
                        ? split[1].getCornerUL()
                        : split[0].getCornerUL();
            }
        }
        for (SafeArea area : bL.safeAreas) {
            if (area.getOptionSafeLower() != null) {
                outputMoves[index++] = area.getOptionSafeLower().getCornerUL();
            }
            if (area.getOptionSafeUpper() != null) {
                outputMoves[index++] = area.getOptionSafeUpper().getCornerUL();
            }
            if (include) outputMoves[index++] = area.getCornerUL();
        }
        return Arrays.copyOf(outputMoves, index);
    }
}