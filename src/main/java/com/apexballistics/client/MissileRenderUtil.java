package com.apexballistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MissileRenderUtil {
    private MissileRenderUtil() {
    }

    public static void orientNoseAlongMotion(PoseStack poseStack, Entity entity, float partialTicks) {
        Vec3 motion = entity.getDeltaMovement();
        float yaw;
        float pitch;
        if (motion.lengthSqr() > 1.0E-6D) {
            yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
            pitch = (float) (Mth.atan2(motion.y, motion.horizontalDistance()) * (180.0D / Math.PI));
        } else {
            yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
            pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }
}
