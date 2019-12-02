package util;

import model.*;
import model.Vec2Double;

public class Utils {

    public static boolean canHit(Vec2Double position, Vec2Double target, Game game){
        Vec2Double r = target.minus(position);

        for (Wall wall : game.getLevel().getWalls()) {
            Vec2Double s = wall.second.minus(wall.first);
            Double d = r.x*s.y - s.x*r.y;
            Double u = ((wall.first.x - position.x) * r.y - (wall.first.y - position.y) * r.x) / d;
            Double t = ((wall.first.x - position.x) * s.y - (wall.first.y - position.y) * s.x) / d;

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

    public static double distanceSqr(Vec2Double a, Vec2Double b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }
}
