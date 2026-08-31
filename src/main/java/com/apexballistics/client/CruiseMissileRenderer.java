package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.CruiseMissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CruiseMissileRenderer extends EntityRenderer<CruiseMissileEntity> {
    private static final ResourceLocation TEXTURE = ApexBallistics.id("textures/entity/cruise_missile.png");
    private final CruiseMissileModel model;

    public CruiseMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CruiseMissileModel(context.bakeLayer(CruiseMissileModel.LAYER));
        this.shadowRadius = 1.1F;
    }

    @Override
    public void render(CruiseMissileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        MissileRenderUtil.orientNoseAlongMotion(poseStack, entity, partialTicks);
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CruiseMissileEntity entity) {
        return TEXTURE;
    }
}
