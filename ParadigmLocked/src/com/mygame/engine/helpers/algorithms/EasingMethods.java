package com.mygame.engine.helpers.algorithms;

public final class EasingMethods {
    private EasingMethods() {
    }

    public static double approach(double current, double target, double speed, double deltaTime) {
        return current + (target - current) * easeOut(speed, deltaTime);
    }

    public static double approachAngle(double current, double target, double speed, double deltaTime) {
        return current + TransformMethods.shortestAngleDifference(target, current)
                * easeOut(speed, deltaTime);
    }

    public static double easeIn(double value) {
        return value * value;
    }

    public static double easeOut(double speed, double deltaTime) {
        return 1 - Math.exp(-speed * Math.max(0, deltaTime));
    }

    public static double smoothStep(double value) {
        double clamped = Math.max(0, Math.min(1, value));
        return clamped * clamped * (3 - 2 * clamped);
    }
}
