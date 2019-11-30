package model;

import util.StreamUtil;

public class WeaponParams {
    private int magazineSize;
    private double fireRate;
    private double reloadTime;
    private double minSpread;
    private double maxSpread;
    private double recoil;
    private double aimSpeed;
    private BulletParams bullet;
    private ExplosionParams explosion;

    public WeaponParams() {}

    public WeaponParams(int magazineSize, double fireRate, double reloadTime, double minSpread, double maxSpread,
                        double recoil, double aimSpeed, BulletParams bullet, ExplosionParams explosion) {
        this.magazineSize = magazineSize;
        this.fireRate = fireRate;
        this.reloadTime = reloadTime;
        this.minSpread = minSpread;
        this.maxSpread = maxSpread;
        this.recoil = recoil;
        this.aimSpeed = aimSpeed;
        this.bullet = bullet;
        this.explosion = explosion;
    }
    public static WeaponParams readFrom(java.io.InputStream stream) throws java.io.IOException {
        WeaponParams result = new WeaponParams();
        result.magazineSize = StreamUtil.readInt(stream);
        result.fireRate = StreamUtil.readDouble(stream);
        result.reloadTime = StreamUtil.readDouble(stream);
        result.minSpread = StreamUtil.readDouble(stream);
        result.maxSpread = StreamUtil.readDouble(stream);
        result.recoil = StreamUtil.readDouble(stream);
        result.aimSpeed = StreamUtil.readDouble(stream);
        result.bullet = BulletParams.readFrom(stream);
        if (StreamUtil.readBoolean(stream)) {
            result.explosion = ExplosionParams.readFrom(stream);
        } else {
            result.explosion = null;
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, magazineSize);
        StreamUtil.writeDouble(stream, fireRate);
        StreamUtil.writeDouble(stream, reloadTime);
        StreamUtil.writeDouble(stream, minSpread);
        StreamUtil.writeDouble(stream, maxSpread);
        StreamUtil.writeDouble(stream, recoil);
        StreamUtil.writeDouble(stream, aimSpeed);
        bullet.writeTo(stream);
        if (explosion == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            explosion.writeTo(stream);
        }
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public void setMagazineSize(int magazineSize) {
        this.magazineSize = magazineSize;
    }

    public double getFireRate() {
        return fireRate;
    }

    public void setFireRate(double fireRate) {
        this.fireRate = fireRate;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public void setReloadTime(double reloadTime) {
        this.reloadTime = reloadTime;
    }

    public double getMinSpread() {
        return minSpread;
    }

    public void setMinSpread(double minSpread) {
        this.minSpread = minSpread;
    }

    public double getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(double maxSpread) {
        this.maxSpread = maxSpread;
    }

    public double getRecoil() {
        return recoil;
    }

    public void setRecoil(double recoil) {
        this.recoil = recoil;
    }

    public double getAimSpeed() {
        return aimSpeed;
    }

    public void setAimSpeed(double aimSpeed) {
        this.aimSpeed = aimSpeed;
    }

    public BulletParams getBullet() {
        return bullet;
    }

    public void setBullet(BulletParams bullet) {
        this.bullet = bullet;
    }

    public ExplosionParams getExplosion() {
        return explosion;
    }

    public void setExplosion(ExplosionParams explosion) {
        this.explosion = explosion;
    }

    @Override
    public String toString() {
        return "WeaponParams{" +
                "magazineSize=" + magazineSize +
                ", fireRate=" + fireRate +
                ", reloadTime=" + reloadTime +
                ", minSpread=" + minSpread +
                ", maxSpread=" + maxSpread +
                ", recoil=" + recoil +
                ", aimSpeed=" + aimSpeed +
                ", bullet=" + bullet +
                ", explosion=" + explosion +
                '}';
    }
}
