package model;

import util.StreamUtil;

public class Vec2Double {
    public double x;
    public double y;

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

    public Vec2Double plus(Vec2Double another){
        return new Vec2Double(this.x + another.x, this.y + another.y);
    }

    public Vec2Double plus(double x, double y){
        return new Vec2Double(this.x + x, this.y + y);
    }

    public Vec2Double minus(Vec2Double another){
        return new Vec2Double(this.x - another.x, this.y - another.y);
    }

    public double dot(Vec2Double another){
        return this.x*another.x + this.y*another.y;
    }

    public Vec2Float toFloatVector(){
        return new Vec2Float((float)x, (float)y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
