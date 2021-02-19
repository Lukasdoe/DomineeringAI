package ai;

public abstract class Area {
    protected final Coordinate cornerUL;  // "upper-left"-corner
    protected final Coordinate cornerLR;   // "lower-right"-corner

    public Area(int startX, int startY, int endX, int endY) {
        cornerUL = new Coordinate(startX, startY);
        cornerLR = new Coordinate(endX, endY);
    }

    public Coordinate getCornerUL() {
        return cornerUL;
    }

    public Coordinate getCornerLR() {
        return cornerLR;
    }

    // return true if the given point is not contained in the area
    public boolean contains(int x, int y) {
        return x >= cornerLR.getX() && x <= cornerLR.getX() && y >= cornerUL.getY() && y <= cornerLR.getY();
    }
}
