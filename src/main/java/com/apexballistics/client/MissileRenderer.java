package com.apexballistics.client;

import com.apexballistics.entity.MissileEntity;
import com.apexballistics.entity.WarheadType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MissileRenderer extends EntityRenderer<MissileEntity> {
    private final MissileModel model;

    public MissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MissileModel(context.bakeLayer(MissileModel.LAYER));
        this.shadowRadius = 0.55F;
    }

    @Override
    public void render(MissileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
        this.orientAlongTravel(poseStack, entity, partialTicks);
        poseStack.scale(1.7F, 1.7F, 1.7F);
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Face the nose along the interpolated travel path. Regular missiles were rotating
     * around their feet and using fireball acceleration as "motion", which made them
     * fly sideways. Cruise missiles keep the shared util and are left unchanged.
     */
    private void orientAlongTravel(PoseStack poseStack, MissileEntity entity, float partialTicks) {
        double dx = Mth.lerp(partialTicks, entity.xo, entity.getX()) - entity.xo;
        double dy = Mth.lerp(partialTicks, entity.yo, entity.getY()) - entity.yo;
        double dz = Mth.lerp(partialTicks, entity.zo, entity.getZ()) - entity.zo;
        Vec3 travel = new Vec3(dx, dy, dz);
        if (travel.lengthSqr() < 1.0E-6D) {
            travel = entity.getDeltaMovement();
        }
        float yaw;
        float pitch;
        if (travel.lengthSqr() > 1.0E-8D) {
            yaw = (float) (Mth.atan2(travel.x, travel.z) * (180.0D / Math.PI));
            pitch = (float) (Mth.atan2(travel.y, travel.horizontalDistance()) * (180.0D / Math.PI));
        } else {
            yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
            pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
    }

    @Override
    public ResourceLocation getTextureLocation(MissileEntity entity) {
        return textureFor(entity.getWarhead());
    }

    public static ResourceLocation textureFor(WarheadType warhead) {
        if (warhead == null) {
            return WarheadType.HE.entityTexture();
        }
        return warhead.entityTexture();
    }
}
