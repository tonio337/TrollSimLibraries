package com.rfsoftware.tonio337.grid;

import com.rfsoftware.tonio337.grid.Grid2D.Object2D;

/**
 * Project: TrollSimInterface
 * Package: com.rfsoftware.tonio337.grid
 * Created by adlee on 2/23/2017.
 */ // Player:
// This player class implements the
public class Player2D extends Object2D {

    double bearing = 0;
    double touchDistance = 2.5;
    double sightDistance = 50;
    // end-to-end horizontal field of vision in degrees
    double fieldOfVision = 170;

    // compass orientation to which way the player is facing.
    // private double getBearing;

    // full delcaration
    public Player2D(String name, double x, double y, double b, Grid2D grid) {
        super(name, x, y, grid);
        bearing = b;
    }

    // -bearing: randomizes bearing
    public Player2D(String name, double x, double y, Grid2D grid) {
        this(name,x,y,Math.random()*360,grid);
    }

    // -name,x,y: static name, randomizes x and y
    public Player2D(String name, Grid2D grid) {
        this(name,
                Math.random()*grid.getxSize()+grid.getxMin(),
                Math.random()*grid.getySize()+grid.getyMin(),
                grid);
    }

    // -name,x,y: static name, randomizes x and y
    public Player2D(Grid2D grid) {
        this("(unnamed)", grid);
    }

    @Override
    public String toString() {
        return "2D Player " + name() +
                " @ location " + super.toString();

    }

    // returns coord of bearing from center with pathDistance of 1
    static private double[] bearing(double bearing) {
        bearing = Math.toRadians(bearing);
        return new double[] {Math.cos(bearing),Math.sin(bearing)};
    }

    public double getBearingX(int offset){
        return bearing(bearing + offset)[0];
    }

    public double getBearingY(int offset){
        return bearing(bearing + offset)[1];
    }

    public double getBearingX(){
        return getBearingX(0);
    }

    public double getBearingY(){
        return getBearingY(0);
    }

    @Override
    public double getBearing() { return bearing; }

    @Override
    public double getBearingTo(Object2D other) {
        if (this == other) return 0.0;

        double xDelta = other.location().x-location().x;
        double yDelta = other.location().y-location().y;

        double unitAngle = Math.toDegrees(Math.atan2(yDelta,xDelta));
        double myBearingTo = 90 - unitAngle;

        return myBearingTo % 360;
    }

    double getRelativeBearingTo(Object2D other) { return (getBearingTo(other)- getBearing())%180; }

    void setMyBearingTo(double bearing) { this.bearing = bearing%360; }

    void setMyBearingTo(Object2D other) {
        if (this==other) return;
        setMyBearingTo(getBearingTo(other));
    }



    public boolean canTouch(Object2D other){
        return distance(other) < touchDistance;
    }


    public boolean canSee(Object2D other) {
        return distance(other) < sightDistance &&
                getRelativeBearingTo(other) > -fieldOfVision/2 &&
                getRelativeBearingTo(other) < fieldOfVision/2;
    }
    public void setFieldOfVision(double fov) {
        fieldOfVision = Math.abs(fov);
        if (fieldOfVision>180) fieldOfVision=180;
    }

}
