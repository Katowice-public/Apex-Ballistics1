package com.apexballistics.entity;

/**
 * Three handheld / launch-pad missile marks. Higher marks are longer, fly a taller
 * rainbow arc, and hit harder. Cruise missiles are a separate entity and ignore this.
 */
public enum MissileTier {
    T1("t1", 1, 1.00F, 1.00F, 64.0D, 52.0D, 1.00F),
    T2("t2", 2, 1.55F, 1.12F, 96.0D, 88.0D, 1.75F),
    T3("t3", 3, 2.35F, 1.28F, 136.0D, 128.0D, 2.60F);

    private final String id;
    private final int mark;
    private final float lengthScale;
    private final float thicknessScale;
    private final double range;
    private final double arcHeight;
    private final float powerMultiplier;

    MissileTier(String id, int mark, float lengthScale, float thicknessScale,
                double range, double arcHeight, float powerMultiplier) {
        this.id = id;
        this.mark = mark;
        this.lengthScale = lengthScale;
        this.thicknessScale = thicknessScale;
        this.range = range;
        this.arcHeight = arcHeight;
        this.powerMultiplier = powerMultiplier;
    }

    public String id() {
        return id;
    }

    public int mark() {
        return mark;
    }

    public float lengthScale() {
        return lengthScale;
    }

    public float thicknessScale() {
        return thicknessScale;
    }

    public double range() {
        return range;
    }

    public double arcHeight() {
        return arcHeight;
    }

    public float powerMultiplier() {
        return powerMultiplier;
    }

    public String roman() {
        return switch (this) {
            case T1 -> "I";
            case T2 -> "II";
            case T3 -> "III";
        };
    }

    public static MissileTier byId(int id) {
        MissileTier[] values = values();
        if (id < 0 || id >= values.length) {
            return T1;
        }
        return values[id];
    }
}
