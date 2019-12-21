package model;

import util.StreamUtil;

public class LootBox {
    private Point position;
    private Vec2Double size;
    private Item item;
    private boolean isOccupied;

    public LootBox() {}

    public static LootBox readFrom(java.io.InputStream stream) throws java.io.IOException {
        LootBox result = new LootBox();
        result.position = Point.readFrom(stream);
        result.size = Vec2Double.readFrom(stream);
        result.item = Item.readFrom(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        position.writeTo(stream);
        size.writeTo(stream);
        item.writeTo(stream);
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Vec2Double getSize() {
        return size;
    }

    public void setSize(Vec2Double size) {
        this.size = size;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    @Override
    public String toString() {
        return "LootBox{" +
                "position=" + position +
                ", size=" + size +
                ", item=" + item +
                '}';
    }
}
