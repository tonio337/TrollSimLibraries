package com.rfsoftware.tonio337.grid;

import org.jetbrains.annotations.Contract;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.Math.floor;

/**
 * Project: TrollSimInterface
 * Created by Antonio on 12/30/2016.
 */

public class Grid2D extends Grid{

    private static final double xMinDefault = 0;
    private static final double yMinDefault = 0;
    private static final double xMaxDefault = 100;
    private static final double yMaxDefault = 100;
    private static final double xStepDefault = 5;
    private static final double yStepDefault = 5;
    private static final double borderMarginDefault = 0;

    private double xMin;
    private double yMin;
    private double xMax;
    private double yMax;
    private double xStep;
    private double yStep;
    private double borderMargin;

    // TODO: Implement or remove baseGrid
    private Grid2D baseGrid;
    final ArrayList<Object2D> gridObject2DList;

    private Grid2D(double xMin, double xMax, double yMin, double yMax, double xStep, double yStep, double borderMargin) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.xStep = xStep;
        this.yStep = yStep;
        this.borderMargin = borderMargin;

        gridObject2DList = new ArrayList<>();
    }

    Grid2D(double xMin, double xMax, double yMin, double yMax, double xStep, double yStep) {
        this(xMin,xMax,yMin,yMax,xStep,yStep,borderMarginDefault);
    }
    public Grid2D(){
        this(xMinDefault,xMaxDefault,yMinDefault,yMaxDefault,xStepDefault,yStepDefault);
    }

    public double getxMin() { return xMin; }
    public double getyMin() { return yMin; }
    public double getxMax() { return xMax; }
    public double getyMax() { return yMax; }
    public double getxStep() { return xStep; }
    public double getyStep() { return yStep; }

    public double getxSize() { return Math.abs(xMax-xMin); }
    public double getySize() { return Math.abs(yMax-yMin); }


    // actual numRegions + 1 to allow for border case
    @Contract(pure = true)
    int numXRegions() { return (int) floor((xMax - xMin) / xStep) + 1; }
    @Contract(pure = true)
    int numYRegions() { return (int) floor((yMax - yMin) / yStep) + 1; }

    int numRegions() { return numXRegions() * numYRegions(); }

    @Contract(pure = true)
    int xRegion(Location2D gl) { return (int) floor(xRegionDouble(gl)) + 1; }
    @Contract(pure = true)
    int yRegion(Location2D gl) { return (int) floor(yRegionDouble(gl)) + 1; }

    @Contract(pure = true)
    private double xRegionDouble(Location2D gl) { return (gl.x - xMin) / xStep; }
    @Contract(pure = true)
    private double yRegionDouble(Location2D gl) { return (gl.y - yMin) / yStep; }

    @Contract(pure = true)
    boolean isXBorder(Location2D gl) { return xRegionDouble(gl) - floor(xRegionDouble(gl)) <= borderMargin; }
    @Contract(pure = true)
    boolean isYBorder(Location2D gl) { return yRegionDouble(gl) - floor(yRegionDouble(gl)) <= borderMargin; }

    double getXCoord(int xRegion){

        if (xRegion < 1 || xRegion > numXRegions()) throw new IndexOutOfBoundsException();
        return (xRegion-1)*xStep + xMin;
    }
    double getYCoord(int yRegion){
        if (yRegion < 1 || yRegion > numYRegions()) throw new IndexOutOfBoundsException();
        return (yRegion-1)*yStep + yMin;
    }

    Location2D getCoords(int xRegion, int yRegion){
        return new Location2D(getXCoord(xRegion),getYCoord(yRegion));
    }

    // Serialized region ID from 1 to numRegions or 0 if not in grid
    int regionID(Location2D gl) {
        if (!inGrid(gl)) return 0;
        return ((yRegion(gl)-1) * numXRegions()) + xRegion(gl);
    }

    // checks if given coords are between grid min and max values
    private boolean inGrid(Location2D gl) {
        return (gl.x >= xMin &&
                gl.x <= xMax &&
                gl.y >= yMin &&
                gl.y <= yMax);
    }

    public void add(Object2D object){
        if (!gridObject2DList.contains(object))
            gridObject2DList.add(object);
    }

    void remove(Object2D object){
        gridObject2DList.remove(object);
    }


    // Location2D - Wrapper class that implements all Location related interfaces.
    // Can be overwritten to make a custom 2D Location, or extended to be used as is.
    static class Location2D
            implements Grid.Location<Location2D>
            //,CollisionOI<Location2D>
            //,AccelerationOI<Location2D>
            //,DeltaOI<Location2D>
    {

        double x;
        double y;

        Location2D() { this(0.0,0.0); }
        Location2D(double x, double y){
            this.x = x;
            this.y = y;
        }

        // 2D distance formula *** sqrt((y2-y1)^2+(x2-x1)^2)
        @Override
        public double distance(Location2D other){
            return Math.sqrt(
                    Math.pow(other.y-this.y,2.0)+
                            Math.pow(other.x-this.x,2.0));
        }

        @Override
        public double distance(Location2D[] others) {
            double distance = 0;
            for (int gl = 0; gl < others.length-1; gl++){
                distance += others[gl].distance(others[gl+1]);
            }
            return distance;
        }

        public double[] direction(Location2D other){
            // TODO: Fill out multi dimensional direction
            return new double[]{0};
        }

        public double direction(double[] directionArray){
            // TODO: Convert multi dim direction to single direction
            return 0;
        }

        @Override
        public String toString(){
            return String.format(Locale.getDefault(),"Location: (%.2f,%.2f)",x,y);
        }
        //public String toString(){ return "Location: (" + x + "," + y + ")"; }

        /*
        @Override
        public boolean setLocation(Grid2D Grid, Location2D target) {
            return false;
        }
        */
    }
    abstract public static class Object2D implements Grid.Object<Object2D,Location2D> {

        double bearing = 0;

        double touchDistance = 2.5;
        double sightDistance = 50;
        // end-to-end horizontal field of vision in degrees
        double fieldOfVision = 170;

        private String name = "(unnamed)";
        private Location2D loc;

        // standard constructor
        Object2D(double x, double y, Grid2D grid){
            loc = new Location2D(x,y);
            grid.gridObject2DList.add(this);
        }

        public Object2D(String name, double x, double y, Grid2D grid){
            this(x,y,grid);
            this.name = name;
        }

        public Grid2D baseGrid;

        public String name(){ return name; }


        // returns coord of bearing from center with distance of 1
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
        public Location2D location() {
            return loc;
        }

        @Override
        public double distance(Object2D other){ return location().distance(other.location()); }

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

        @Override
        public boolean isTouching(Object2D other){
            return distance(other) < touchDistance;
        }

        @Override
        public boolean canSee(Object2D other) {
            return distance(other) < sightDistance &&
                    getRelativeBearingTo(other) > -fieldOfVision/2 &&
                    getRelativeBearingTo(other) < fieldOfVision/2;
        }

        @Override
        public void setLocation(Object2D other) {
            setLocation(other.location().x,other.location().y);
        }

        public void setFieldOfVision(double fov) {
            fieldOfVision = Math.abs(fov);
        }

        @Override
        public String toString(){
            return loc.toString();
        }

        public boolean inSameRegion(Grid2D grid, Object2D other){
            return grid.xRegion(this.location()) == grid.xRegion(other.location()) &&
                    grid.yRegion(this.location()) == grid.yRegion(other.location());
        }

        protected void setLocation(double x, double y){
            loc.x = x;
            loc.y = y;
        }
    }
}