package com.rfsoftware.tonio337.dice;

/**
 * Created by adlee on 12/23/2016.
 */

public class Dice {

    private static final int MIN_SIDES_MINIMUM = 4;
    private static int minSides = 4;

    public static int getMinSides() { return minSides; }

    public static void setMinSides(int newMinSides) {
        minSides = Math.max(newMinSides, MIN_SIDES_MINIMUM);
    }

    private int numSides;

    public int getNumSides() { return numSides; }

    public static int checkNumSides(int numSides) {
        return Math.max(minSides, numSides);
    }

    public void setNumSides(int numSides) {
        this.numSides = checkNumSides(numSides);
    }

    public Dice(){
        this(minSides);
    }

    public Dice(int numSides){
        setNumSides(numSides);
    }

    public int roll() {
        return roll(numSides);
    }

    public static int roll(int numSided){
        if (numSided <= 1) return 1;
        return  ((int)(Math.random()*numSided)) + 1;
    }

    public static int roll(int numSided, int numDice){
        if (numDice < 1) return 0;

        int sum = 0;

        for (int d = 0; d < numDice; d++)
            sum += roll(numSided);
        return sum;
    }

    @Override
    public String toString() {
        return "D" + numSides + " - " + numSides + " sided die";
    }
}
