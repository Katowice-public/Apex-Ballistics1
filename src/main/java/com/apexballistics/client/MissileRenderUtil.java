package com.apexballistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class MissileRenderUtil {
    private MissileRenderUtil() {
    }

    /**
     * +Z-nose models. Yaw 0 = south, pitch + = nose up. Must match
     * {@link com.apexballistics.entity.MissileOrientation}.
     */
    public static void orientNoseAlongMotion(PoseStack poseStack, Entity entity, float partialTicks) {
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        applyNoseRotation(poseStack, yaw, pitch);
    }

    public static void orientNoseAlongMotion(PoseStack poseStack, Entity entity, float entityYaw, float partialTicks) {
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        applyNoseRotation(poseStack, entityYaw, pitch);
    }

    public static void applyNoseRotation(PoseStack poseStack, float yaw, float pitch) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }
}
