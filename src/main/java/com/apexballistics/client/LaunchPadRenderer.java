package com.apexballistics.client;

import com.apexballistics.block.LaunchPadBlock;
import com.apexballistics.blockentity.LaunchPadBlockEntity;
import com.apexballistics.entity.WarheadType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

public class LaunchPadRenderer implements BlockEntityRenderer<LaunchPadBlockEntity> {
    private final MissileModel model;

    public LaunchPadRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MissileModel(context.bakeLayer(MissileModel.LAYER));
    }

    @Override
    public void render(LaunchPadBlockEntity pad, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        WarheadType warhead = pad.getWarhead();
        if (warhead == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.95D, 0.5D);
        Direction facing = pad.getBlockState().getValue(LaunchPadBlock.FACING);
        // Same arrow-style stack the in-flight renderer uses when going straight up.
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot() - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(1.55F, 1.55F, 1.55F);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(MissileRenderer.textureFor(warhead)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
