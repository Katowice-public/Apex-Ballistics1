package com.apexballistics.entity;

import com.apexballistics.ApexBallistics;
import net.minecraft.resources.ResourceLocation;

public enum WarheadType {
    HE("he_missile", 6.5F, false, false),
    INCENDIARY("incendiary_missile", 6.5F, true, false),
    CLUSTER("cluster_missile", 6.5F, false, false),
    HOMING("homing_missile", 6.5F, false, true),
    BUNKER("bunker_missile", 6.5F, false, false),
    BOMBLET("bomblet", 2.2F, false, false);

    private final String itemId;
    private final float explosionPower;
    private final boolean ignites;
    private final boolean homing;

    WarheadType(String itemId, float explosionPower, boolean ignites, boolean homing) {
        this.itemId = itemId;
        this.explosionPower = explosionPower;
        this.ignites = ignites;
        this.homing = homing;
    }

    public String itemId() {
        return itemId;
    }

    public float explosionPower() {
        return explosionPower;
    }

    public boolean ignites() {
        return ignites;
    }

    public boolean homing() {
        return homing;
    }

    public ResourceLocation entityTexture() {
        return ApexBallistics.id("textures/entity/" + itemId + ".png");
    }

    public static WarheadType byId(int id) {
        WarheadType[] values = values();
        if (id < 0 || id >= values.length) {
            return HE;
        }
        return values[id];
    }
}
