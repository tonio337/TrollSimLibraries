package com.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by adlee on 12/30/2016.
 */

public class Grid2D {

    private double xMin = 15;
    private double yMin = 15;
    private double xMax = 85;
    private double yMax = 85;
    private double xStep = 5;
    private double yStep = 5;

    private int  xNumRegions() { return (int) Math.ceil((xMax - xMin) / xStep); }
    private int  yNumRegions() { return (int) Math.ceil((yMax - yMin) / yStep); }

    private int xRegion(GridLocation2D gl) { return (int) Math.ceil((gl.x - xMin) / xStep); }
    private int yRegion(GridLocation2D gl) { return (int) Math.ceil((gl.y - yMin) / yStep); }

    public static ArrayList<GridObject2D> gridObject2DList = new ArrayList<>();
    public static ArrayList<Spell> spellList = new ArrayList<>();

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

    private interface GridLocation<GL extends GridLocation>{
        double distance(GL other);
        double distance(GL[] others);



        double[] direction(GL other);
    }
    private interface GridCollision<GL extends GridLocation>{
        boolean moveTo(Grid2D grid, GL target);

        boolean collidesWith(GL[] others);

        // TODO: make static
        boolean collidesWith(GL[] self,GL[] others);
    }
    private interface GridAcceleration<GL extends GridLocation>{

        boolean accelerate(double accelDelta);
        boolean accelerate(double xAccelDelta,double yAccelDelta);
    }
    private interface GridDelta<GL extends GridLocation>{
        double maxSpeed = 0;

        boolean moveTo(GL target);
        boolean moveTo(GL target, double maxSpeed);
    }

    private interface GridObject<GO extends GridObject>{
        double TOUCH_DISTANCE = 0;
        double SIGHT_DISTANCE = 0;

        GridLocation location();

        double bearing();

        double distance (GO other);

        boolean isTouching(GO other);

        boolean canSee(GO other);

        void moveTo(GO other);
    }

    // GridLocation2D - Wrapper class that implements all GridLocation related interfaces.
    // Can be overwritten to make a custom 2D GridLocation, or extended to be used as is.
    private class GridLocation2D
            implements GridLocation<GridLocation2D>
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
        public boolean moveTo(Grid2D grid, GridLocation2D target) {
            return false;
        }
        */
    }
    abstract class GridObject2D implements GridObject<GridObject2D>{

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

        public boolean castSpell(Spell spell,GridObject2D other){

            System.out.println(name + " casts " + spell.name + " on " + other.name() +
                    " with effective range of " + spell.effRange + "...");

            // auto-fail if effective range or success rate is not positive
            if (spell.effRange <= 0 || spell.succRate <= 0) return false;

            // auto-success if within effective range
            double dist = this.distance(other);
            if (dist < spell.effRange) return true;

            // percentage success dependent on distance from max range
            // auto-fail if outside max range
            double spreadPercentage = (dist - spell.maxRange) / (spell.effRange - spell.maxRange);
            System.out.println(String.format("Spell percentage: %1.2f Spread percentage: %2.2f",
                    spell.succRate, spreadPercentage));
            return spreadPercentage > spell.succRate;
        }

        @Override
        public String toString(){
            return loc.toString();
        }

    }

    class Player2D extends GridObject2D{

        // compass orientation to which way the player is facing.
        private double bearing = 0;

        Player2D(String name, double x, double y) {
            super(x,y);
            this.name = name;
        }

        @Override
        public String toString(){
            return "2D Player " + name +
                    " @ location " + super.toString() +
                    " in X region " + xRegion(location()) + "/" + xNumRegions() +
                    " and Y region " + yRegion(location()) + "/" + yNumRegions();

        }

        // Auto-adjusts bearing to be between -180 and 180
        public double bearing(){
            adjustBearing();
            return bearing;
        }

        void adjustBearing(){
            adjustBearing(this.bearing);
        }

        double adjustBearing(double bearing){
            while (bearing < -180 || bearing > 180)
                bearing -= Math.signum(bearing)*360;
            return bearing;
        }

        public Player2D setBearing(double bearing){
            this.bearing = bearing;
            bearing();
            return this;
        }

        public Player2D setBearing(Player2D other){
            return this.setBearing(other,0);
        }

        public Player2D setBearing(Player2D other, double offset){
            this.bearing = this.getAbsBearing(other) + offset;
            return this;
        }

        public double getAbsBearing(GridObject2D other){
            double x = other.location().x-this.location().x;
            double y = other.location().y-this.location().y;
            double degrees = Math.atan2(y,x);

            //return adjustBearing(180 - (90*Math.signum(x)) - (degrees*Math.signum(x)));
            return Math.signum(x)*(90 - degrees);
        }
        public double getRelBearing(GridObject2D other){
            return getAbsBearing(other) - bearing();
        }

        public boolean canSee(GridObject2D other){
            return this.distance(other) < SIGHT_DISTANCE &&
                    Math.abs(this.getRelBearing(other)) < SIGHT_BEARING;
        }

    }
    class Weapon2D extends GridObject2D{
        private int str = 40;

        public int getStr() { return str; }

        public Weapon2D(double x, double y){
            super(x,y);
        }
        public Weapon2D(String name, int str, double x, double y){
            this(str,x,y);
            this.name = name;
        }
        public Weapon2D(int str, double x, double y){
            this(x,y);
            this.str = str;
        }


        @Override
        public double bearing() {
            return 0;
        }
    }

    private abstract class Spell {
        String name;
        double effRange;
        double maxRange;
        double succRate;

        Spell(){
            this.name = "Unnamed Spell - No effect";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
            spellList.add(this);
        }

        public boolean cast(GridObject2D caster, GridObject2D target){

            //System.out.println("Casting " + name + " with effective range of " + effRange + "...");

            return caster.castSpell(this,target);
        }
    }
    class Fire extends Spell {
        Fire(){
            super();
            this.name = "Fire";
            this.effRange = 20;
            this.maxRange = 25;
            this.succRate = .75;
        }
    }
    class Buff extends Spell {
        Buff(){
            super();
            this.name = "Buff";
            this.effRange = 100;
            this.maxRange = 1000;
            this.succRate = 1;
        }
    }
    class Fail extends Spell {
        Fail(){
            super();
            this.name = "FAIL";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
        }
    }

    public static void main(String args[]){
        Grid2D grid2D = new Grid2D();

        System.out.println("Atan 18/10 = " + Math.toDegrees(Math.atan2(10,18)));

        Player2D player1 = grid2D.new Player2D("Anne",20.1,20.1);
        grid2D.new Player2D("Bob",30,38).setBearing(player1);
        grid2D.new Player2D("Charlie",24,22);
        grid2D.new Weapon2D("Ultima Weapon",40,20,21);

        grid2D.new Fire();
        grid2D.new Buff();
        grid2D.new Fail();

        for(GridObject2D go : gridObject2DList)
            System.out.println(go.name() + " - " + go.getClass().getSimpleName());
        System.out.println();

        Iterator<GridObject2D> go2dFirstIterator = gridObject2DList.iterator();
        while (go2dFirstIterator.hasNext()){

            GridObject2D go2dFirst = go2dFirstIterator.next();
            System.out.println("Grid2D capabilities of " + go2dFirst.name());

            Iterator<GridObject2D> go2dSecondIterator = gridObject2DList.iterator();
            while (go2dSecondIterator.hasNext()){

                GridObject2D go2dSecond = go2dSecondIterator.next();

                if (go2dFirst == go2dSecond) continue;

                System.out.println(String.format(Locale.getDefault(),"%s is a distance of %.2f from %s",
                        go2dFirst.name(),
                        go2dFirst.distance(go2dSecond),
                        go2dSecond.name()));

                try {
                    System.out.println(String.format(Locale.getDefault(), "%s is an absolute bearing of %.2f from %s",
                            go2dFirst.name(),
                            ((Player2D) go2dFirst).getAbsBearing(go2dSecond),
                            go2dSecond.name()));

                    System.out.println(String.format(Locale.getDefault(), "%s is an relative bearing of %.2f from %s",
                            go2dFirst.name(),
                            ((Player2D) go2dFirst).getRelBearing(go2dSecond),
                            go2dSecond.name()));
                }
                catch (Exception e) {
                    System.out.println(go2dFirst.name() + " cannot get a bearing on " +
                            go2dSecond.name());
                }

                if (go2dFirst.canSee(go2dSecond)) {
                    System.out.println(go2dFirst.name() + " can see " + go2dSecond.name());

                    for (Spell spell : spellList){
                        try{
                            if (spell.cast(go2dFirst,go2dSecond))
                                System.out.println("DAI HUGE SUCCESS!");
                            else System.out.println("Fail.");
                        }
                        catch (Exception e){
                            System.out.println(go2dFirst.name() + " is unable to cast " +
                                    spell.name + " on " +
                                    go2dSecond.name());
                        }
                    }
                }

                if (go2dFirst.inSameRegion(go2dSecond))
                    System.out.println(go2dFirst.name() + " is in the same reigon as " + go2dSecond.name());

                if (go2dFirst.isTouching(go2dSecond))
                    System.out.println(go2dFirst.name() + " is touching " + go2dSecond.name());

            }

            System.out.println();
        }
    }
}