package model;

import util.Debug;
import util.Logger;
import util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Path {
    private double ticksPerSecond;
    private int updatesPerTick;
    private List<Vec2Double> directions;
    private Point startPosition;

    private Path(List<Vec2Double> directions, Point startPosition, int updatesPerTick, double ticksPerSecond) {
        this.directions = directions;
        this.startPosition = startPosition;
        this.updatesPerTick = updatesPerTick;
        this.ticksPerSecond = ticksPerSecond;
    }

    public List<Vec2Double> getDirections() {
        return directions;
    }

    public Point getStartPosition() {
        return startPosition;
    }

    public Point getPositionAtTick(int tick){
        return getPositionAtMicroTick(tick*updatesPerTick); //
    }

    public Point getPositionAtMicroTick(int microtick){
        microtick = microtick/10; //I have no idea why :)
        Iterator<Vec2Double> pathIterator = directions.iterator();
        Point position = startPosition;
        Vec2Double activePathPart = pathIterator.next();
        Vec2Double activeNorm = normalizeToMicroTick(activePathPart); //normalize to microticks
        Point activePartStart = startPosition;
        Point activePartEnd = startPosition.offset(activePathPart);

        for (int i = 0; i <= microtick; i++) {
            Point tmpPosition = position.offset(activeNorm);
            if(tmpPosition.buildVector(activePartStart).length() <= activePartEnd.buildVector(activePartStart).length()){//we are at the same vector
                position = tmpPosition;
            } else { //take the next vector
                if(pathIterator.hasNext()){
                    activePathPart = pathIterator.next();
                    activeNorm = normalizeToMicroTick(activePathPart);
                    activePartStart = activePartEnd; //new vector starts where previous ends
                    activePartEnd = activePartStart.offset(activePathPart);
                    position = activePartStart; //switch position to new vector start. It can lead to some inaccuracy :)
                } else {
                    Logger.log("Warning! Trying to get position which is too far! Microtick: " + microtick);
                    return position;
                }
            }
        }

        return position;
    }

    private Vec2Double normalizeToMicroTick(Vec2Double velocity){
        return velocity.getNormalized().scaleThis(1/(ticksPerSecond*updatesPerTick));
    }

    public static Path buildPath(Unit unit, Vec2Double predictedVelocity, Game game, Debug debug){
        List<Vec2Double> path = new ArrayList<>(2);
        Point currentPosition = unit.getPositionForShooting();
        Intersection intersection = Utils.closestIntersectionBox(currentPosition, currentPosition.offset(predictedVelocity),
                game.getLevel().getWalls(), unit.getSize());
        if(intersection != null){
            Point intPoint = intersection.point;
            debug.draw(new CustomData.Rect(intPoint, new Vec2Double(0.2, 0.2), ColorFloat.BLUE));
            Vec2Double beforeInt = intPoint.buildVector(currentPosition);

            //remained velocity after intersection
            Vec2Double afterInt = predictedVelocity.minus(beforeInt);
            if(intersection.wall.isVertical){
                afterInt.x = 0;
            } else {
                afterInt.y = 0;
            }
            debug.draw(new CustomData.Line(intPoint, intPoint.offset(afterInt), 0.05f, ColorFloat.BLUE));

            path.add(beforeInt);
            path.add(afterInt);
        } else if(unit.getJumpState().getMaxTime() != 0){
            double maxTime = unit.getJumpState().getMaxTime();
            Vec2Double maxJumpVector = new Vec2Double(predictedVelocity.x*maxTime, predictedVelocity.y*maxTime);
            Vec2Double afterMaxHeight = predictedVelocity.minus(maxJumpVector);
            afterMaxHeight.y = -afterMaxHeight.y;//invert vertical speed

            debug.draw(new CustomData.Line(currentPosition, currentPosition.offset(maxJumpVector), 0.05f, ColorFloat.YELLOW));
            debug.draw(new CustomData.Line(currentPosition.offset(maxJumpVector), currentPosition.offset(maxJumpVector).offset(afterMaxHeight), 0.05f, ColorFloat.YELLOW));

            path.add(maxJumpVector);
            path.add(afterMaxHeight);
        } else{
            path.add(predictedVelocity);
        }

        return new Path(path, currentPosition, game.getProperties().getUpdatesPerTick(), game.getProperties().getTicksPerSecond());
    }

    public static Path buildPath_(Unit unit, Vec2Double predictedVelocity, Game game, Debug debug){
        List<Vec2Double> path = new ArrayList<>(3);
        Point currentPosition = unit.getPositionForShooting();

        //unit is jumping
        if(unit.getJumpState().getMaxTime() != 0){
            double maxTime = unit.getJumpState().getMaxTime();
            Vec2Double maxJumpVector = new Vec2Double(predictedVelocity.x*maxTime, predictedVelocity.y*maxTime);
            Vec2Double afterMaxHeight = predictedVelocity.minus(maxJumpVector);
            afterMaxHeight.y = -afterMaxHeight.y;//invert vertical speed

            debug.draw(new CustomData.Line(currentPosition, currentPosition.offset(maxJumpVector), 0.05f, ColorFloat.YELLOW));
            debug.draw(new CustomData.Line(currentPosition.offset(maxJumpVector), currentPosition.offset(maxJumpVector).offset(afterMaxHeight), 0.05f, ColorFloat.YELLOW));

            //path.add(maxJumpVector);
            //check collision
            List<Vec2Double> splitFirst = splitOnCollision(currentPosition, maxJumpVector, unit.getSize(), game);
            if(splitFirst.size() > 1){ //collision has occurred
                path.addAll(splitFirst);
            } else { //there were no collisions
                path.add(maxJumpVector);//it's same maxJumpVector vector
            }

            Point positionAfterFirstPart = currentPosition;
            for (Vec2Double vec2Double : splitFirst) {
                positionAfterFirstPart = positionAfterFirstPart.offset(vec2Double);
            }
            List<Vec2Double> splitSecond = splitOnCollision(positionAfterFirstPart, afterMaxHeight, unit.getSize(), game);
            if(splitFirst.size() > 1){//collision has occurred in the second part
                path.addAll(splitSecond);
            } else {
                path.add(afterMaxHeight);
            }
        }

        Point vecStart = currentPosition;
        for (Vec2Double vec2Double : path) {
            Point vecEnd = vecStart.offset(vec2Double);
            debug.draw(new CustomData.Line(vecStart, vecEnd, 0.05f, ColorFloat.YELLOW));
            vecStart = vecEnd;
        }

        return new Path(path, currentPosition, game.getProperties().getUpdatesPerTick(), game.getProperties().getTicksPerSecond());
    }

    private static List<Vec2Double> splitOnCollision(Point startPoint, Vec2Double vector, Vec2Double size, Game game){
        Intersection intersection = Utils.closestIntersectionBox(startPoint, startPoint.offset(vector),
                game.getLevel().getWalls(), size);
        if(intersection != null){
            List<Vec2Double> splitted = new ArrayList<>(2);
            Point intPoint = intersection.point;
            Vec2Double beforeInt = intPoint.buildVector(startPoint);

            //remained velocity after intersection
            Vec2Double afterInt = vector.minus(beforeInt);
            if(intersection.wall.isVertical){
                afterInt.x = 0;
            } else {
                if(afterInt.y > 0){ //ceiling, falling back
                    afterInt.y = -afterInt.y;
                } else { //floor
                    afterInt.y = 0;
                }
            }

            splitted.add(beforeInt);
            splitted.add(afterInt);
            return splitted;
        } else {
            return Collections.singletonList(vector);
        }
    }
}
