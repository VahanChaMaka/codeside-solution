import model.*;
import util.Debug;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MyStrategy {
    private Unit unit;
    private Game game;
    private Debug debug;

    private static final int STATES_SIZE = 3;
    private StateHolder<Unit> previousEnemyStates = new StateHolder<>(STATES_SIZE); //holds t(current), t-1, t-2, t-3

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
           // debug.draw(new CustomData.Log("Enemy pos:" + nearestEnemy.getPosition()));
        }

        LootBox nearestWeapon = getNearestLootBox(Item.Weapon.class);

        Point targetPos = unit.getPosition();

        boolean shoot = false;
        Vec2Double aim = new Vec2Double(-1, -1);
        if (nearestEnemy != null ) {
            if(unit.getWeapon() != null) {
                Vec2Double predictedVelocity = predictVelocity();
                aim = buildAimVector(nearestEnemy, predictedVelocity);

                shoot = canHit(unit.getPositionForShooting(), unit.getPositionForShooting().offset(aim), unit.getWeapon(), true);

                double maxJumpTime = unit.getJumpState().getMaxTime();
                Vec2Double maxJumpVector = new Vec2Double(predictedVelocity.x*maxJumpTime, predictedVelocity.y*maxJumpTime);
                boolean willCollide = Utils.closestIntersectionBox(nearestEnemy.getPositionForShooting(), nearestEnemy.getPositionForShooting().offset(predictedVelocity),
                        game.getLevel().getWalls(), nearestEnemy.getSize()) != null;
                //if bullet will hit wall/floor, try to predict enemy's velocity with collisions
                //if aiming higher than enemy can jump
                if(!shoot || willCollide || unit.getPositionForShooting().offset(aim).y > nearestEnemy.getPositionForShooting().offset(maxJumpVector).y){
                    aim = buildAimVectorNew(nearestEnemy);
                    shoot = canHit(unit.getPositionForShooting(), unit.getPositionForShooting().offset(aim), unit.getWeapon(), true);
                }
                //debug.draw(new CustomData.Line(unit.getPositionForShooting(), unit.getPositionForShooting().offset(aim), 0.05f, ColorFloat.GREEN));

                if(unit.getWeapon().getType() == WeaponType.ROCKET_LAUNCHER){
                    //aim = aimInLegs(aim);
                }
                //debug.draw(new CustomData.Line(unit.getPositionForShooting(), unit.getPositionForShooting().offset(aim), 0.05f, ColorFloat.GREEN));

                Point intersection = unit.getPositionForShooting().offset(aim);
                targetPos = buildPositionForShooting(nearestEnemy, intersection);

                /*if(!shoot){
                    aim = new Vec2Double(nearestEnemy.getPositionForShooting().x - unit.getPositionForShooting().x,
                            nearestEnemy.getPositionForShooting().y - unit.getPositionForShooting().y);
                }*/
            } else {//look at the enemy without a weapon to reduce spread on the first shot
                aim = new Vec2Double(nearestEnemy.getPositionForShooting().x - unit.getPositionForShooting().x,
                  nearestEnemy.getPositionForShooting().y - unit.getPositionForShooting().y);
            }

            //debug.draw(new CustomData.Line(unit.getPositionForShooting(), nearestEnemy.getPositionForShooting(), 0.05f, ColorFloat.YELLOW));
        }

        //take weapon if have no any
        if (unit.getWeapon() == null && nearestWeapon != null) {
            targetPos = nearestWeapon.getPosition();
        }
        //debug.draw(new CustomData.Log("Target pos: " + targetPos));

        if(unit.getHealth() < 75){
            LootBox nearestHealthPack = getNearestLootBox(Item.HealthPack.class);
            if(nearestHealthPack != null){
                targetPos = nearestHealthPack.getPosition();
            }
        }

        boolean jump = targetPos.y > unit.getPosition().y;
        if (targetPos.x > unit.getPosition().x && game.getLevel()
              .getTiles()[(int) (unit.getPosition().x + 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
            jump = true;
        }

        if (targetPos.x < unit.getPosition().x
                && game.getLevel().getTiles()[(int) (unit.getPosition().x - 1)][(int) (unit.getPosition().y)] == Tile.WALL) {
            jump = true;
        }

        if(shoot){
            Vec2Double bulletVec = aim.getNormalized().scaleThis(unit.getWeapon().getParams().getBullet().getSpeed());
            Vec2Double bulletVecMcrtck = bulletVec.scale(1/(game.getProperties().getTicksPerSecond()*game.getProperties().getUpdatesPerTick()));
            for (Unit gameUnit : game.getUnits()) {
                if(gameUnit.getId() != unit.getId() && gameUnit.getPlayerId() == unit.getPlayerId()){
                    for (int i = 0; i < 800; i++) {
                        if(Utils.isPointInsideRect(unit.getPositionForShooting().offset(bulletVecMcrtck.scale(i)), gameUnit.getPositionForShooting(), gameUnit.getSize().scale(1.5))){
                            shoot = false;
                            jump = true;
                            //debug.draw(new CustomData.Log("Shooting is prohibited!"));
                        }
                    }
                }
            }
        }

        double horizontalVelocity = Math.signum(targetPos.x - unit.getPosition().x) * game.getProperties().getUnitMaxHorizontalSpeed();
        Utils.Pair<Boolean, Double> dodge = dodge(horizontalVelocity, jump);
        //debug.draw(new CustomData.Log("Dodging " + dodge));
        jump =  dodge.getOne();
        horizontalVelocity = dodge.getAnother();

        UnitAction action = new UnitAction();
        action.setVelocity(horizontalVelocity);
        action.setJump(jump);
        action.setJumpDown(!jump);
        action.setAim(aim);
        action.setReload(false);
        action.setShoot(shoot);
        action.setSwapWeapon(false);
        action.setPlantMine(false);
        return action;
    }

    private LootBox getNearestLootBox(Class<? extends Item> itemClass){
        LootBox nearestLootBox = null;
        for (LootBox lootBox : game.getLootBoxes()) {
            if (itemClass.isInstance(lootBox.getItem())
                    /*&& itemClass == Item.Weapon.class && ((Item.Weapon)lootBox.getItem()).getWeaponType() == WeaponType.ROCKET_LAUNCHER*/) {
                if (nearestLootBox == null || Utils.distanceSqr(unit.getPosition(),
                        lootBox.getPosition()) < Utils.distanceSqr(unit.getPosition(), nearestLootBox.getPosition())) {
                    nearestLootBox = lootBox;
                }
            }
        }
        return nearestLootBox;
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
        /*for (Wall wall : game.getLevel().getWalls()) {
            debug.draw(new CustomData.Line(wall.first, wall.second, 0.1f, ColorFloat.RED));
        }*/

        for (Bullet bullet : game.getBullets()) {
            Point leftCorner = bullet.getPosition().offset(-bullet.getSize()/2, -bullet.getSize()/2);
            //debug.draw(new CustomData.Rect(leftCorner, new Vec2Double(bullet.getSize(), bullet.getSize()), bullet.getPlayerId() == unit.getPlayerId()? ColorFloat.BLUE : ColorFloat.RED));
        }

        for (Unit gameUnit : game.getUnits()) {
            Point leftDownCorner = gameUnit.getPosition().offset(-game.getProperties().getUnitSize().x/2, 0);
            //debug.draw(new CustomData.Rect(leftDownCorner, game.getProperties().getUnitSize(), new ColorFloat(0, 1, 0, 0.2f)));
        }

        if(unit.getWeapon() != null) {
            //debug.draw(new CustomData.Log("Spread: " + unit.getWeapon().getSpread()));
            //debug.draw(new CustomData.Log("Aim speed: " + unit.getWeapon().getParams().getAimSpeed()));
        }

        //debug.draw(new CustomData.Log("My pos: " + unit.getPosition()));
    }

    private double getHitProbability(Point targetPosition){
        if(unit.getWeapon() == null) {
            return 0;
        }

        Vec2Double aim = targetPosition.buildVector(unit.getPositionForShooting());

        double spreadAngle = unit.getWeapon().getSpread();
        //copy and rotate aim vector counterclock-wise
        Vec2Double s1 = new Vec2Double(aim.x * Math.cos(spreadAngle) - aim.y * Math.sin(spreadAngle), aim.x * Math.sin(spreadAngle) + aim.y * Math.cos(spreadAngle));
        //copy and rotate aim vector clock-wise
        Vec2Double s2 = new Vec2Double(aim.x * Math.cos(spreadAngle) + aim.y * Math.sin(spreadAngle), -aim.x * Math.sin(spreadAngle) + aim.y * Math.cos(spreadAngle));

        double scaler = (s1.length() * aim.length()) / s1.dot(aim);
        s1.scaleThis(scaler);
        s2.scaleThis(scaler);

        Point position = unit.getPositionForShooting();
        Point s1End = new Point(position.x + s1.x, position.y + s1.y);
        Point s2End = new Point(position.x + s2.x, position.y + s2.y);
        Vec2Double coneBaseV = s2End.buildVector(s1End);

        /*debug.draw(new CustomData.Line(position, s1End, 0.05f, ColorFloat.RED));
        debug.draw(new CustomData.Line(position, s2End, 0.05f, ColorFloat.RED));
        debug.draw(new CustomData.Line(s1End, s2End, 0.05f, ColorFloat.RED));*/

        //two vectors, representing diagonals of the aim's hitbox
        Vec2Double unitSize = game.getProperties().getUnitSize();
        Vec2Double diag1 = unitSize;
        Vec2Double diag2 = targetPosition.offset(-unitSize.x/2, unitSize.y).buildVector(targetPosition.offset(unitSize.x/2, 0));

        //project diagonals on the cone's base and select max
        double diag1Proj = diag1.dot(coneBaseV)/coneBaseV.length();
        diag1Proj = diag1Proj * Math.signum(diag1Proj);

        double diag2Proj = diag2.dot(coneBaseV)/coneBaseV.length();
        diag2Proj = diag2Proj * Math.signum(diag2Proj);

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
        double deltaX = predictedPosition.buildVector(unit.getPositionForShooting()).x;
        deltaX = deltaX * Math.signum(deltaX);

        double deltaY = predictedPosition.buildVector(unit.getPositionForShooting()).y;
        deltaY = deltaY * Math.signum(deltaY);
        //debug.draw(new CustomData.Log("Hit chance: " + hitChance));
        //debug.draw(new CustomData.Log("Delta x: " + deltaX));

        if(!canHit(unit.getPositionForShooting(), predictedPosition, unit.getWeapon(), true)
                || (hitChance < 0.3 && deltaX > unit.getSize().x * 2)){//prevent sitting on top of the enemy
            return enemy.getPosition();
        } else if(deltaX < unit.getSize().x * 2 && deltaY > unit.getSize().y * 3) {//if on top/under enemy, move towards closest health pack. Can stick if HP is on the same x as enemy
            LootBox nearestHP = getNearestLootBox(Item.HealthPack.class);
            if(nearestHP != null) {
                return nearestHP.getPosition();
            } else { //just move to the left or right side
                Point edgePoint = unit.getPosition().cpy();
                if(unit.getPosition().x > game.getLevel().getTiles().length/2){
                    edgePoint.x = 0;
                } else {
                    edgePoint.x = game.getLevel().getTiles().length;
                }
                return edgePoint;
            }
        } else if(distanceToEnemy < 6){//6 is min distance to have enough time to dodge from bullet in center
            return unit.getPosition().offset(Math.signum(unit.getPositionForShooting().buildVector(enemy.getPositionForShooting()).x), 0);
        } else {
            return unit.getPosition();
        }
    }

    //logic from https://www.gamedev.net/forums/?topic_id=401165
    private Point getIntersection(Point targetPosition, Vec2Double targetVelocity, Point shooterPosition, double bulletSpeed){
        Vec2Double distanceVector = targetPosition.buildVector(shooterPosition);
        double a = bulletSpeed*bulletSpeed - targetVelocity.dot(targetVelocity);
        double b = targetVelocity.scale(-2).dot(distanceVector);
        double c = -distanceVector.dot(distanceVector);

        /*double root = 0;
        if(unit.getWeapon().getType() == WeaponType.ROCKET_LAUNCHER){ //rocket is slower, than unit
            root = Utils.getSmallestRootOfQuadraticEquation(a, b, c);
        } else {
            root = Utils.getLargestRootOfQuadraticEquation(a, b, c);
        }*/
        return targetPosition.offset(targetVelocity.scale(Utils.getLargestRootOfQuadraticEquation(a, b, c)));
    }

    //just builds vector with exact length equal to bullet speed
    /*private Vec2Double shootAt(Point shooterPosition, Point interception, double bulletSpeed){
        Vec2Double v = interception.buildVector(shooterPosition);
        return v.scale(bulletSpeed/(v.length()));
    }*/

    //do not consider walls and floor
    private Vec2Double predictVelocity(){
        Point currentPosition = previousEnemyStates.get(0).getPositionForShooting();
        int t = STATES_SIZE-1;
        //prevent failing when collect not enough data
        if(t >= previousEnemyStates.getCurrentSize()){
            t = previousEnemyStates.getCurrentSize()-1;
        }

        Point previousPosition = previousEnemyStates.get(t).getPositionForShooting();
        Vec2Double predictedEnemyVelocity = currentPosition.buildVector(previousPosition).scaleThis(game.getProperties().getTicksPerSecond()/t);

        //if unit is falling, it is falling. Avoid averaging vertical velocity
        if(!previousEnemyStates.get(0).isOnGround() && !previousEnemyStates.get(0).getJumpState().isCanJump()){
            predictedEnemyVelocity.y = - game.getProperties().getUnitFallSpeed();
        }
        //debug.draw(new CustomData.Line(currentPosition, currentPosition.offset(predictedEnemyVelocity), 0.05f, ColorFloat.BLUE));
        return predictedEnemyVelocity;
    }

    private Vec2Double predictVelocityWithCollision(){
        Vec2Double rawVelocity = predictVelocity();
        Point currentPosition = previousEnemyStates.get(0).getPositionForShooting();
        Intersection intersection = Utils.closestIntersectionBox(currentPosition, currentPosition.offset(rawVelocity),
                game.getLevel().getWalls(), unit.getSize());
        if(intersection != null){
            Point intPoint = intersection.point;
            debug.draw(new CustomData.Rect(intPoint, new Vec2Double(0.2, 0.2), ColorFloat.BLUE));
            Vec2Double beforeInt = intPoint.buildVector(currentPosition);

            //remained velocity after intersection
            Vec2Double afterInt = rawVelocity.minus(beforeInt);
            if(intersection.wall.isVertical){
                afterInt.x = 0;
            } else {
                afterInt.y = 0;
            }

            //debug.draw(new CustomData.Line(intPoint, intPoint.offset(afterInt), 0.05f, ColorFloat.BLUE));
            Vec2Double finalVelocity = beforeInt.plus(afterInt);
            //debug.draw(new CustomData.Line(currentPosition, currentPosition.offset(finalVelocity), 0.05f, ColorFloat.BLUE));
            return finalVelocity;
        }
        return null;
    }

    //TODO: when shooting from ladder/jump, first bullet always hits wall
    public boolean canHit(Point position, Point target, Weapon weapon, boolean drawIntersection){
        if (weapon == null){
            return false;
        }

        position = position.offset(0, -weapon.getParams().getBullet().getSize());

        double bulletSize = weapon.getParams().getBullet().getSize();
        Intersection closestIntersection = Utils.closestIntersectionBox(position, target, game.getLevel().getWalls(), new Vec2Double(bulletSize, bulletSize));
        if(drawIntersection && closestIntersection != null){
            //debug.draw(new CustomData.Rect(closestIntersection.point, new Vec2Double(0.2, 0.2), ColorFloat.RED));
        }
        return closestIntersection == null;
    }

    public Vec2Double buildAimVector(Unit nearestEnemy, Vec2Double predictedEnemyVelocity){
        double bulletSpeed = unit.getWeapon().getParams().getBullet().getSpeed();
        Point intersection = getIntersection(nearestEnemy.getPositionForShooting(), predictedEnemyVelocity, unit.getPositionForShooting(), bulletSpeed);

        Vec2Double aim = intersection.buildVector(unit.getPositionForShooting());
        //debug.draw(new CustomData.Line(unit.getPositionForShooting(), intersection, 0.05f, ColorFloat.GREEN));
        return aim;
    }

    public Vec2Double buildAimVectorNew(Unit enemy){
        Path path = Path.buildPath(enemy, predictVelocity(), game, debug);
        double bulletSpeed = unit.getWeapon().getParams().getBullet().getSpeed();
        double minDistance = Double.MAX_VALUE;

        //simulate for 60 ticks
        for (int i = 0; i < 6000; i++) {
            Point predictedPosition = path.getPositionAtMicroTick(i);
            //Point enemyLeftBotCorner = predictedPosition.offset(enemy.getSize().x/2, enemy.getSize().y/2);
            Vec2Double bulletVec = predictedPosition.buildVector(unit.getPositionForShooting()).normalizeThis().scaleThis(bulletSpeed);
            Vec2Double bulletVecMcrtck = bulletVec.scale(1/(game.getProperties().getTicksPerSecond()*game.getProperties().getUpdatesPerTick()));
            Point bulletPosition = unit.getPositionForShooting().offset(bulletVecMcrtck.scaleThis(i));
            double distance = predictedPosition.buildVector(bulletPosition).length();

            if(distance < minDistance){
                minDistance = distance;
            } else {
                //debug.draw(new CustomData.Line(unit.getPositionForShooting(), predictedPosition, 0.05f, new ColorFloat(1, 1, 1, 1)));
                return predictedPosition.buildVector(unit.getPositionForShooting());
            }

            if(i % 300 == 0){
                //Logger.log("Pred pos: " + predictedPosition + " , bul pos: " + bulletPosition);
                //debug.draw(new CustomData.Rect(predictedPosition, new Vec2Double(0.1f, 0.1f), ColorFloat.RED));
            }
        }

        //rocket can fly more than 60 ticks
        return enemy.getPositionForShooting().buildVector(unit.getPositionForShooting());
    }

    private Utils.Pair<Boolean, Double> dodge(double xVelocity, boolean jump){
        double yVelocity = 0;
        if(!unit.isOnGround() ){
            if(unit.getJumpState().isCanJump() && jump) { //in jump up state
                yVelocity = unit.getJumpState().getSpeed();
            } else { //falling down
                yVelocity = - game.getProperties().getUnitFallSpeed();
            }
        } else if(jump){
            yVelocity = unit.getJumpState().getSpeed();
        }
        Vec2Double velVector = new Vec2Double(xVelocity, yVelocity);

        Utils.Pair<Double, Integer> doNothingDamage = maxCollectedDamageByX(velVector);
        Utils.Pair<Double, Integer> cancelJumpDamage = doNothingDamage;
        Utils.Pair<Double, Integer> doJumpDamage = doNothingDamage;

        //if jumping, try to cancel
        if(unit.getJumpState().isCanJump() && jump && unit.getJumpState().isCanCancel()){
            Vec2Double cancelVel = velVector.cpy();
            cancelVel.y = - game.getProperties().getUnitFallSpeed();
            cancelJumpDamage = maxCollectedDamageByX(cancelVel);
        }

        //try to jump
        if(unit.getJumpState().isCanJump() && !jump ){
            Vec2Double jumpVel = velVector.cpy();
            jumpVel.y = unit.getJumpState().getSpeed();
            doJumpDamage = maxCollectedDamageByX(jumpVel);
        }

        //debug.draw(new CustomData.Log("Nothing: " + doNothingDamage + ", j: " + doJumpDamage + ", c: " + cancelJumpDamage));

        if(cancelJumpDamage.getAnother() < doNothingDamage.getAnother() || doJumpDamage.getAnother() < doNothingDamage.getAnother()){
            if(cancelJumpDamage.getAnother() < doJumpDamage.getAnother()){
                return new Utils.Pair<>(false, cancelJumpDamage.getOne());
            } else {
                return new Utils.Pair<>(true, doJumpDamage.getOne());
            }
        } else {
            return new Utils.Pair<>(jump, doNothingDamage.getOne());
        }
        //debug.draw(new CustomData.Rect(unitFuturePosition, new Vec2Double(0.1f, 0.1f), ColorFloat.GREEN));
    }

    //return horizontal velocity to collected damage
    private Utils.Pair<Double, Integer> maxCollectedDamageByX(Vec2Double velocity){
        double maxHorSpeed = game.getProperties().getUnitMaxHorizontalSpeed();
        Utils.Pair<Double, Integer> doNothing = new Utils.Pair<>(velocity.x, collectedDamage(velocity));
        Utils.Pair<Double, Integer> left = new Utils.Pair<>(- maxHorSpeed, collectedDamage(velocity.cpy().setX(- maxHorSpeed)));
        Utils.Pair<Double, Integer> right = new Utils.Pair<>(maxHorSpeed, collectedDamage(velocity.cpy().setX(maxHorSpeed)));

        //debug.draw(new CustomData.Log("J: " + (velocity.y>0) + ", l: " + left.getAnother() + ", r: " + right.getAnother() + ", n: " + doNothing.getAnother()
                //+ ", vel: " + velocity));

        //important to do nothing if there is no difference
        if(left.getAnother() < doNothing.getAnother() || right.getAnother() < doNothing.getAnother()){
            if(left.getAnother() < right.getAnother()){
                return left;
            } else {
                return right;
            }
        } else {
            return doNothing;
        }
    }

    private int collectedDamage(Vec2Double velocity){
        int damage = 0;
        List<Integer> caughtBulletInd = new ArrayList<>();

        Vec2Double futureVelocity = velocity.cpy();
        Vec2Double velVectorMicrotick = futureVelocity.scale(1/(game.getProperties().getUpdatesPerTick() * game.getProperties().getTicksPerSecond()));
        Point unitFuturePosition = unit.getPositionForShooting();
        //30 ticks
        for (int i = 0; i < 3000; i++) {
            if(caughtBulletInd.size() == game.getBullets().length){
                break;
            }
            
            unitFuturePosition = unitFuturePosition.offset(velVectorMicrotick);

            //check collisions against walls
            if(i % 10 == 0){
                Intersection intersection = Utils.closestIntersectionBox(unit.getPositionForShooting(), unitFuturePosition, game.getLevel().getWalls(), unit.getSize());
                if(intersection != null && intersection.wall.isVertical){
                    futureVelocity.x = 0;
                    velVectorMicrotick = futureVelocity.scale(1/(game.getProperties().getUpdatesPerTick() * game.getProperties().getTicksPerSecond()));
                }
            }

            for (int j = 0; j < game.getBullets().length; j++) {
                Bullet bullet = game.getBullets()[j];

                if(bullet.getUnitId() != unit.getId() && !caughtBulletInd.contains(j)) { //skip already caught bullet
                    Point bulFuturePos = bullet.getPosition().offset(bullet.getVelocity().scale(i / (game.getProperties().getUpdatesPerTick() * game.getProperties().getTicksPerSecond())));
                    /*boolean hitsWall = bulFuturePos.x >= 0 && bulFuturePos.x < game.getLevel().getTiles().length && bulFuturePos.y >= 0 && bulFuturePos.y < game.getLevel().getTiles()[0].length
                            && game.getLevel().getTiles()[(int)bulFuturePos.x][(int)bulFuturePos.y] == Tile.WALL;*/
                    boolean hitsWall = Utils.closestIntersectionBox(bullet.getPosition(), bulFuturePos, game.getLevel().getWalls(), new Vec2Double(bullet.getSize(), bullet.getSize())) != null;
                    if(hitsWall){
                        caughtBulletInd.add(j);
                        if(bullet.getExplosionParams() != null
                                && unitFuturePosition.buildVector(bulFuturePos).length() <= bullet.getExplosionParams().getRadius() + unit.getSize().length()){ //expand expl radius to check unit's corners
                            damage += bullet.getExplosionParams().getDamage();
                        }
                        continue;
                    }

                    if (Utils.isPointInsideRect(bulFuturePos, unitFuturePosition, unit.getSize().plus(bullet.getSize(), bullet.getSize()).scaleThis(1.2))) {
                        caughtBulletInd.add(j);
                        damage += bullet.getDamage();
                        if(bullet.getExplosionParams() != null){
                            damage += bullet.getExplosionParams().getDamage();
                        }
                        //debug.draw(new CustomData.Rect(bulFuturePos, new Vec2Double(0.1f, 0.1f), ColorFloat.GREEN));
                    }
                }
            }
        }

        return damage;
    }

    private Vec2Double aimInLegs(Vec2Double originalAim){
        //Point intersection = unit.getPositionForShooting().offset(originalAim.getNormalized().scaleThis(unit.getWeapon().getParams().getBullet().getSpeed()));
        Point intersection = unit.getPositionForShooting().offset(originalAim);
        Point closestWall = null;
        for (int i = (int)intersection.x - 3; i <= intersection.x + 3; i++) {
            for (int j = (int)intersection.y - 3; j < intersection.y + 3; j++) {
                if(i <= 0 || i >= game.getLevel().getTiles().length - 1
                        || j <= 0 || j >= game.getLevel().getTiles()[0].length - 1 || game.getLevel().getTiles()[i][j] == Tile.WALL){
                    Point wall = new Point(i + 0.5, j + 0.5);//center of the cell
                    debug.draw(new CustomData.Rect(wall, new Vec2Double(0.1, 0.1), ColorFloat.RED));
                    if(closestWall == null || wall.buildVector(intersection).length() < closestWall.buildVector(intersection).length()){
                        closestWall = wall;
                    }
                }
            }
        }

        if(closestWall != null){
            return closestWall.buildVector(unit.getPositionForShooting());
        } else {
            return originalAim;
        }
    }
}
