package com.apexballistics.client;

import com.apexballistics.entity.MissileEntity;
import com.apexballistics.entity.WarheadType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

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
        MissileRenderUtil.orientNoseAlongMotion(poseStack, entity, partialTicks);
        poseStack.scale(1.7F, 1.7F, 1.7F);
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
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
