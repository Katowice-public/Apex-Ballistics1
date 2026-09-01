package com.apexballistics.entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BallisticFlight {
    public enum Phase {
        BOOST,
        CRUISE,
        DIVE
    }

    private BallisticFlight() {
    }

    public static double cruiseAltitude(Level level, double startY, double targetY, double bonus) {
        double desired = Math.max(startY, targetY) + bonus;
        double floor = startY + 32.0D;
        double cap = level.getMaxBuildHeight() - 8.0D;
        return Math.min(cap, Math.max(floor, desired));
    }

    public static double diveRange(Vec3 position, Vec3 target) {
        double drop = Math.max(8.0D, position.y - target.y);
        return Math.max(24.0D, drop * 0.45D);
    }

    public static Vec3 desiredDirection(Phase phase, Vec3 position, Vec3 target, double cruiseY) {
        return switch (phase) {
            case BOOST -> new Vec3(0.0D, 1.0D, 0.0D);
            case CRUISE -> {
                Vec3 cruisePoint = new Vec3(target.x, cruiseY, target.z);
                Vec3 to = cruisePoint.subtract(position);
                yield to.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : to.normalize();
            }
            case DIVE -> {
                Vec3 to = target.subtract(position);
                yield to.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, -1.0D, 0.0D) : to.normalize();
            }
        };
    }

    public static Phase nextPhase(Phase phase, Vec3 position, Vec3 target, double cruiseY) {
        double horiz = Math.hypot(target.x - position.x, target.z - position.z);
        double diveAt = diveRange(position, target);
        return switch (phase) {
            case BOOST -> position.y >= cruiseY - 1.5D
                    ? (horiz <= diveAt ? Phase.DIVE : Phase.CRUISE)
                    : Phase.BOOST;
            case CRUISE -> horiz <= diveAt ? Phase.DIVE : Phase.CRUISE;
            case DIVE -> Phase.DIVE;
        };
    }

    public static double speed(Phase phase) {
        return switch (phase) {
            case BOOST -> 1.45D;
            case CRUISE -> 2.05D;
            case DIVE -> 2.45D;
        };
    }
}
