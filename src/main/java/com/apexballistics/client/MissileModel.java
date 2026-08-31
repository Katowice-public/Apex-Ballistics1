package com.apexballistics.client;

import com.apexballistics.entity.MissileEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import com.apexballistics.ApexBallistics;

public class MissileModel extends HierarchicalModel<MissileEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ApexBallistics.id("missile"), "main");

    private final ModelPart root;

    public MissileModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 4.0F, 16.0F)
                        .texOffs(0, 20).addBox(-1.0F, -1.0F, -11.0F, 2.0F, 2.0F, 3.0F)
                        .texOffs(24, 0).addBox(-3.5F, -0.5F, 4.0F, 7.0F, 1.0F, 4.0F)
                        .texOffs(24, 5).addBox(-0.5F, -3.5F, 4.0F, 1.0F, 7.0F, 4.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MissileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
