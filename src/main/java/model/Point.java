package model;

import util.StreamUtil;

import java.util.Objects;

public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2Double buildVector(Point another){
        return new Vec2Double(this.x - another.x, this.y - another.y);
    }

    public Vec2Double buildVector(double x, double y){
        return new Vec2Double(this.x - x, this.y - y);
    }

    public Point offset(Vec2Double offset){
        return new Point(x + offset.x, y + offset.y);
    }

    public Point offset(double x, double y){
        return new Point(this.x + x, this.y + y);
    }

    public static Point readFrom(java.io.InputStream stream) throws java.io.IOException {
        return new Point(StreamUtil.readDouble(stream), StreamUtil.readDouble(stream));
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeDouble(stream, x);
        StreamUtil.writeDouble(stream, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 &&
                Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
