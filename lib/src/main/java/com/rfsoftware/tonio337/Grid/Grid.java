package com.rfsoftware.tonio337.grid;

/**
 * Created by adlee on 1/11/2017.
 */
public abstract class Grid {
    public interface Location<L extends Location>{
        // single point pathDistance
        double distance(L other);

        // path-finding pathDistance
        double pathDistance(L[] others);

        // single point pathDistance for multi-direction
        double[] direction(L other);
    }

    public interface Object<O extends Object, L extends Location> {
        double TOUCH_DISTANCE = 0;
        double SIGHT_DISTANCE = 0;

        L location();

        double getBearing();

        double getBearingTo(O other);

        double distance(O other);

        //boolean canTouch(O other);

        //boolean canSee(O other);

        void setLocation(O other);
    }

    public interface CollisionOI<L extends Location>{
        boolean moveTo(Grid grid, L target);

        boolean collidesWith(L[] others);

        boolean collidesWith(L[] self, L[] others);
    }
    public interface DeltaOI<L extends Location>{
        boolean moveTo(L target);
        boolean moveTo(L target, double maxSpeed);
    }
    public interface AccelerationOI<L extends Location> extends DeltaOI<L> {

        boolean accelerate(double accelDelta);
        boolean accelerate(double xAccelDelta, double yAccelDelta);
    }
}
