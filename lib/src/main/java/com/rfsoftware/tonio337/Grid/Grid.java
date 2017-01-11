package com.rfsoftware.tonio337.grid;

/**
 * Created by adlee on 1/11/2017.
 */
public abstract class Grid {
    public interface GridLocation<GL extends GridLocation>{
        // single point distance
        double distance(GL other);

        // path-finding distance
        double distance(GL[] others);

        // single point distance for multi-direction
        double[] direction(GL other);
    }

    public interface GridCollision<GL extends GridLocation>{
        boolean moveTo(Grid grid, GL target);

        boolean collidesWith(GL[] others);

        // TODO: make static
        boolean collidesWith(GL[] self, GL[] others);
    }

    public interface GridAcceleration<GL extends GridLocation>{

        boolean accelerate(double accelDelta);
        boolean accelerate(double xAccelDelta, double yAccelDelta);
    }

    public interface GridDelta<GL extends GridLocation>{
        double maxSpeed = 0;

        boolean moveTo(GL target);
        boolean moveTo(GL target, double maxSpeed);
    }

    public interface GridObject<GO extends GridObject>{
        double TOUCH_DISTANCE = 0;
        double SIGHT_DISTANCE = 0;

        GridLocation location();

        double bearing();

        double distance(GO other);

        boolean isTouching(GO other);

        boolean canSee(GO other);

        void moveTo(GO other);
    }
}
