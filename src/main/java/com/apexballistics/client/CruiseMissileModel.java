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
        // Nose points +Z. About 3.5 blocks long, 0.75 thick, with fins.
        part.addOrReplaceChild("body_fore",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 12.0F, 16.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("body_aft",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -16.0F, 12.0F, 12.0F, 16.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(0, 44).addBox(-4.0F, -4.0F, 16.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("tip",
                CubeListBuilder.create()
                        .texOffs(32, 44).addBox(-2.0F, -2.0F, 24.0F, 4.0F, 4.0F, 6.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(0, 44).addBox(-5.0F, -5.0F, -22.0F, 10.0F, 10.0F, 6.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("nozzle",
                CubeListBuilder.create()
                        .texOffs(48, 44).addBox(-3.5F, -3.5F, -27.0F, 7.0F, 7.0F, 5.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("fin_v",
                CubeListBuilder.create()
                        .texOffs(0, 28).addBox(-0.5F, -10.0F, -20.0F, 1.0F, 20.0F, 8.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("fin_h",
                CubeListBuilder.create()
                        .texOffs(18, 28).addBox(-10.0F, -0.5F, -20.0F, 20.0F, 1.0F, 8.0F),
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
