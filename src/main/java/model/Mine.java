package model;

import util.StreamUtil;

public class Mine {
    private int playerId;
    private Vec2Double position;
    private Vec2Double size;
    private MineState state;
    private Double timer;
    private double triggerRadius;
    private ExplosionParams explosionParams;

    public Mine() {}

    public Mine(int playerId, Vec2Double position, Vec2Double size, MineState state,
                Double timer, double triggerRadius, ExplosionParams explosionParams) {
        this.playerId = playerId;
        this.position = position;
        this.size = size;
        this.state = state;
        this.timer = timer;
        this.triggerRadius = triggerRadius;
        this.explosionParams = explosionParams;
    }

    public static Mine readFrom(java.io.InputStream stream) throws java.io.IOException {
        Mine result = new Mine();
        result.playerId = StreamUtil.readInt(stream);
        result.position = Vec2Double.readFrom(stream);
        result.size = Vec2Double.readFrom(stream);
        switch (StreamUtil.readInt(stream)) {
        case 0:
            result.state = MineState.PREPARING;
            break;
        case 1:
            result.state = MineState.IDLE;
            break;
        case 2:
            result.state = MineState.TRIGGERED;
            break;
        case 3:
            result.state = MineState.EXPLODED;
            break;
        default:
            throw new java.io.IOException("Unexpected discriminant value");
        }
        if (StreamUtil.readBoolean(stream)) {
            result.timer = StreamUtil.readDouble(stream);
        } else {
            result.timer = null;
        }
        result.triggerRadius = StreamUtil.readDouble(stream);
        result.explosionParams = ExplosionParams.readFrom(stream);
        return result;
    }

    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, playerId);
        position.writeTo(stream);
        size.writeTo(stream);
        StreamUtil.writeInt(stream, state.discriminant);
        if (timer == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            StreamUtil.writeDouble(stream, timer);
        }
        StreamUtil.writeDouble(stream, triggerRadius);
        explosionParams.writeTo(stream);
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Vec2Double getPosition() {
        return position;
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

    public MineState getState() {
        return state;
    }

    public void setState(MineState state) {
        this.state = state;
    }

    public Double getTimer() {
        return timer;
    }

    public void setTimer(Double timer) {
        this.timer = timer;
    }

    public double getTriggerRadius() {
        return triggerRadius;
    }

    public void setTriggerRadius(double triggerRadius) {
        this.triggerRadius = triggerRadius;
    }

    public ExplosionParams getExplosionParams() {
        return explosionParams;
    }

    public void setExplosionParams(ExplosionParams explosionParams) {
        this.explosionParams = explosionParams;
    }

    @Override
    public String toString() {
        return "Mine{" +
                "playerId=" + playerId +
                ", position=" + position +
                ", size=" + size +
                ", state=" + state +
                ", timer=" + timer +
                ", triggerRadius=" + triggerRadius +
                ", explosionParams=" + explosionParams +
                '}';
    }
}
