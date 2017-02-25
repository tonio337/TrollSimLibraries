package com.rfsoftware.tonio337.dice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DiceTest {

    /* Test Suite */
    @Test
    public void minSidesTest(){
        Dice d;

        d = new Dice();
        assertEquals(d.getNumSides(),Dice.getMinSides());

        d = new Dice(0);
        assertEquals(d.getNumSides(),Dice.getMinSides());

        Dice.setMinSides(6);
        assertEquals(Dice.getMinSides(),6);

        d.setNumSides(4);
        assertTrue(d.getNumSides() >= Dice.getMinSides());
        assertEquals(d.getNumSides(),6);

        int numSides = Dice.checkNumSides(randomNum(-100,100));
        assertTrue(numSides>=Dice.getMinSides());
    }

    @Test
    public void multiRollTestx100(){
        for (int i=0; i<100; i++)
            multiRollTest();
    }

    @Test
    public void withinRangeTestsx100(){
        for (int i=0;i<100;i++) {
            withinRangeTest();
            // TODO: Test static function
            withinRangeStaticTest();
        }
    }

    @Test
    public void inheritedClassTestsx100(){
        for (int i=0;i<100;i++) {
            inheritedClassTest();
            // TODO: Test static function
            inheritedClassStaticTest();
        }
    }

    /* Individual tests */
    @Test
    public void multiRollTest(){
        int numDie = randomNum(0,100);
        int numSides = randomNum(-100,100);

        int roll = Dice.roll(numSides,numDie);
        assertTrue(roll>= numDie && roll <= numDie*Dice.checkNumSides(numSides));
    }

    @Test
    public void withinRangeTest(){

        Dice d;

        // constructor test
        d = new Dice(randomNum(-100,100));
        withinRangeAssertion(d);

        // manual numSides change test
        d.setNumSides(randomNum(-100,100));
        withinRangeAssertion(d);
    }

    @Test
    public void withinRangeStaticTest(){
        int numSides = randomNum(-100,100);
        int roll = Dice.roll(numSides);
        assertTrue("Rolled " + roll + " with D" + numSides, roll >= 1 && roll <= Dice.checkNumSides(numSides));
    }

    @Test
    public void inheritedClassTest(){
        CS_Dice csd = new CS_Dice(randomNum(-100,100));
        int roll = csd.roll();

        csDiceRollAssertion(csd.getNumSides(),roll);
    }

    @Test
    public void inheritedClassStaticTest(){
        int numSides = randomNum(-100,100);
        int roll = CS_Dice.roll(numSides);

        csDiceRollAssertion(Dice.checkNumSides(numSides),roll);
    }

    /* Individual assertion statements */
    private void withinRangeAssertion(Dice d) {
        int roll = d.roll();
        assertTrue(roll >= 1 && roll <= d.getNumSides());
    }

    private void csDiceRollAssertion(int numSides, int roll) {
        numSides = Dice.checkNumSides(numSides);
        assertTrue("Number of sides: " + numSides +
                        " Rolled: " + roll,
                roll >= 0 && roll <= numSides-1);
    }

    /* Helper functions */
    private static int randomNum(int min, int max){

        // if the random value is too low, return the Dice-defined minimum of sides
        // return Math.max(numSides,Dice.getMinSides());

        // remove error-checking from test helper functions
        return ((int) (Math.random()*(max-min+1)+min));
    }

}

// Computer Science Dice starts counting at 0!
class CS_Dice extends Dice{
    CS_Dice(int thing) { super(thing); }
    public static int roll(int numSides) { return Dice.roll(numSides) - 1; }

    @Override
    public int roll() { return super.roll() - 1; }

}
