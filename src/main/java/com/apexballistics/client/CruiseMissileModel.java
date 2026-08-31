package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.CruiseMissileEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class CruiseMissileModel extends HierarchicalModel<CruiseMissileEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ApexBallistics.id("cruise_missile"), "main");

    private final ModelPart root;

    public CruiseMissileModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        // 1 block wide/long, 3 blocks tall (16 x 48 x 16)
        part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 48.0F, 16.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(CruiseMissileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
