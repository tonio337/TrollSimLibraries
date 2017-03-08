package com.rfsoftware.tonio337.grid;

import com.rfsoftware.tonio337.grid.Grid2D.Object2D;
import com.rfsoftware.tonio337.grid.Player2D.Bearing.*;

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

        //translate to Unit Circle based Degrees
        bearing = Bearing.translate(new Bearing(bearing,
                        Direction.UP,
                        Orientation.CLOCKWISE),
                Direction.RIGHT,
                Orientation.COUNTERCLOCKWISE);

        bearing = Math.toRadians(bearing);

        return new double[] {Math.cos(bearing),Math.sin(bearing)};
    }



    public static double bearingX(double bearing, int offset){
        return bearing(bearing + offset)[0];
    }
    public static double bearingY(double bearing, int offset){
        return bearing(bearing + offset)[1];
    }
    public static double bearingX(double bearing){
        return bearingX(bearing,0);
    }
    public static double bearingY(double bearing){
        return bearingY(bearing,0);
    }

    public double getBearingX(int offset){
        return bearingX(bearing,offset);
    }
    public double getBearingY(int offset){
        return bearingY(bearing,offset);
    }
    public double getBearingX(){
        return bearingX(bearing);
    }
    public double getBearingY(){
        return bearingY(bearing);
    }

    @Override
    public double getBearing() { return Bearing.recenterBearing(bearing); }

    @Override
    public double getBearingTo(Object2D other) {
        if (this == other) return 0.0;

        double xDelta = other.location().x-location().x;
        double yDelta = other.location().y-location().y;

        double unitAngle = Math.toDegrees(Math.atan2(yDelta,xDelta));

        //translate to Grid based degrees
        return Bearing.translate(new Bearing(unitAngle,
                                            Direction.RIGHT,
                                            Orientation.COUNTERCLOCKWISE),
                                Direction.UP,
                                Orientation.CLOCKWISE);

    }

    double getRelativeBearingTo(Object2D other) {
        double bearing = Bearing.recenterBearing(getBearingTo(other)- getBearing());
        if (bearing > 180) bearing -= 360;
        return bearing;
    }

    Player2D setMyBearingTo(double bearing) {
        this.bearing = Bearing.recenterBearing(bearing);
        return this;
    }

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


    public static class Bearing{

        // TODO: Create Base Direction Enum Up Down Left Right
        // TODO: Create Base Orientation Enum Clockwise CounterClockwise

        public enum Direction {
            UP(0), RIGHT(1), DOWN(2), LEFT(3);

            private final int value;

            Direction(int value){
                this.value = value;
            }

            public int getValue() {
                return this.value;
            }
        }
        public enum Orientation{
            CLOCKWISE(1),COUNTERCLOCKWISE(-1);

            private final int value;

            Orientation(int value){
                this.value = value;
            }

            public int getValue() {
                return this.value;
            }
        }

        private Double degrees;
        private Direction direction;
        private Orientation orientation;

        public Double getDegrees(){
            return degrees;
        }

        public Bearing  (double degrees, Direction baseDirection, Orientation baseOrientation){
            setBearing(degrees,baseDirection,baseOrientation);
        }

        public Bearing(){
            this(0, Direction.UP, Orientation.CLOCKWISE);
        }

        public static double translate(Bearing source, Direction direction, Orientation orientation) {

            double target = source.degrees;

            //(target.dir-source.dir) * 90 deg
            double directionMod = modulo((source.direction.getValue() - direction.getValue()),4) * 90;

            target -= directionMod * source.orientation.getValue() * -1;

            if (source.orientation.getValue() * orientation.getValue() == -1)
                target = 360-target;

            target = recenterBearing(target);

            return target;
        }

        public double translate(Direction direction, Orientation orientation){
            return translate(this,direction,orientation);
        }

        private void setBearing (double degrees, Direction direction, Orientation orientation){
            this.degrees = degrees;
            this.direction = direction;
            this.orientation = orientation;
            recenterBearing();
        }

        public void setBearing (double degrees){
            setBearing(degrees,this.direction,this.orientation);
        }

        private void recenterBearing(){
            degrees = modulo(degrees,360.0);
        }
        public static double recenterBearing(double degrees) {
            return modulo(degrees,360.0);
        }

        private static double modulo(double x, double y){
            double remainder = x%y;
            if (remainder < 0) remainder+=y;
            return remainder;
        }
    }
}
