package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.block.CruiseLauncherBlock;
import com.apexballistics.blockentity.CruiseLauncherBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CruiseLauncherRenderer implements BlockEntityRenderer<CruiseLauncherBlockEntity> {
    private final CruiseMissileModel model;

    public CruiseLauncherRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CruiseMissileModel(context.bakeLayer(CruiseMissileModel.LAYER));
    }

    @Override
    public void render(CruiseLauncherBlockEntity launcher, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!launcher.hasMissile() || launcher.getBlockState().getValue(CruiseLauncherBlock.PART) != BedPart.FOOT) {
            return;
        }
        Direction facing = launcher.getBlockState().getValue(CruiseLauncherBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5D + facing.getStepX() * 0.5D, 1.0D, 0.5D + facing.getStepZ() * 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        ResourceLocation texture = ApexBallistics.id("textures/entity/cruise_missile.png");
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(texture));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
