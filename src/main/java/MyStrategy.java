import model.*;
import util.Utils;

public class MyStrategy {
  static double distanceSqr(Vec2Double a, Vec2Double b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
  }

  public UnitAction getAction(Unit unit, Game game, Debug debug) {
    for (Wall wall : game.getLevel().getWalls()) {
      debug.draw(new CustomData.Line(wall.first.toFloatVector(), wall.second.toFloatVector(), 0.1f, ColorFloat.RED));
    }

    Unit nearestEnemy = null;
    for (Unit other : game.getUnits()) {
      if (other.getPlayerId() != unit.getPlayerId()) {
        if (nearestEnemy == null || distanceSqr(unit.getPosition(),
            other.getPosition()) < distanceSqr(unit.getPosition(), nearestEnemy.getPosition())) {
          nearestEnemy = other;
        }
      }
    }

    LootBox nearestWeapon = null;
    for (LootBox lootBox : game.getLootBoxes()) {
      if (lootBox.getItem() instanceof Item.Weapon) {
        if (nearestWeapon == null || distanceSqr(unit.getPosition(),
            lootBox.getPosition()) < distanceSqr(unit.getPosition(), nearestWeapon.getPosition())) {
          nearestWeapon = lootBox;
        }
      }
    }

    Vec2Double targetPos = unit.getPosition();
    if (unit.getWeapon() == null && nearestWeapon != null) {
      targetPos = nearestWeapon.getPosition();
    } else if (nearestEnemy != null) {
      targetPos = nearestEnemy.getPosition();
    }

    debug.draw(new CustomData.Log("Target pos: " + targetPos));
    Vec2Double aim = new Vec2Double(0, 0);
    if (nearestEnemy != null) {
      aim = new Vec2Double(nearestEnemy.getPosition().x - unit.getPosition().x,
          nearestEnemy.getPosition().y - unit.getPosition().y);
    }

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
    action.setVelocity(targetPos.x - unit.getPosition().x);
    action.setJump(jump);
    action.setJumpDown(!jump);
    action.setAim(aim);
    action.setShoot(Utils.canHit(unit.getPosition(), targetPos, game));
    action.setSwapWeapon(false);
    action.setPlantMine(false);
    return action;
  }
}