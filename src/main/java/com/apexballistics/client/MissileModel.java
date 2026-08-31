package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
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
        // Nose points +Z. Roughly 1.5 blocks long after renderer scale.
        part.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -3.0F, -10.0F, 6.0F, 6.0F, 20.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-2.0F, -2.0F, 10.0F, 4.0F, 4.0F, 6.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("tip",
                CubeListBuilder.create()
                        .texOffs(14, 26).addBox(-1.0F, -1.0F, 16.0F, 2.0F, 2.0F, 4.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("nozzle",
                CubeListBuilder.create()
                        .texOffs(22, 26).addBox(-2.5F, -2.5F, -14.0F, 5.0F, 5.0F, 4.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("fin_v",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-0.5F, -7.0F, -10.0F, 1.0F, 14.0F, 7.0F),
                PartPose.ZERO);
        part.addOrReplaceChild("fin_h",
                CubeListBuilder.create()
                        .texOffs(32, 21).addBox(-7.0F, -0.5F, -10.0F, 14.0F, 1.0F, 7.0F),
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
