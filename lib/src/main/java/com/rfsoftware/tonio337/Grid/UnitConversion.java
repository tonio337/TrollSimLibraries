package com.rfsoftware.tonio337.grid;

/**
 * Created by adlee on 1/11/2017.
 */
public class UnitConversion {

    private static final double METERS_TO_FEET = 3.28084;

    enum DistanceUnit {
        METER(1.0),
        KILOMETER(.001),
        MILLIMETER(1000),
        FOOT(METERS_TO_FEET),
        INCH(METERS_TO_FEET * 12),
        YARD(METERS_TO_FEET / 3),
        MILE(METERS_TO_FEET / 5280);


        private final double baseDistance; // stored in meters

        DistanceUnit(double baseDistance) {
            this.baseDistance = baseDistance;
        }

        private double baseDistance() {
            return baseDistance;
        }

        public double convertTo(DistanceUnit other) {
            return this.baseDistance() * other.baseDistance();
        }

        public static double convert(DistanceUnit first, DistanceUnit second) {
            return first.convertTo(second);
        }
    }
}
