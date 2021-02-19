package ai;

public enum Player {
    H, V;

    public Player getOtherPlayer() {
        return this == H ? V : H;
    }
}
