package util;

import model.*;
import model.Vec2Double;

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
}
