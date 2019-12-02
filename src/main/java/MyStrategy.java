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
    drawFireCone(aim);

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
  }

  private void drawFireCone(Vec2Double aim){
    if(unit.getWeapon() != null) {
      double spreadAngleHalf = unit.getWeapon().getSpread() / 2;
      Vec2Double one = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) - aim.y * Math.sin(spreadAngleHalf), aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));
      Vec2Double another = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) + aim.y * Math.sin(spreadAngleHalf), -aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));

      Vec2Double position = unit.getPositionForShooting();
      debug.draw(new CustomData.Line(position, new Vec2Double(position.x + one.x, position.y + one.y), 0.05f, ColorFloat.RED));
      debug.draw(new CustomData.Line(position, new Vec2Double(position.x + another.x, position.y + another.y), 0.05f, ColorFloat.RED));
    }
  }

}
