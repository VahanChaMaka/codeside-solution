package util;

import model.*;
import model.Vec2Double;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static double distanceSqr(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    public static double getLargestRootOfQuadraticEquation(double a, double b, double c){
        return (-b+Math.sqrt(b*b - 4*a*c))/(2*a);
    }

    public static double getSmallestRootOfQuadraticEquation(double a, double b, double c){
        return (-b-Math.sqrt(b*b - 4*a*c))/(2*a);
    }

    public static Intersection closestIntersection(Point source, Point target, List<Wall> walls, Debug debug){
        Vec2Double r = target.buildVector(source);

        if(debug != null) {
            //debug.draw(new CustomData.Line(source, target, 0.05f, ColorFloat.WHITE));
        }

        Intersection closest = null;
        for (Wall wall : walls) {
            Vec2Double s = wall.second.buildVector(wall.first);
            double d = r.x*s.y - s.x*r.y;
            double u = ((wall.first.x - source.x) * r.y - (wall.first.y - source.y) * r.x) / d;
            double t = ((wall.first.x - source.x) * s.y - (wall.first.y - source.y) * s.x) / d;

            //check only point fact, ignore point point
            if(u >= 0 && u <= 1 && t >= 0 && t <= 1){
                Point intersectionPoint = source.offset(r.scale(t));
                if(closest == null || source.buildVector(intersectionPoint).length() <= source.buildVector(closest.point).length()){
                    closest = new Intersection(intersectionPoint, wall);
                }
            }
        }

        return closest;
    }

    // consider size of an object (bullet or unit)
    // source point should be a center of an object
    @Deprecated
    public static Intersection closestIntersectionBoxByCenter(Point source, Point target, List<Wall> walls, Vec2Double objectSize){
        Intersection intersection = closestIntersection(source, target, walls, null);
        if(intersection == null){
            return null;
        }

        Vec2Double ray = target.buildVector(source);
        Vec2Double normedRay = ray.scale(1/ray.length());
        Vec2Double intAxis = intersection.wall.isVertical? new Vec2Double(0, 1) : new Vec2Double(1, 0);
        double axisSize = intersection.wall.isVertical? objectSize.x/2 : objectSize.y/2;

        double angleBetweenRayAndWallSin = Math.sqrt(1 - Math.pow(ray.dot(intAxis)/(ray.length()*intAxis.length()), 2));
        double offsetLength = axisSize/angleBetweenRayAndWallSin;
        Vec2Double offsetVec = normedRay.scaleThis(-offsetLength);

        return new Intersection(intersection.point.offset(offsetVec), intersection.wall);
    }

    public static Intersection closestIntersectionBox(Point source, Point target, List<Wall> walls, Vec2Double objectSize){
        return closestIntersectionBox(source, target, walls, objectSize, null);
    }

    //cast a ray from each corner of the object and check collision
    public static Intersection closestIntersectionBox(Point source, Point target, List<Wall> walls, Vec2Double objectSize, Debug debug){
        Point leftDownCorner = source.offset(-objectSize.x/2, -objectSize.y/2);
        Point leftUpCorner = leftDownCorner.offset(0, objectSize.y);
        Point rightUpCorner = leftDownCorner.offset(objectSize);
        Point rightDownCorner = leftDownCorner.offset(objectSize.x, 0);

        Point leftDownTarget = target.offset(-objectSize.x/2, -objectSize.y/2);
        Point leftUpTarget = leftDownTarget.offset(0, objectSize.y);
        Point rightUpTarget = leftDownTarget.offset(objectSize);
        Point rightDownTarget = leftDownTarget.offset(objectSize.x, 0);

        List<Pair<Point, Intersection>> intersections = new ArrayList<>(4);
        intersections.add(new Pair<>(leftDownCorner, closestIntersection(leftDownCorner, leftDownTarget, walls, debug)));
        intersections.add(new Pair<>(leftUpCorner, closestIntersection(leftUpCorner, leftUpTarget, walls, debug)));
        intersections.add(new Pair<>(rightUpCorner, closestIntersection(rightUpCorner, rightUpTarget, walls, debug)));
        intersections.add(new Pair<>(rightDownCorner, closestIntersection(rightDownCorner, rightDownTarget, walls, debug)));

        Pair<Point, Intersection> closest = null;
        for (int i = 0; i < intersections.size(); i++) {
            Pair<Point, Intersection> pointToInt = intersections.get(i);
            if(debug != null && pointToInt.another != null){
                //debug.draw(new CustomData.Rect(intersections.get(i).another.point, new Vec2Double(0.1f, 0.1f), ColorFloat.WHITE));
            }
            if(closest == null || closest.another == null || pointToInt.another != null
                    && pointToInt.another.point.buildVector(pointToInt.one).length()
                    < closest.another.point.buildVector(closest.one).length()){
                closest = pointToInt;
            }
        }

        if(closest == null || closest.another == null){
            return null;
        }

        Intersection centerPositionOnCol = null;
        if(closest.one == leftDownCorner){
            centerPositionOnCol = new Intersection(closest.another.point.offset(objectSize.x/2, objectSize.y/2), closest.another.wall);
        } else if(closest.one == leftUpCorner){
            centerPositionOnCol = new Intersection(closest.another.point.offset(objectSize.x/2, -objectSize.y/2), closest.another.wall);
        } else if(closest.one == rightUpCorner){
            centerPositionOnCol = new Intersection(closest.another.point.offset(-objectSize.x/2, -objectSize.y/2), closest.another.wall);
        } else if(closest.one == rightDownCorner) {
            centerPositionOnCol = new Intersection(closest.another.point.offset(-objectSize.x/2, objectSize.y/2), closest.another.wall);
        }

        return centerPositionOnCol;
    }

    public static boolean isPointInsideRect(Point point, Point leftBotCorner, Vec2Double rectSize){
        Point rightUpCorner = leftBotCorner.offset(rectSize);
        return point.x >= leftBotCorner.x && point.x <= rightUpCorner.x
                && point.y >= leftBotCorner.y && point.y <= rightUpCorner.y;
    }

    private static class Pair<K, V>{
        private K one;
        private V another;

        public Pair(K one, V another) {
            this.one = one;
            this.another = another;
        }
    }
}
