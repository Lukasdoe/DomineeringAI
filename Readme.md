# Domineering AI

The game [domineering](https://en.wikipedia.org/wiki/Domineering) has very few rules and is therefore easy to learn.
However, the strategies which evolve can be more complex than one might think at first glance.

Domineering can be played on a chess board in any rectangular shape, increasing the complexity with the overall size of
the board. Two players, here called "V" for vertical and "H" for horizontal, each place a domino pieces onto the board. 
The pieces may not overlap each other and can only be placed fully on the board. 

The first player who is unable to legally place another piece has lost the game.

To compute the winning strategy for the game, one has to compute a score for the possible board configurations which
result in the currently possible moves, the player could make. Because the number of possible moves and board
configurations eventually rises exponentially with bigger boards, we need a good strategy to sort out bad moves quickly.

For this AI, I chose to use a combination of a min-max-algorithm with alpha-beta-pruning. For move ordering, I based my
board scoring and move evaluation algorithm on methods for move counting and scanning from Nathan Bullocks master thesis.

Depending on how long the AI is allowed to "think" about the next move, the "depthForBoardState" function should be
altered. It is responsible for evaluating the search depth, aka. the number of next moves, the ai should take into
consideration.