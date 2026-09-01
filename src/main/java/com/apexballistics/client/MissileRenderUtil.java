package com.apexballistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class MissileRenderUtil {
    private MissileRenderUtil() {
    }

    /**
     * +Z-nose models (cruise missile). Uses the entity's smoothed yaw/pitch so the
     * nose eases from the rail pose into the climb instead of snapping to velocity.
     */
    public static void orientNoseAlongMotion(PoseStack poseStack, Entity entity, float partialTicks) {
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }

    /**
     * Small missiles share AbstractHurtingProjectile's arrow-style rotation
     * ({@code yRot-90} + Z pitch). The extra Y -90 turns our +Z nose into +X.
     */
    public static void orientArrowStyle(PoseStack poseStack, Entity entity, float entityYaw, float partialTicks) {
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
    }
}
