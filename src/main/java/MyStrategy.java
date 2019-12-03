import model.*;
import util.Utils;

public class MyStrategy {
  private Unit unit;
  private Game game;
  private Debug debug;

  public void update(Game game, Unit unit, Debug debug){
    this.game = game;
    this.unit = unit;
    this.debug = debug;

    drawRandomShit();
  }

  public UnitAction getAction() {
    Unit nearestEnemy = getNearestEnemy();

    LootBox nearestWeapon = getNearestWeapon();

    Vec2Double targetPos = unit.getPosition();
    if (unit.getWeapon() == null && nearestWeapon != null) {
      targetPos = nearestWeapon.getPosition();
    } else if (nearestEnemy != null) {
      targetPos = nearestEnemy.getPosition();
    }

    debug.draw(new CustomData.Log("Target pos: " + targetPos));
    Vec2Double aim = new Vec2Double(0, 0);
    if (nearestEnemy != null) {
      aim = new Vec2Double(nearestEnemy.getPositionForShooting().x - unit.getPositionForShooting().x,
          nearestEnemy.getPositionForShooting().y - unit.getPositionForShooting().y);
    }
    debug.draw(new CustomData.Line(unit.getPositionForShooting(), nearestEnemy != null? nearestEnemy.getPositionForShooting() : new Vec2Double(), 0.05f, ColorFloat.GREEN));

    double hitChance = getHitProbability(aim, nearestEnemy);
    debug.draw(new CustomData.Log("Hit chance: " + hitChance));

    boolean jump = targetPos.y > unit.getPosition().y;
    if (targetPos.x > unit.getPosition().x && game.getLevel()
        .getTiles()[(int) (unit.getPosition().x + 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
      jump = true;
    }

    if (targetPos.x < unit.getPosition().x && game.getLevel()
        .getTiles()[(int) (unit.getPosition().x - 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
      jump = true;
    }

    UnitAction action = new UnitAction();
    action.setVelocity(Math.signum(targetPos.x - unit.getPosition().x) * game.getProperties().getUnitMaxHorizontalSpeed());
    action.setJump(jump);
    action.setJumpDown(!jump);
    action.setAim(aim);
    action.setShoot(Utils.canHit(unit.getPosition(), targetPos, game));
    action.setSwapWeapon(false);
    action.setPlantMine(false);
    return action;
  }

  private LootBox getNearestWeapon(){
    LootBox nearestWeapon = null;
    for (LootBox lootBox : game.getLootBoxes()) {
      if (lootBox.getItem() instanceof Item.Weapon) {
        if (nearestWeapon == null || Utils.distanceSqr(unit.getPosition(),
                lootBox.getPosition()) < Utils.distanceSqr(unit.getPosition(), nearestWeapon.getPosition())) {
          nearestWeapon = lootBox;
        }
      }
    }
    return nearestWeapon;
  }

  private Unit getNearestEnemy(){
    Unit nearestEnemy = null;
    for (Unit other : game.getUnits()) {
      if (other.getPlayerId() != unit.getPlayerId()) {
        if (nearestEnemy == null || Utils.distanceSqr(unit.getPosition(),
                other.getPosition()) < Utils.distanceSqr(unit.getPosition(), nearestEnemy.getPosition())) {
          nearestEnemy = other;
        }
      }
    }
    return nearestEnemy;
  }

  private void drawRandomShit(){
    for (Wall wall : game.getLevel().getWalls()) {
      debug.draw(new CustomData.Line(wall.first.toFloatVector(), wall.second.toFloatVector(), 0.1f, ColorFloat.RED));
    }

    for (Bullet bullet : game.getBullets()) {
      Vec2Double leftCorner = bullet.getPosition().minus(bullet.getSize()/2, bullet.getSize()/2);
      debug.draw(new CustomData.Rect(leftCorner, new Vec2Double(bullet.getSize(), bullet.getSize()), ColorFloat.BLUE));
    }

    for (Unit gameUnit : game.getUnits()) {
      Vec2Double leftDownCorner = gameUnit.getPosition().minus(game.getProperties().getUnitSize().x/2, 0);
      debug.draw(new CustomData.Rect(leftDownCorner, game.getProperties().getUnitSize(), new ColorFloat(0, 1, 0, 0.2f)));
    }
  }

  private double getHitProbability(Vec2Double aim, Unit target){
    if(unit.getWeapon() == null) {
      return 0;
    }

    double spreadAngleHalf = unit.getWeapon().getSpread() / 2;
    //copy and rotate aim vector counterclock-wise
    Vec2Double s1 = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) - aim.y * Math.sin(spreadAngleHalf), aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));
    //copy and rotate aim vector clock-wise
    Vec2Double s2 = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) + aim.y * Math.sin(spreadAngleHalf), -aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));

    double scaler = (s1.length() * aim.length()) / s1.dot(aim);
    s1.scaleThis(scaler);
    s2.scaleThis(scaler);

    Vec2Double position = unit.getPositionForShooting();
    Vec2Double s1End = new Vec2Double(position.x + s1.x, position.y + s1.y);
    Vec2Double s2End = new Vec2Double(position.x + s2.x, position.y + s2.y);
    Vec2Double coneBaseV = s2End.minus(s1End);

    debug.draw(new CustomData.Line(position, s1End, 0.05f, ColorFloat.RED));
    debug.draw(new CustomData.Line(position, s2End, 0.05f, ColorFloat.RED));
    debug.draw(new CustomData.Line(s1End, s2End, 0.05f, ColorFloat.RED));

    //two vectors, representing diagonals of the aim's hitbox
    Vec2Double unitSize = game.getProperties().getUnitSize();
    Vec2Double diag1 = unitSize;
    Vec2Double diag2 = target.getPosition().plus(-unitSize.x/2, unitSize.y).minus(target.getPosition().plus(unitSize.x/2, 0));

    //project diagonals on the cone's base and select max
    double diag1Proj = diag1.dot(coneBaseV)/coneBaseV.length();
    double diag2Proj = diag2.dot(coneBaseV)/coneBaseV.length();
    double maxProj = Math.max(diag1Proj, diag2Proj);

    double result = maxProj/coneBaseV.length();
    if(result > 1){//normalize probability
      return 1;
    } else {
      return result;
    }

    //TODO: consider partially hidden target (by casting few more rays for example)
  }

}
