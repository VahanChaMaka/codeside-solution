package model;

import util.Debug;
import util.Logger;
import util.Utils;

import java.util.*;

public class Path {
    private double ticksPerSecond;
    private int updatesPerTick;
    private List<PointToVel> milestones;
    private List<Point> path;

    private Path(List<PointToVel> milestones, int updatesPerTick, double ticksPerSecond) {
        this.milestones = milestones;
        this.updatesPerTick = updatesPerTick;
        this.ticksPerSecond = ticksPerSecond;
    }

    public Point getPositionAtTick(int tick){
        return getPositionAtMicroTick(tick*updatesPerTick); //
    }

    public Point getPositionAtMicroTick(int microtick){
        return path.get(microtick);
    }

    private Vec2Double normalizeToMicroTick(Vec2Double velocity){
        return velocity.scale(1/(ticksPerSecond*updatesPerTick));
    }

    private void convertToPoints(Debug debug){
        path = new ArrayList<>();

        Iterator<PointToVel> pathIterator = milestones.iterator();
        PointToVel activePathPart = pathIterator.next();
        Point activePartStart = activePathPart.point;
        PointToVel nextPathPart = null;
        Point activePartEnd = null;
        if(pathIterator.hasNext()) {
            nextPathPart = pathIterator.next();
            activePartEnd = nextPathPart.point;
        } else {
            activePartEnd = activePartStart.offset(activePathPart.velocity);
        }
        Point position = activePathPart.point;
        Vec2Double activeNorm = normalizeToMicroTick(activePathPart.velocity);


        path.add(activePartStart);
        for (int i = 1; i < 60000; i++) { // 60 tics
            Point tmpPosition = position.offset(activeNorm);
            if(tmpPosition.buildVector(activePartStart).length() <= activePartEnd.buildVector(activePartStart).length()){//we are at the same vector
                position = tmpPosition;
                path.add(position);
            } else { //take the next vector
                activePathPart = nextPathPart;
                if(activePathPart == null){
                    return;
                }
                activeNorm = normalizeToMicroTick(activePathPart.velocity);
                activePartStart = activePathPart.point; //new vector starts where previous ends
                position = activePathPart.point; //switch position to new vector start. It can lead to some inaccuracy :)
                path.add(position);
                if(pathIterator.hasNext()){
                    nextPathPart = pathIterator.next();
                    activePartEnd = nextPathPart.point;
                } else {
                    nextPathPart = null;
                    activePartEnd = position.offset(activePathPart.velocity);
                }
            }
            if(Logger.isLocalRun && i % 500 == 0) {
                debug.draw(new CustomData.Rect(path.get(i), new Vec2Double(0.1, 0.1), ColorFloat.RED));
            }
        }
    }

    public static Path buildPath(Unit unit, Vec2Double predictedVelocity, Game game, Debug debug){
        Point currentPosition = unit.getPositionForShooting();

        List<PointToVel> pointToVelocity = new ArrayList<>();
        pointToVelocity.add(new PointToVel(currentPosition, predictedVelocity));

        //unit is jumping
        if(unit.getJumpState().getMaxTime() != 0){
            double maxTime = unit.getJumpState().getMaxTime();
            Vec2Double maxJumpVector = new Vec2Double(predictedVelocity.x*maxTime, predictedVelocity.y*maxTime);

            //check collision
            List<PointToVel> splitFirst = splitOnCollision(currentPosition, maxJumpVector, predictedVelocity, unit.getSize(), game);
            if(splitFirst.size() > 1){ //collision has occurred
                PointToVel afterCol = splitFirst.get(1);

                pointToVelocity.add(afterCol);
            } else { //there were no collisions
                Point maxJumpPoint = currentPosition.offset(maxJumpVector);
                Vec2Double velAfterMaxJump = predictedVelocity.cpy();
                velAfterMaxJump.y = - game.getProperties().getUnitFallSpeed();
                pointToVelocity.add(new PointToVel(maxJumpPoint, velAfterMaxJump));//just add initial point with predicted vel
            }

            PointToVel lastPoint = pointToVelocity.get(pointToVelocity.size()-1);

            List<PointToVel> splitSecond = splitOnCollision(lastPoint.point, lastPoint.velocity, lastPoint.velocity, unit.getSize(), game);
            if(splitSecond.size() > 1){//collision has occurred in the second part
                pointToVelocity.add(splitSecond.get(1));
            } else {
                //do nothing, already present in list
            }
        } else {
            Intersection intersection = Utils.closestIntersectionBox(currentPosition, currentPosition.offset(predictedVelocity),
                    game.getLevel().getWalls(), unit.getSize());
            if(intersection != null) {
                Point intPoint = intersection.point;
                debug.draw(new CustomData.Rect(intPoint, new Vec2Double(0.2, 0.2), ColorFloat.BLUE));

                Vec2Double velAfterInt = predictedVelocity.cpy();
                if (intersection.wall.isVertical) {
                    velAfterInt.x = 0;
                } else {
                    velAfterInt.y = 0;
                }
                //debug.draw(new CustomData.Line(intPoint, intPoint.offset(afterInt), 0.05f, ColorFloat.BLUE));
                pointToVelocity.add(new PointToVel(intPoint, velAfterInt));
            } else {
                //already in list
            }
        }

        for (PointToVel pointToVel : pointToVelocity) {
            debug.draw(new CustomData.Line(pointToVel.point, pointToVel.point.offset(pointToVel.velocity), 0.05f, ColorFloat.YELLOW));
        }


        Path toReturn = new Path(pointToVelocity, game.getProperties().getUpdatesPerTick(), game.getProperties().getTicksPerSecond());
        toReturn.convertToPoints(debug);
        return toReturn;
    }

    private static List<PointToVel> splitOnCollision(Point startPoint, Vec2Double vector, Vec2Double velocity, Vec2Double size, Game game){
        List<PointToVel> pointToVelocity = new ArrayList<>();
        pointToVelocity.add(new PointToVel(startPoint, velocity));

        Intersection intersection = Utils.closestIntersectionBox(startPoint, startPoint.offset(vector),
                game.getLevel().getWalls(), size);
        if(intersection != null){
            Point intPoint = intersection.point;

            Vec2Double velAfterInt = velocity.cpy();
            if(intersection.wall.isVertical){
                velAfterInt.x = 0;
            } else {
                if(velAfterInt.y > 0){ //ceiling, falling down
                    velAfterInt.y = - game.getProperties().getUnitFallSpeed();
                } else { //floor
                    velAfterInt.y = 0;
                }
            }

            pointToVelocity.add(new PointToVel(intPoint, velAfterInt));
            return pointToVelocity;
        } else {
            return Collections.singletonList(new PointToVel(startPoint, velocity));
        }
    }

    private static class PointToVel {
        private Point point;
        private Vec2Double velocity;

        public PointToVel(Point point, Vec2Double velocity) {
            this.point = point;
            this.velocity = velocity;
        }
    }
}
