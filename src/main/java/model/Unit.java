package model;

import util.StreamUtil;

public class Unit {
    private int playerId;
    private int id;
    private int health;
    private Vec2Double position;
    private Vec2Double size;
    private JumpState jumpState;
    private boolean walkedRight;
    private boolean stand;
    private boolean onGround;
    private boolean onLadder;
    private int mines;
    private Weapon weapon;

    public Unit() {}

    public Unit(int playerId, int id, int health, Vec2Double position, Vec2Double size,
                JumpState jumpState, boolean walkedRight, boolean stand, boolean onGround,
                boolean onLadder, int mines, Weapon weapon) {
        this.playerId = playerId;
        this.id = id;
        this.health = health;
        this.size = size;
        this.jumpState = jumpState;
        this.walkedRight = walkedRight;
        this.stand = stand;
        this.onGround = onGround;
        this.onLadder = onLadder;
        this.mines = mines;
        this.weapon = weapon;
    }

    public static Unit readFrom(java.io.InputStream stream) throws java.io.IOException {
        Unit result = new Unit();
        result.playerId = StreamUtil.readInt(stream);
        result.id = StreamUtil.readInt(stream);
        result.health = StreamUtil.readInt(stream);
        result.position = Vec2Double.readFrom(stream);
        result.size = Vec2Double.readFrom(stream);
        result.jumpState = JumpState.readFrom(stream);
        result.walkedRight = StreamUtil.readBoolean(stream);
        result.stand = StreamUtil.readBoolean(stream);
        result.onGround = StreamUtil.readBoolean(stream);
        result.onLadder = StreamUtil.readBoolean(stream);
        result.mines = StreamUtil.readInt(stream);
        if (StreamUtil.readBoolean(stream)) {
            result.weapon = Weapon.readFrom(stream);
        } else {
            result.weapon = null;
        }
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, playerId);
        StreamUtil.writeInt(stream, id);
        StreamUtil.writeInt(stream, health);
        position.writeTo(stream);
        size.writeTo(stream);
        jumpState.writeTo(stream);
        StreamUtil.writeBoolean(stream, walkedRight);
        StreamUtil.writeBoolean(stream, stand);
        StreamUtil.writeBoolean(stream, onGround);
        StreamUtil.writeBoolean(stream, onLadder);
        StreamUtil.writeInt(stream, mines);
        if (weapon == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            weapon.writeTo(stream);
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Vec2Double getPosition() {
        return position;
    }

    //correct position to center
    public Vec2Double getPositionForShooting(){
        return position.plus(0, 0.9);
    }

    public void setPosition(Vec2Double position) {
        this.position = position;
    }

    public Vec2Double getSize() {
        return size;
    }

    public void setSize(Vec2Double size) {
        this.size = size;
    }

    public JumpState getJumpState() {
        return jumpState;
    }

    public void setJumpState(JumpState jumpState) {
        this.jumpState = jumpState;
    }

    public boolean isWalkedRight() {
        return walkedRight;
    }

    public void setWalkedRight(boolean walkedRight) {
        this.walkedRight = walkedRight;
    }

    public boolean isStand() {
        return stand;
    }

    public void setStand(boolean stand) {
        this.stand = stand;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isOnLadder() {
        return onLadder;
    }

    public void setOnLadder(boolean onLadder) {
        this.onLadder = onLadder;
    }

    public int getMines() {
        return mines;
    }

    public void setMines(int mines) {
        this.mines = mines;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "playerId=" + playerId +
                ", id=" + id +
                ", health=" + health +
                ", position=" + position +
                ", size=" + size +
                ", jumpState=" + jumpState +
                ", walkedRight=" + walkedRight +
                ", stand=" + stand +
                ", onGround=" + onGround +
                ", onLadder=" + onLadder +
                ", mines=" + mines +
                ", weapon=" + weapon +
                '}';
    }
}
