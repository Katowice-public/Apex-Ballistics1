package com.apexballistics.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Shared facing for every missile model (nose is +Z).
 * Yaw matches Minecraft / {@code Direction#toYRot()}: 0 = south (+Z), 90 = west.
 * Pitch is nose elevation: 0 = level, +90 = straight up, -90 = straight down.
 *
 * AbstractHurtingProjectile snaps vanilla arrow rotation inside {@code super.tick()}.
 * Call {@link #restoreAndSmooth} after that so we keep this convention.
 */
public final class MissileOrientation {
    private MissileOrientation() {
    }

    public static Vec3 visualMotion(Entity entity) {
        if (entity.level().isClientSide && entity.tickCount > 2) {
            Vec3 stepped = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
            if (stepped.lengthSqr() > 1.0E-5D) {
                return stepped;
            }
        }
        return entity.getDeltaMovement();
    }

    public static float yawFromMotion(Vec3 motion, float fallbackYaw) {
        if (motion.horizontalDistance() < 0.04D) {
            return fallbackYaw;
        }
        return (float) (Mth.atan2(-motion.x, motion.z) * (180.0D / Math.PI));
    }

    public static float pitchFromMotion(Vec3 motion) {
        double horiz = motion.horizontalDistance();
        return (float) (Mth.atan2(motion.y, Math.max(horiz, 1.0E-4D)) * (180.0D / Math.PI));
    }

    public static void faceVelocity(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-8D) {
            return;
        }
        entity.setYRot(yawFromMotion(motion, entity.getYRot()));
        entity.setXRot(Mth.clamp(pitchFromMotion(motion), -90.0F, 90.0F));
        if (entity.tickCount <= 1) {
            entity.yRotO = entity.getYRot();
            entity.xRotO = entity.getXRot();
        }
    }

    /**
     * Put rotation back to what it was before {@code super.tick()}, then turn
     * toward velocity at {@code maxDegrees} per tick.
     */
    public static void restoreAndSmooth(Entity entity, float yawBefore, float pitchBefore,
                                       Vec3 motion, float maxDegrees) {
        entity.setYRot(yawBefore);
        entity.setXRot(pitchBefore);
        entity.yRotO = yawBefore;
        entity.xRotO = pitchBefore;
        if (motion.lengthSqr() < 1.0E-8D) {
            return;
        }
        float targetYaw = yawFromMotion(motion, yawBefore);
        float targetPitch = Mth.clamp(pitchFromMotion(motion), -90.0F, 90.0F);
        entity.setYRot(moveTowardsAngle(yawBefore, targetYaw, maxDegrees));
        entity.setXRot(Mth.clamp(moveTowardsAngle(pitchBefore, targetPitch, maxDegrees), -90.0F, 90.0F));
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
}
