import model.*;
import util.Utils;

public class MyStrategy {
    private Unit unit;
    private Game game;
    private Debug debug;

    private StateHolder<Unit> previousEnemyStates = new StateHolder<>(4); //holds t(current), t-1, t-2, t-3

    public void update(Game game, Unit unit, Debug debug){
        this.game = game;
        this.unit = unit;
        this.debug = debug;

        drawRandomShit();
    }

    public UnitAction getAction() {
        Unit nearestEnemy = getNearestEnemy();
        if(nearestEnemy != null) {
            previousEnemyStates.put(nearestEnemy);
            debug.draw(new CustomData.Log("Enemy pos:" + nearestEnemy.getPosition()));
        }

        LootBox nearestWeapon = getNearestWeapon();

        Point targetPos = unit.getPosition();

        Vec2Double aim = new Vec2Double(0, 0);
        if (nearestEnemy != null && unit.getWeapon() != null) {
            Vec2Double predictedEnemyVelocity = predictVelocity();
            //debug.draw(new CustomData.Log("Predicted velocity: " + predictedEnemyVelocity));

            double bulletSpeed = unit.getWeapon().getParams().getBullet().getSpeed();
            Point interception = getInterception(nearestEnemy.getPositionForShooting(), predictedEnemyVelocity, unit.getPositionForShooting(), bulletSpeed);

            aim = shootAt(unit.getPositionForShooting(), interception, bulletSpeed);
            /*aim = new Vec2Double(nearestEnemy.getPositionForShooting().x - unit.getPositionForShooting().x,
                  nearestEnemy.getPositionForShooting().y - unit.getPositionForShooting().y);*/

            targetPos = buildPositionForShooting(nearestEnemy, interception);

            debug.draw(new CustomData.Line(unit.getPositionForShooting(), interception, 0.05f, ColorFloat.GREEN));
            debug.draw(new CustomData.Line(unit.getPositionForShooting(), nearestEnemy.getPositionForShooting(), 0.05f, ColorFloat.YELLOW));
        }

        //take weapon if have no any
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition();
        }
        debug.draw(new CustomData.Log("Target pos: " + targetPos));

        boolean jump = targetPos.y > unit.getPosition().y;
        if (targetPos.x > unit.getPosition().x && game.getLevel()
              .getTiles()[(int) (unit.getPosition().x + 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
            jump = true;
        }

        if (targetPos.x < unit.getPosition().x
                && game.getLevel().getTiles()[(int) (unit.getPosition().x - 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
            jump = true;
        }

        UnitAction action = new UnitAction();
        action.setVelocity(Math.signum(targetPos.x - unit.getPosition().x) * game.getProperties().getUnitMaxHorizontalSpeed());
        action.setJump(jump);
        action.setJumpDown(!jump);
        action.setAim(aim);
        action.setShoot(nearestEnemy != null && Utils.canHit(unit.getPosition(), nearestEnemy.getPosition(), game));
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
            Point leftCorner = bullet.getPosition().offset(-bullet.getSize()/2, -bullet.getSize()/2);
            debug.draw(new CustomData.Rect(leftCorner, new Vec2Double(bullet.getSize(), bullet.getSize()), bullet.getPlayerId() == unit.getPlayerId()? ColorFloat.BLUE : ColorFloat.RED));
        }

        for (Unit gameUnit : game.getUnits()) {
            Point leftDownCorner = gameUnit.getPosition().offset(-game.getProperties().getUnitSize().x/2, 0);
            debug.draw(new CustomData.Rect(leftDownCorner, game.getProperties().getUnitSize(), new ColorFloat(0, 1, 0, 0.2f)));
        }

        if(unit.getWeapon() != null) {
            //debug.draw(new CustomData.Log("Spread: " + unit.getWeapon().getSpread()));
            //debug.draw(new CustomData.Log("Aim speed: " + unit.getWeapon().getParams().getAimSpeed()));
        }

        debug.draw(new CustomData.Log("My pos: " + unit.getPosition()));
    }

    private double getHitProbability(Point targetPosition){
        if(unit.getWeapon() == null) {
            return 0;
        }

        Vec2Double aim = targetPosition.buildVector(unit.getPositionForShooting());

        double spreadAngleHalf = unit.getWeapon().getSpread() / 2;
        //copy and rotate aim vector counterclock-wise
        Vec2Double s1 = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) - aim.y * Math.sin(spreadAngleHalf), aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));
        //copy and rotate aim vector clock-wise
        Vec2Double s2 = new Vec2Double(aim.x * Math.cos(spreadAngleHalf) + aim.y * Math.sin(spreadAngleHalf), -aim.x * Math.sin(spreadAngleHalf) + aim.y * Math.cos(spreadAngleHalf));

        double scaler = (s1.length() * aim.length()) / s1.dot(aim);
        //s1.scaleThis(scaler);
        //s2.scaleThis(scaler);

        Point position = unit.getPositionForShooting();
        Point s1End = new Point(position.x + s1.x, position.y + s1.y);
        Point s2End = new Point(position.x + s2.x, position.y + s2.y);
        Vec2Double coneBaseV = s2End.buildVector(s1End);

        debug.draw(new CustomData.Line(position, s1End, 0.05f, ColorFloat.RED));
        debug.draw(new CustomData.Line(position, s2End, 0.05f, ColorFloat.RED));
        debug.draw(new CustomData.Line(s1End, s2End, 0.05f, ColorFloat.RED));

        //two vectors, representing diagonals of the aim's hitbox
        Vec2Double unitSize = game.getProperties().getUnitSize();
        Vec2Double diag1 = unitSize;
        Vec2Double diag2 = targetPosition.offset(-unitSize.x/2, unitSize.y).buildVector(targetPosition.offset(unitSize.x/2, 0));

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

    private Point buildPositionForShooting(Unit enemy, Point predictedPosition){
        double hitChance = getHitProbability(predictedPosition);
        double distanceToEnemy = predictedPosition.buildVector(unit.getPositionForShooting()).length();
        //debug.draw(new CustomData.Log("Hit chance: " + hitChance));

        if(!Utils.canHit(unit.getPositionForShooting(), predictedPosition, game) || hitChance < 0.5){
            return enemy.getPosition();
        } else if(distanceToEnemy < 4){//not sure if it's a good idea
            return unit.getPositionForShooting().offset(Math.signum(unit.getPositionForShooting().buildVector(enemy.getPositionForShooting()).x), 0);
        } else {
            return unit.getPosition();
        }
    }

    //logic from https://www.gamedev.net/forums/?topic_id=401165
    private Point getInterception(Point targetPosition, Vec2Double targetVelocity, Point shooterPosition, double bulletSpeed){
        Vec2Double distanceVector = targetPosition.buildVector(shooterPosition);
        double a = bulletSpeed*bulletSpeed - targetVelocity.dot(targetVelocity);
        double b = -2*targetVelocity.dot(distanceVector);
        double c = -distanceVector.dot(distanceVector);

        return targetPosition.offset(targetVelocity.scale(Utils.getLargestRootOfQuadraticEquation(a, b, c)));
    }

    private Vec2Double shootAt(Point shooterPosition, Point interception, double bulletSpeed){
        Vec2Double v = interception.buildVector(shooterPosition);
        return v.scale(bulletSpeed/(v.length()));
    }

    private Vec2Double predictVelocity(){
        Point currentPosition = previousEnemyStates.get(0).getPositionForShooting();
        int t = 3;
        Point previousPosition = previousEnemyStates.get(t).getPositionForShooting();
        return currentPosition.buildVector(previousPosition).scaleThis(game.getProperties().getTicksPerSecond()/t);
    }

}
