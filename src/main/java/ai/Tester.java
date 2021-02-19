package ai;

// small test class for running games
public class Tester {
    public static void main(String[] args) {
        AI vertical = new HardMinMax();
        AI horizontal = new HardMinMax();
        Game g = new Game(vertical, horizontal, true);
        g.runGame();
        System.out.println(g.getWinner() == vertical ? "Vertical wins!" : "Horizontal wins!");
    }
}
