package com.apexballistics.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MissileOrientation {
    private MissileOrientation() {
    }

    /**
     * Point the nose along velocity. Yaw 0 = +Z, pitch positive = nose up.
     * Nearly vertical paths keep the previous yaw so a launcher missile pitches
     * up instead of snapping to south.
     */
    public static void faceVelocity(Entity entity) {
        faceDirection(entity, entity.getDeltaMovement());
    }

    public static void faceDirection(Entity entity, Vec3 motion) {
        if (motion.lengthSqr() < 1.0E-8D) {
            return;
        }
        double horiz = motion.horizontalDistance();
        float yaw = entity.getYRot();
        if (horiz > 0.05D) {
            yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
        }
        float pitch = (float) (Mth.atan2(motion.y, horiz) * (180.0D / Math.PI));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity.tickCount <= 1) {
            entity.yRotO = yaw;
            entity.xRotO = pitch;
        }
    }
}
