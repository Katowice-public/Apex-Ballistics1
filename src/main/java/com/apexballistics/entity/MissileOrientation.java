package com.apexballistics.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MissileOrientation {
    private MissileOrientation() {
    }

    /**
     * Point the nose along velocity. Yaw 0 = +Z.
     * {@code arrowStyle} uses vanilla projectile pitch (up = -90) for the small
     * missiles; cruise missiles use nose-style pitch (up = +90).
     */
    public static void faceVelocity(Entity entity, boolean arrowStyle) {
        faceDirection(entity, entity.getDeltaMovement(), arrowStyle);
    }

    public static void faceVelocity(Entity entity) {
        faceDirection(entity, entity.getDeltaMovement(), false);
    }

    public static void faceDirection(Entity entity, Vec3 motion, boolean arrowStyle) {
        float[] target = yawPitch(entity, motion, arrowStyle);
        if (target == null) {
            return;
        }
        entity.setYRot(target[0]);
        entity.setXRot(target[1]);
        if (entity.tickCount <= 1) {
            entity.yRotO = target[0];
            entity.xRotO = target[1];
        }
    }

    public static void faceDirection(Entity entity, Vec3 motion) {
        faceDirection(entity, motion, false);
    }

    /**
     * Turn at most {@code maxDegrees} toward the velocity heading. Nearly vertical
     * paths keep the previous yaw so a launcher missile pitches instead of snapping south.
     */
    public static void smoothTowardsMotion(Entity entity, Vec3 motion, float maxDegrees, boolean arrowStyle) {
        float[] target = yawPitch(entity, motion, arrowStyle);
        if (target == null) {
            return;
        }
        entity.setYRot(moveTowardsAngle(entity.getYRot(), target[0], maxDegrees));
        entity.setXRot(Mth.clamp(moveTowardsAngle(entity.getXRot(), target[1], maxDegrees), -90.0F, 90.0F));
    }

    public static Vec3 rotateToward(Vec3 from, Vec3 to, float maxDegrees) {
        if (from.lengthSqr() < 1.0E-8D) {
            return to.normalize();
        }
        if (to.lengthSqr() < 1.0E-8D) {
            return from.normalize();
        }
        Vec3 a = from.normalize();
        Vec3 b = to.normalize();
        double dot = Mth.clamp(a.dot(b), -1.0D, 1.0D);
        double angle = Math.acos(dot);
        double max = Math.toRadians(maxDegrees);
        if (angle <= max || angle < 1.0E-5D) {
            return b;
        }
        double t = max / angle;
        return a.scale(1.0D - t).add(b.scale(t)).normalize();
    }

    public static float moveTowardsAngle(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxDelta) {
            delta = maxDelta;
        } else if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }

    private static float[] yawPitch(Entity entity, Vec3 motion, boolean arrowStyle) {
        if (motion.lengthSqr() < 1.0E-8D) {
            return null;
        }
        double horiz = motion.horizontalDistance();
        float yaw = entity.getYRot();
        if (horiz > 0.05D) {
            yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
        }
        float pitch = (float) (Mth.atan2(motion.y, Math.max(horiz, 1.0E-4D)) * (180.0D / Math.PI));
        if (arrowStyle) {
            pitch = -pitch;
        }
        return new float[]{yaw, pitch};
    }
}
