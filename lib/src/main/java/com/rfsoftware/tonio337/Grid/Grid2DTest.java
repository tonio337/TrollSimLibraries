package com.rfsoftware.tonio337.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by adlee on 1/11/2017.
 */
public class Grid2DTest extends Grid2D{

    public static boolean castSpell(GridObject2D caster, GridObject2D target, Spell spell){

        System.out.println(caster.name() + " casts " + spell.name + " on " + target.name() +
                " with effective range of " + spell.effRange + "...");

        // auto-fail if effective range or success rate is not positive
        if (spell.effRange <= 0 || spell.succRate <= 0) return false;

        // auto-success if within effective range
        double dist = caster.distance(target);
        if (dist < spell.effRange) return true;

        // percentage success dependent on distance from max range
        // auto-fail if outside max range
        double spreadPercentage = (dist - spell.maxRange) / (spell.effRange - spell.maxRange);
        System.out.println(String.format("Spell percentage: %1.2f Spread percentage: %2.2f",
                spell.succRate, spreadPercentage));
        return spreadPercentage > spell.succRate;
    }

    class Player2D extends GridObject2D {

        // compass orientation to which way the player is facing.
        private double bearing = 0;

        Player2D(String name, double x, double y) {
            super(x, y);
            this.name = name;
        }

        @Override
        public String toString() {
            return "2D Player " + name +
                    " @ location " + super.toString() +
                    " in X region " + xRegion(location()) + "/" + xNumRegions() +
                    " and Y region " + yRegion(location()) + "/" + yNumRegions();

        }

        // Auto-adjusts bearing to be between -180 and 180
        public double bearing() {
            adjustBearing();
            return bearing;
        }

        void adjustBearing() {
            adjustBearing(this.bearing);
        }

        double adjustBearing(double bearing) {
            while (bearing < -180 || bearing > 180)
                bearing -= Math.signum(bearing) * 360;
            return bearing;
        }

        public Player2D setBearing(double bearing) {
            this.bearing = bearing;
            bearing();
            return this;
        }

        public Player2D setBearing(Player2D other) {
            return this.setBearing(other, 0);
        }

        public Player2D setBearing(Player2D other, double offset) {
            this.bearing = this.getAbsBearing(other) + offset;
            return this;
        }

        public double getAbsBearing(Grid2D.GridObject2D other) {
            double x = other.location().x - this.location().x;
            double y = other.location().y - this.location().y;
            double degrees = Math.atan2(y, x);

            //return adjustBearing(180 - (90*Math.signum(x)) - (degrees*Math.signum(x)));
            return Math.signum(x) * (90 - degrees);
        }

        public double getRelBearing(Grid2D.GridObject2D other) {
            return getAbsBearing(other) - bearing();
        }

        public boolean canSee(Grid2D.GridObject2D other) {
            return this.distance(other) < SIGHT_DISTANCE &&
                    Math.abs(this.getRelBearing(other)) < SIGHT_BEARING;
        }

    }
    class Weapon2D extends Grid2D.GridObject2D {
        private int str = 40;

        public int getStr() {
            return str;
        }

        public Weapon2D(double x, double y) {
            super(x, y);
        }

        public Weapon2D(String name, int str, double x, double y) {
            this(str, x, y);
            this.name = name;
        }

        public Weapon2D(int str, double x, double y) {
            this(x, y);
            this.str = str;
        }


        @Override
        public double bearing() {
            return 0;
        }
    }

    private static abstract class Spell {
        String name;
        double effRange;
        double maxRange;
        double succRate;

        public static ArrayList<Spell> spellList = new ArrayList<>();

        Spell() {
            this.name = "Unnamed Spell - No effect";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
            spellList.add(this);
        }

        public boolean cast(Grid2D.GridObject2D caster, Grid2D.GridObject2D target) {
            return castSpell(caster, target, this);
        }
    }
    class Fire extends Spell {
        Fire() {
            super();
            this.name = "Fire";
            this.effRange = 20;
            this.maxRange = 25;
            this.succRate = .75;
        }
    }
    class Buff extends Spell {
        Buff() {
            super();
            this.name = "Buff";
            this.effRange = 100;
            this.maxRange = 1000;
            this.succRate = 1;
        }
    }
    class Fail extends Spell {
        Fail() {
            super();
            this.name = "FAIL";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
        }
    }

    public static void main(String args[]) {
        Grid2DTest grid2DTest = new Grid2DTest();

        Player2D player1 = grid2DTest.new Player2D("Anne", 20.1, 20.1);
        grid2DTest.new Player2D("Bob", 30, 38).setBearing(player1);
        grid2DTest.new Player2D("Charlie", 24, 22);
        grid2DTest.new Weapon2D("Ultima Weapon", 40, 20, 21);

        grid2DTest.new Fire();
        grid2DTest.new Buff();
        grid2DTest.new Fail();

        for (Grid2D.GridObject2D go : Grid2D.gridObject2DList)
            System.out.println(go.name() + " - " + go.getClass().getSimpleName());
        System.out.println();

        Iterator<Grid2D.GridObject2D> go2dFirstIterator = Grid2D.gridObject2DList.iterator();
        while (go2dFirstIterator.hasNext()) {

            Grid2D.GridObject2D go2dFirst = go2dFirstIterator.next();
            System.out.println("Grid2D capabilities of " + go2dFirst.name());

            Iterator<Grid2D.GridObject2D> go2dSecondIterator = Grid2D.gridObject2DList.iterator();
            while (go2dSecondIterator.hasNext()) {

                Grid2D.GridObject2D go2dSecond = go2dSecondIterator.next();

                if (go2dFirst == go2dSecond) continue;

                System.out.println(String.format(Locale.getDefault(), "%s is a distance of %.2f from %s",
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
                } catch (Exception e) {
                    System.out.println(go2dFirst.name() + " cannot get a bearing on " +
                            go2dSecond.name());
                }

                if (go2dFirst.canSee(go2dSecond)) {
                    System.out.println(go2dFirst.name() + " can see " + go2dSecond.name());

                    for (Spell spell : Spell.spellList) {
                        try {
                            if (spell.cast(go2dFirst, go2dSecond))
                                System.out.println("DAI HUGE SUCCESS!");
                            else System.out.println("Fail.");
                        } catch (Exception e) {
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
