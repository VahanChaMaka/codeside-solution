package util;

import model.*;
import model.Vec2Double;

public class Utils {

    public static boolean canHit(Point position, Point target, Game game){
        Vec2Double r = target.buildVector(position);

        for (Wall wall : game.getLevel().getWalls()) {
            Vec2Double s = wall.second.minus(wall.first);
            double d = r.x*s.y - s.x*r.y;
            double u = ((wall.first.x - position.x) * r.y - (wall.first.y - position.y) * r.x) / d;
            double t = ((wall.first.x - position.x) * s.y - (wall.first.y - position.y) * s.x) / d;

            //check only interception fact, ignore interception point
            if(u >= 0 && u <= 1 && t >= 0 && t <= 1){
                return false;
            }
        }

        return true;
    }

    public static double getAverageDamage(Weapon weapon){
        return 0;
    }

    public static double distanceSqr(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    public static double getLargestRootOfQuadraticEquation(double a, double b, double c){
        return (b+Math.sqrt(b*b - 4*a*c))/(2*a);
    }
}
