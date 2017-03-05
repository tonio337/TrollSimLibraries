package com.rfsoftware.tonio337.grid;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Locale;

import java.awt.Graphics2D.*;
import java.util.concurrent.TimeUnit;

import com.rfsoftware.tonio337.grid.Grid2D.Object2D;
import com.rfsoftware.tonio337.grid.Player2D.Bearing;
import com.rfsoftware.tonio337.grid.Player2D.Bearing.Direction;
import com.rfsoftware.tonio337.grid.Player2D.Bearing.Orientation;

/**
 * Project: TrollSimInterface
 * Package: ${PACKAGE_NAME}
 * Created by adlee on 1/11/2017.
 */
public class Grid2DTest{

    private static final boolean DEBUG = false;

    private static Grid2D testGrid = new Grid2D();
    private static ArrayList<Object2D> testGridObjectList;
    private static abstract class Spell {
        String name;
        double effRange;
        double maxRange;
        double succRate;

        static ArrayList<Spell> spellList = new ArrayList<>();

        Spell() {
            this.name = "Unnamed Spell - No effect";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
            spellList.add(this);
        }

        boolean cast(Player2D caster, Object2D target) {
            if (DEBUG)
                System.out.println(castHeader(caster,target));

            // auto-fail if either effective range or success rate is not positive
            if (effRange <= 0 || succRate <= 0) return false;

            // auto-success if within effective range
            double dist = caster.distance(target);
            if (dist < effRange) return true;

            // percentage success dependent on pathDistance from max range
            // auto-fail if outside max range
            double sprdPer = spreadPercentage(caster,target);
            if (DEBUG)
                System.out.println(castFooter(sprdPer));
            return sprdPer > succRate;
        }

        String castHeader(Player2D caster, Object2D target){
            return caster.name() + " casts " + name + " on " + target.name() +
                    " with effective range of " + effRange + "...";
        }
        String castFooter(double spreadPercentage){
            return String.format(Locale.getDefault(),"Spell percentage: %1.2f Spread percentage: %2.2f",
                    succRate, spreadPercentage);
        }

        double spreadPercentage(Object2D first, Object2D second) {
            double dist = first.distance(second);
            return (dist - maxRange) / (effRange - maxRange);
        }
    }

    static{
        //testGrid = new Grid2D();
        testGridObjectList = testGrid.gridObject2DList;
        new Player2D("Anne", 20.1, 20.1,testGrid);
        new Player2D("Bob", 30, 38,testGrid);
        new Player2D("Charlie", 24, 22,testGrid);
        new Weapon2D("Ultima Weapon", 40, 20, 21, testGrid);
    }

    private class Fire extends Spell {
        Fire() {
            super();
            this.name = "Fire";
            this.effRange = 20;
            this.maxRange = 25;
            this.succRate = .75;
        }
    }
    private class Buff extends Spell {
        Buff() {
            super();
            this.name = "Buff";
            this.effRange = 100;
            this.maxRange = 1000;
            this.succRate = 1;
        }
    }
    private class Fail extends Spell {
        Fail() {
            super();
            this.name = "FAIL";
            this.effRange = 0;
            this.maxRange = 0;
            this.succRate = 0;
        }
    }

    public Grid2DTest(){
        new Fire();
        new Buff();
        new Fail();
    }

    public static void main(String[] args){
        Grid2DTest suite = new Grid2DTest();

        //suite.castSpellsTest();
        //suite.addObjectsTest();
        //suite.locationToStringTest();
    }

    @Test
    public void castSpellsTest(){
        for (Object2D caster : testGridObjectList)
            for (Object2D target : testGridObjectList)
                // cast if the caster isn't targeting itself and if the caster is a player
                //if ((caster != target) && (caster.getClass() == Player2D.class))
                if ((caster != target) && (caster instanceof Player2D))
                    castSpellsAssertion((Player2D) caster, target);
    }

    private void castSpellsAssertion(Player2D caster, Object2D target) {

        for (Spell spell : Spell.spellList){
            if (spell.cast(caster, target)){
                assertEquals(caster.getClass(),Player2D.class);
                assertTrue(caster.distance(target) < spell.maxRange);
            }
        }
    }

    @Test
    public void addObjectsTest()
    {
        // SCOPE: this testGrid is different from main one
        Grid2D otherTestGrid = new Grid2D();

        // Created a test player in a different grid
        Player2D testPlayer = new Player2D("Coolio",42,42,new Grid2D());
        assertFalse("Created in different grid, should not exist in this one.",otherTestGrid.gridObject2DList
                .contains(testPlayer));

        otherTestGrid.add(testPlayer);
        assertTrue("Created in this grid, should exist.",otherTestGrid.gridObject2DList
                .contains(testPlayer));

        otherTestGrid.add(testPlayer);
        otherTestGrid.remove(testPlayer);
        assertFalse("Should be properly removed from grid now.", otherTestGrid.gridObject2DList
                .contains(testPlayer));
    }

    @Test
    public void locationToStringTest() {
        Grid2D testGrid = new Grid2D();
        Player2D testPlayer = new Player2D("Test",2.2423456,4.421455125,testGrid);
        String locationString = testPlayer.location().toString();

        assertThat(locationString,equalTo("Location: (2.24,4.42)"));
    }

    @Test
    public void regionsTest() {
        Grid2D testGrid;

        testGrid = new Grid2D(0,100,0,100,5,5);
        assertThat(testGrid.numXRegions(),equalTo(20+1));
        assertThat(testGrid.numYRegions(),equalTo(20+1));
        assertThat(testGrid.numRegions(),equalTo(400+20+20+1));

        assertThat(testGrid.getXCoord(2),equalTo(5.0));
        assertThat(testGrid.getYCoord(7),equalTo(30.0));
        assertThat(testGrid.getCoords(2,7).x,equalTo(5.0));
        assertThat(testGrid.getCoords(2,7).y,equalTo(30.0));

        Object2D testPlayer = new Player2D(testGrid);

        testPlayer.setLocation(0,0);
        assertThat(testGrid.xRegion(testPlayer.location()),equalTo(1));
        assertThat(testGrid.yRegion(testPlayer.location()),equalTo(1));
        assertThat(testGrid.isXBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.isYBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(1));

        testPlayer.setLocation(100,100);
        assertThat(testGrid.xRegion(testPlayer.location()),equalTo(21));
        assertThat(testGrid.yRegion(testPlayer.location()),equalTo(21));
        assertThat(testGrid.isXBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.isYBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(441));

        testPlayer.setLocation(99.9,99.9);
        assertThat(testGrid.xRegion(testPlayer.location()),equalTo(20));
        assertThat(testGrid.yRegion(testPlayer.location()),equalTo(20));
        assertThat(testGrid.isXBorder(testPlayer.location()),equalTo(false));
        assertThat(testGrid.isYBorder(testPlayer.location()),equalTo(false));
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(419));

        testPlayer.setLocation(0,105);
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(0));

        testPlayer.setLocation(-.2,50);
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(0));

        testGrid = new Grid2D(4,20,6,38,4,6);
        assertThat(testGrid.numXRegions(),equalTo(4+1));
        assertThat(testGrid.numYRegions(),equalTo(5+1));
        assertThat(testGrid.numRegions(),equalTo(30));

        assertThat(testGrid.getXCoord(5),equalTo(20.0));
        assertThat(testGrid.getYCoord(6),equalTo(36.0));
        assertThat(testGrid.getCoords(5,6).x,equalTo(20.0));
        assertThat(testGrid.getCoords(5,6).y,equalTo(36.0));

        testGrid.add(testPlayer);

        testPlayer.setLocation(4,6);
        assertThat(testGrid.xRegion(testPlayer.location()),equalTo(1));
        assertThat(testGrid.yRegion(testPlayer.location()),equalTo(1));
        assertThat(testGrid.isXBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.isYBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(1));

        testPlayer.setLocation(20,37);
        assertThat(testGrid.xRegion(testPlayer.location()),equalTo(5));
        assertThat(testGrid.yRegion(testPlayer.location()),equalTo(6));
        assertThat(testGrid.isXBorder(testPlayer.location()),equalTo(true));
        assertThat(testGrid.isYBorder(testPlayer.location()),equalTo(false));
        assertThat(testGrid.regionID(testPlayer.location()),equalTo(30));
    }

    @Test
    public void bearingTest() {

        Grid2D grid = new Grid2D();
        Player2D p1 = new Player2D("P1",2,2,grid);
        Player2D p2 = new Player2D("P2",4,4,grid);

        //assertThat(p1.getBearing(),is(0.0));
        //assertThat(p2.getBearing(),is(0.0));

        // Set Bearing Test

        p1.setMyBearingTo(p2);
        assertThat(p1.getBearing(),is(45.0));
        p2.setMyBearingTo(p1);
        assertThat(p2.getBearing(),is(225.0));

        // Relative Bearing Test

        p1.setMyBearingTo(p2);
        assertThat(p1.getRelativeBearingTo(p2),is(0.0));
        p1.setMyBearingTo(360+45);
        assertThat(p1.getRelativeBearingTo(p2),is(0.0));
        p1.setMyBearingTo(45-2);
        assertThat(p1.getRelativeBearingTo(p2),is(2.0));
        p1.setMyBearingTo(45+2);
        assertThat(p1.getRelativeBearingTo(p2),is(-2.0));

        //Reverse Bearing Test

        p1.setLocation(0,0);
        p2.setLocation(20,40);
        p1.setMyBearingTo(p2);
        double p1top2Ratio = (p2.location().y-p1.location().y)/(p2.location().x-p1.location().x);
        double p1top2RatioTest = p1.getBearingY()/p1.getBearingX();

        assertEquals(p1top2Ratio,p1top2RatioTest,.1);

    }

    @Test
    public void drawGrid(){
        drawGrid(testGrid);
    }

    public void drawGrid(Grid2D grid) {
        // These two grids should effectively be the same.
        try {


            GridApp.main(new String[]{});
            GridApp gridApp = GridApp.getCurrentApp();
            //gridApp.setGrid(testGrid);

            GridApp.main(new String[]{});
            GridApp gridApp2 = GridApp.getCurrentApp();

            //TODO: Figure out how to view instantiated JPanel objects.
            TimeUnit.SECONDS.sleep(10);
        }

        catch(InterruptedException e) {
            return;
        }
    }

    @Test
    public void bearingClassTest(){

        Bearing unitCircleZero = new Bearing(0.0, Direction.RIGHT, Orientation.COUNTERCLOCKWISE);
        assertThat(unitCircleZero.getDegrees(),is(0.0));

        Bearing unitCirclePi = new Bearing(180.0, Direction.RIGHT, Orientation.COUNTERCLOCKWISE);
        assertThat(unitCirclePi.getDegrees(),is(180.0));

        Bearing unitCircleOnePtFivePi = new Bearing(-90-360, Direction.RIGHT, Orientation.COUNTERCLOCKWISE);
        assertThat(unitCircleOnePtFivePi.getDegrees(),is(270.0));
        unitCircleOnePtFivePi.setBearing(270+360);
        assertThat(unitCircleOnePtFivePi.getDegrees(),is(270.0));

        double gridNorthDegrees = unitCircleZero.translate(Direction.UP, Orientation.CLOCKWISE);
        assertThat(gridNorthDegrees,is(90.0));
        double gridSouthDegrees = unitCircleZero.translate(Direction.DOWN, Orientation.CLOCKWISE);
        assertThat(gridSouthDegrees,is(270.0));
    }

}

