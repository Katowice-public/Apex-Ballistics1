package com.apexballistics.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Rainbow / half-circle loft for regular missiles. A cubic Bezier that takes off
 * at about 45° (not a 90° silo boost), peaks at {@code height}, then dives onto
 * the impact point.
 */
public final class LoftedArc {
    private LoftedArc() {
    }

    public static Vec3 point(Vec3 start, Vec3 end, double height, double t) {
        t = Mth.clamp(t, 0.0D, 1.0D);
        Vec3 chord = new Vec3(end.x - start.x, 0.0D, end.z - start.z);
        double range = chord.length();
        Vec3 horiz = range < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : chord.scale(1.0D / range);

        // P1 is equally forward and up so the takeoff tangent is ~45°, never vertical.
        // P2 is over-weighted so the cubic actually peaks near {@code height}.
        Vec3 p0 = start;
        Vec3 p1 = start.add(horiz.scale(range * 0.24D)).add(0.0D, range * 0.24D, 0.0D);
        Vec3 p2 = start.add(horiz.scale(range * 0.50D)).add(0.0D, height * 2.08D, 0.0D);
        Vec3 p3 = end;
        return cubic(p0, p1, p2, p3, t);
    }

    public static int durationTicks(double range, double height) {
        double path = range * 1.15D + height * 0.85D;
        return Mth.clamp((int) Math.round(path / 0.48D), 70, 700);
    }

    private static Vec3 cubic(Vec3 a, Vec3 b, Vec3 c, Vec3 d, double t) {
        double u = 1.0D - t;
        double uu = u * u;
        double tt = t * t;
        return a.scale(uu * u)
                .add(b.scale(3.0D * uu * t))
                .add(c.scale(3.0D * u * tt))
                .add(d.scale(tt * t));
    }
}
