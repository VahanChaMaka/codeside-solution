package model;

import util.StreamUtil;

public class ColorFloat {
    private float r;
    private float g;
    private float b;
    private float a;

    public ColorFloat() {}

    public ColorFloat(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public static ColorFloat readFrom(java.io.InputStream stream) throws java.io.IOException {
        ColorFloat result = new ColorFloat();
        result.r = StreamUtil.readFloat(stream);
        result.g = StreamUtil.readFloat(stream);
        result.b = StreamUtil.readFloat(stream);
        result.a = StreamUtil.readFloat(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeFloat(stream, r);
        StreamUtil.writeFloat(stream, g);
        StreamUtil.writeFloat(stream, b);
        StreamUtil.writeFloat(stream, a);
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }

    public static final ColorFloat RED = new ColorFloat(1, 0, 0, 1);

    public static final ColorFloat GREEN = new ColorFloat(0, 1, 0, 1);

    public static final ColorFloat BLUE = new ColorFloat(0, 0, 1, 1);
}
