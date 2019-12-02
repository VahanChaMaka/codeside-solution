package model;

import util.StreamUtil;

public class Weapon {
    private WeaponType type;
    private WeaponParams params;
    private int magazine;
    private boolean wasShooting;
    private double spread;
    private Double fireTimer;
    private Double lastAngle; //in radians
    private Integer lastFireTick;

    public Weapon() {}

    public Weapon(WeaponType type, WeaponParams params, int magazine, boolean wasShooting,
                  double spread, Double fireTimer, Double lastAngle, Integer lastFireTick) {
        this.type = type;
        this.params = params;
        this.magazine = magazine;
        this.wasShooting = wasShooting;
        this.spread = spread;
        this.fireTimer = fireTimer;
        this.lastAngle = lastAngle;
        this.lastFireTick = lastFireTick;
    }
    public static Weapon readFrom(java.io.InputStream stream) throws java.io.IOException {
        Weapon result = new Weapon();
        switch (StreamUtil.readInt(stream)) {
        case 0:
            result.type = WeaponType.PISTOL;
            break;
        case 1:
            result.type = WeaponType.ASSAULT_RIFLE;
            break;
        case 2:
            result.type = WeaponType.ROCKET_LAUNCHER;
            break;
        default:
            throw new java.io.IOException("Unexpected discriminant value");
        }
        result.params = WeaponParams.readFrom(stream);
        result.magazine = StreamUtil.readInt(stream);
        result.wasShooting = StreamUtil.readBoolean(stream);
        result.spread = StreamUtil.readDouble(stream);
        if (StreamUtil.readBoolean(stream)) {
            result.fireTimer = StreamUtil.readDouble(stream);
        } else {
            result.fireTimer = null;
        }
        if (StreamUtil.readBoolean(stream)) {
            result.lastAngle = StreamUtil.readDouble(stream);
        } else {
            result.lastAngle = null;
        }
        if (StreamUtil.readBoolean(stream)) {
            result.lastFireTick = StreamUtil.readInt(stream);
        } else {
            result.lastFireTick = null;
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, type.discriminant);
        params.writeTo(stream);
        StreamUtil.writeInt(stream, magazine);
        StreamUtil.writeBoolean(stream, wasShooting);
        StreamUtil.writeDouble(stream, spread);
        if (fireTimer == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            StreamUtil.writeDouble(stream, fireTimer);
        }
        if (lastAngle == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            StreamUtil.writeDouble(stream, lastAngle);
        }
        if (lastFireTick == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            StreamUtil.writeInt(stream, lastFireTick);
        }
    }

    public WeaponType getType() {
        return type;
    }

    public void setType(WeaponType type) {
        this.type = type;
    }

    public WeaponParams getParams() {
        return params;
    }

    public void setParams(WeaponParams params) {
        this.params = params;
    }

    public int getMagazine() {
        return magazine;
    }

    public void setMagazine(int magazine) {
        this.magazine = magazine;
    }

    public boolean isWasShooting() {
        return wasShooting;
    }

    public void setWasShooting(boolean wasShooting) {
        this.wasShooting = wasShooting;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public Double getFireTimer() {
        return fireTimer;
    }

    public void setFireTimer(Double fireTimer) {
        this.fireTimer = fireTimer;
    }

    public Double getLastAngle() {
        return lastAngle;
    }

    public void setLastAngle(Double lastAngle) {
        this.lastAngle = lastAngle;
    }

    public Integer getLastFireTick() {
        return lastFireTick;
    }

    public void setLastFireTick(Integer lastFireTick) {
        this.lastFireTick = lastFireTick;
    }

    @Override
    public String toString() {
        return "Weapon{" +
                "type=" + type +
                ", params=" + params +
                ", magazine=" + magazine +
                ", wasShooting=" + wasShooting +
                ", spread=" + spread +
                ", fireTimer=" + fireTimer +
                ", lastAngle=" + lastAngle +
                ", lastFireTick=" + lastFireTick +
                '}';
    }
}
