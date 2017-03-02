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
    ArrayList gridObject2DList;

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

        // 2D pathDistance formula *** sqrt((y2-y1)^2+(x2-x1)^2)
        @Override
        public double distance(Location2D other){
            return Math.sqrt(
                    Math.pow(other.y-this.y,2.0)+
                            Math.pow(other.x-this.x,2.0));
        }

        @Override
        public double pathDistance(Location2D[] others) {
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

        private String name = "(unnamed)";
        private Location2D loc;

        public Grid2D baseGrid;

        // standard constructor
        Object2D(double x, double y, Grid2D grid){
            loc = new Location2D(x,y);
            grid.gridObject2DList.add(this);
            baseGrid = grid;
        }

        public Object2D(String name, double x, double y, Grid2D grid){
            this(x,y,grid);
            this.name = name;
        }

        public String name(){ return name; }

        @Override
        public Location2D location() {
            return loc;
        }

        @Override
        public double distance(Object2D other){ return location().distance(other.location()); }

        @Override
        public void setLocation(Object2D other) {
            setLocation(other.location().x,other.location().y);
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