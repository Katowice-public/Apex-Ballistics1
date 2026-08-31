package com.apexballistics.entity;

import com.apexballistics.ApexBallistics;
import net.minecraft.resources.ResourceLocation;

public enum WarheadType {
    HE("he_missile", 4.0F, false, false),
    INCENDIARY("incendiary_missile", 2.6F, true, false),
    CLUSTER("cluster_missile", 2.2F, false, false),
    HOMING("homing_missile", 3.6F, false, true),
    BUNKER("bunker_missile", 5.5F, false, false),
    BOMBLET("bomblet", 1.7F, false, false);

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
