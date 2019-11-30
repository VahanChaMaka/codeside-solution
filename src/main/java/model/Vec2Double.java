package model;

import util.StreamUtil;

public class Vec2Double {
    private double x;
    private double y;

    public Vec2Double() {}

    public Vec2Double(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vec2Double readFrom(java.io.InputStream stream) throws java.io.IOException {
        Vec2Double result = new Vec2Double();
        result.x = StreamUtil.readDouble(stream);
        result.y = StreamUtil.readDouble(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeDouble(stream, x);
        StreamUtil.writeDouble(stream, y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
