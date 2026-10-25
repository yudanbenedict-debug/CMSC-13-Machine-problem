package com.mygame.engine.helpers.algorithms;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public final class TransformMethods {
    private TransformMethods() {
    }

    public static Point2D.Double polarToScreen(double centerX, double centerY,
                                               double radius, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        return new Point2D.Double(
                centerX + radius * Math.cos(radians),
                centerY - radius * Math.sin(radians));
    }

    public static double screenAngle(double centerX, double centerY,
                                     double pointX, double pointY) {
        return Math.toDegrees(Math.atan2(centerY - pointY, pointX - centerX));
    }

    public static double distance(double firstX, double firstY, double secondX, double secondY) {
        return Math.hypot(secondX - firstX, secondY - firstY);
    }

    public static double shortestAngleDifference(double target, double current) {
        double difference = target - current;
        return difference - 360 * Math.floor((difference + 180) / 360);
    }

    public static AffineTransform radialTransform(double x, double y,
                                                   double angleDegrees, double scale) {
        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(-Math.toRadians(angleDegrees));
        transform.scale(scale, scale);
        return transform;
    }
}
