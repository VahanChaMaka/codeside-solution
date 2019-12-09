package model;

public class Wall {
    public final Point first;
    public final Point second;
    public final boolean isVertical;

    public Wall(Point first, Point second, boolean isVertical) {
        this.first = first;
        this.second = second;
        this.isVertical = isVertical;
    }

    public Vec2Double asVector(){
        return second.buildVector(first);
    }
}
