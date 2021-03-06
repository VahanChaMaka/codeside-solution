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

    //vector from (0,0) to (p.x, p.y)
    public Vec2Double(Point p){
        this.x = p.x;
        this.y = p.y;
    }

    public Vec2Double setX(double x) {
        this.x = x;
        return this;
    }

    public Vec2Double setY(double y) {
        this.y = y;
        return this;
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

    public Vec2Double minus(double x, double y) {
        return new Vec2Double(this.x - x, this.y - y);
    }

    public double dot(Vec2Double another){
        return this.x*another.x + this.y*another.y;
    }

    public double length(){
        return Math.sqrt(x*x + y*y);
    }

    public Vec2Double scaleThis(double scale){
        x = x * scale;
        y = y * scale;
        return this;
    }

    public Vec2Double scale(double scale){
        return new Vec2Double(x * scale, y * scale);
    }

    public Vec2Double getNormalized(){
        return scale(1/length());
    }

    public Vec2Double normalizeThis(){
        return scaleThis(1/length());
    }

    public Vec2Double cpy(){
        return new Vec2Double(this.x, this.y);
    }

    public double angleCos(Vec2Double another){
        return this.dot(another)/(this.length()*another.length());
    }

    public Vec2Double invertThis(){
        this.x = -x;
        this.y = -y;
        return this;
    }

    public Vec2Float toFloatVector(){
        return new Vec2Float((float)x, (float)y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
