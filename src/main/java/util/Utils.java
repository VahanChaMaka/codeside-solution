package util;

import model.*;
import model.Vec2Double;

import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static double getAverageDamage(Weapon weapon){
        return 0;
    }

    public static double distanceSqr(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    public static double getLargestRootOfQuadraticEquation(double a, double b, double c){
        return (-b+Math.sqrt(b*b - 4*a*c))/(2*a);
    }

    public static double getSmallestRootOfQuadraticEquation(double a, double b, double c){
        return (-b-Math.sqrt(b*b - 4*a*c))/(2*a);
    }

    public static Point closestIntersection(Point source, Point target, List<Wall> walls){
        Vec2Double r = target.buildVector(source);

        Point closest = null;
        for (Wall wall : walls) {
            Vec2Double s = wall.second.minus(wall.first);
            double d = r.x*s.y - s.x*r.y;
            double u = ((wall.first.x - source.x) * r.y - (wall.first.y - source.y) * r.x) / d;
            double t = ((wall.first.x - source.x) * s.y - (wall.first.y - source.y) * s.x) / d;

            //check only intersection fact, ignore intersection point
            if(u >= 0 && u <= 1 && t >= 0 && t <= 1){
                Point intersection = source.offset(r.scale(t));
                if(closest == null || source.buildVector(intersection).length() <= source.buildVector(closest).length()){
                    closest = intersection;
                }
            }
        }

        return closest;
    }
}
