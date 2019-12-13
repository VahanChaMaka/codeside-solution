package util;

import model.*;
import model.Vec2Double;

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

    public static Intersection closestIntersection(Point source, Point target, List<Wall> walls){
        Vec2Double r = target.buildVector(source);

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
    public static Intersection closestIntersectionBox(Point source, Point target, List<Wall> walls, Vec2Double objectSize){
        Intersection intersection = closestIntersection(source, target, walls);
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

    public static boolean isPointInsideRect(Point point, Point leftBotCorner, Vec2Double rectSize){
        Point rightUpCorner = leftBotCorner.offset(rectSize);
        return point.x >= leftBotCorner.x && point.x <= rightUpCorner.x
                && point.y >= leftBotCorner.y && point.y <= rightUpCorner.y;
    }
}
