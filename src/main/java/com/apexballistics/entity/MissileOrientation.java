package com.apexballistics.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MissileOrientation {
    private MissileOrientation() {
    }

    /**
     * Point the nose along velocity. Yaw 0 = +Z, pitch positive = nose up.
     * Call after {@code super.tick()} so it wins over fireball rotation.
     */
    public static void faceVelocity(Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-8D) {
            return;
        }
        float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
        float pitch = (float) (Mth.atan2(motion.y, motion.horizontalDistance()) * (180.0D / Math.PI));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        if (entity.tickCount <= 1) {
            entity.yRotO = yaw;
            entity.xRotO = pitch;
        }
    }
}
