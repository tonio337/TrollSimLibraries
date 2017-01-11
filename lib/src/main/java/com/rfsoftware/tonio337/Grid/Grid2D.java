package com.rfsoftware.tonio337.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by adlee on 12/30/2016.
 */

public class Grid2D {

    private double xMin = 0;
    private double yMin = 0;
    private double xMax = 100;
    private double yMax = 100;
    private double xStep = 5;
    private double yStep = 5;

    public int  xNumRegions() { return (int) Math.ceil((xMax - xMin) / xStep); }
    public int  yNumRegions() { return (int) Math.ceil((yMax - yMin) / yStep); }

    public int xRegion(GridLocation2D gl) { return (int) Math.ceil((gl.x - xMin) / xStep); }
    public int yRegion(GridLocation2D gl) { return (int) Math.ceil((gl.y - yMin) / yStep); }

    public static ArrayList<GridObject2D> gridObject2DList = new ArrayList<>();


    //private static final double METERS_TO_FEET = 3.28084;
    /*enum DistanceUnit {
        METER(1.0),
        KILOMETER(.001),
        MILLIMETER(1000),
        FOOT(METERS_TO_FEET),
        INCH(METERS_TO_FEET*12),
        YARD(METERS_TO_FEET/3),
        MILE(METERS_TO_FEET/5280);


        private final double baseDistance; // stored in meters

        DistanceUnit(double baseDistance){
            this.baseDistance=baseDistance;
        }

        private double baseDistance(){
            return baseDistance;
        }

        public double convertTo(DistanceUnit other){
            return this.baseDistance() * other.baseDistance();
        }

        public static double convert(DistanceUnit first, DistanceUnit second){
            return first.convertTo(second);
        }
    }
    */

    // GridLocation2D - Wrapper class that implements all GridLocation related interfaces.
    // Can be overwritten to make a custom 2D GridLocation, or extended to be used as is.
    class GridLocation2D
            implements Grid.GridLocation<GridLocation2D>
            //,GridCollision<GridLocation2D>
            //,GridAcceleration<GridLocation2D>
            //,GridDelta<GridLocation2D>
    {

        double x;
        double y;

        GridLocation2D() { this(0.0,0.0); }
        GridLocation2D(double x, double y){
            this.x = x;
            this.y = y;
        }

        // 2D distance formula *** sqrt((y2-y1)^2+(x2-x1)^2)
        @Override
        public double distance(GridLocation2D other){
            return Math.sqrt(
                    Math.pow(other.y-this.y,2.0)+
                            Math.pow(other.x-this.x,2.0));
        }

        @Override
        public double distance(GridLocation2D[] others) {
            double distance = 0;
            for (int gl = 0; gl < others.length-1; gl++){
                distance += others[gl].distance(others[gl+1]);
            }
            return distance;
        }

        public double[] direction(GridLocation2D other){
            // TODO: Fill out multi dimensional direction
            return new double[]{0};
        }

        public double direction(double[] directionArray){
            // TODO: Convert multi dim direction to single direction
            return 0;
        }

        @Override
        public String toString(){
            return "(" + x + "," + y + ")";
        }

        /*
        @Override
        public boolean moveTo(Grid2D Grid, GridLocation2D target) {
            return false;
        }
        */
    }
    abstract class GridObject2D implements Grid.GridObject<GridObject2D> {

        double TOUCH_DISTANCE = 2.5;
        double SIGHT_DISTANCE = 50;
        double SIGHT_BEARING = 60;

        String name = "Weapon";

        private GridLocation2D loc;

        GridObject2D(double x, double y){
            loc = new GridLocation2D(x,y);
            gridObject2DList.add(this);
        }

        public String name(){ return name; }

        @Override
        public GridLocation2D location() {
            return loc;
        }

        public double distance(GridObject2D other){ return location().distance(other.location()); }

        public boolean isTouching(GridObject2D other){
            return distance(other) < TOUCH_DISTANCE;
        }

        @Override
        public boolean canSee(GridObject2D other) {
            return distance(other) < SIGHT_DISTANCE;
        }

        @Override
        public void moveTo(GridObject2D other) {

        }

        public boolean inSameRegion(GridObject2D other){
            return xRegion(this.location()) == xRegion(other.location()) &&
                    yRegion(this.location()) == yRegion(other.location());
        }


        @Override
        public String toString(){
            return loc.toString();
        }

    }

}