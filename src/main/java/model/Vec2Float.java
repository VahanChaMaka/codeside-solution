package model;

import util.StreamUtil;

public class Vec2Float {
    public float x;
    public float y;

    public Vec2Float() {}

    public Vec2Float(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vec2Float readFrom(java.io.InputStream stream) throws java.io.IOException {
        Vec2Float result = new Vec2Float();
        result.x = StreamUtil.readFloat(stream);
        result.y = StreamUtil.readFloat(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeFloat(stream, x);
        StreamUtil.writeFloat(stream, y);
    }

    public Vec2Float plus(Vec2Float another){
        return new Vec2Float(this.x + another.x, this.y + another.y);
    }

    public Vec2Float minus(Vec2Float another){
        return new Vec2Float(this.x - another.x, this.y - another.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
