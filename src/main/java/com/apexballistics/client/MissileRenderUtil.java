package com.apexballistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MissileRenderUtil {
    private MissileRenderUtil() {
    }

    /**
     * +Z-nose models (cruise missile). When the path is nearly vertical, keep the
     * existing yaw so a rail/pad launch pitches up instead of snapping sideways.
     */
    public static void orientNoseAlongMotion(PoseStack poseStack, Entity entity, float partialTicks) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < 1.0E-6D) {
            motion = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        }
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch;
        if (motion.lengthSqr() > 1.0E-6D) {
            double horiz = motion.horizontalDistance();
            if (horiz > 0.05D) {
                yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
            }
            pitch = (float) (Mth.atan2(motion.y, horiz) * (180.0D / Math.PI));
        } else {
            pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        }
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
