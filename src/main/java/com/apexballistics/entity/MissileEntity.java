package com.apexballistics.entity;

import com.apexballistics.ApexConfig;
import com.apexballistics.registry.ModEntities;
import com.apexballistics.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class MissileEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> DATA_WARHEAD =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIER =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);

    private static final float TURN_DEGREES_PER_TICK = 5.0F;

    private int maxLife = 200;
    private boolean exploded;
    private boolean arcFlight;
    private boolean arcInitialized;
    private Vec3 arcStart = Vec3.ZERO;
    private Vec3 arcEnd = Vec3.ZERO;
    private double arcHeight = 52.0D;
    private int arcTicks;
    private int arcDuration = 120;
    @Nullable
    private BlockPos launchPos;
    private float launchYaw;
    private boolean launchYawKnown;
    @Nullable
    private UUID lockedTargetId;
    @Nullable
    private LivingEntity lockedTarget;

    public MissileEntity(EntityType<? extends MissileEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.12D;
    }

    public MissileEntity(Level level, LivingEntity owner, Vec3 direction, WarheadType warhead) {
        super(ModEntities.MISSILE.get(), owner, direction.normalize(), level);
        this.setWarhead(warhead);
        this.setPos(owner.getX(), owner.getEyeY() - 0.15D, owner.getZ());
        this.setFlight(direction, 0.55D);
        this.maxLife = ApexConfig.maxLifetimeTicks > 0 ? ApexConfig.maxLifetimeTicks : 200;
    }

    public MissileEntity(Level level, double x, double y, double z, Vec3 direction, WarheadType warhead) {
        super(ModEntities.MISSILE.get(), x, y, z, direction.normalize(), level);
        this.setWarhead(warhead);
        this.setFlight(direction, 0.14D);
        this.maxLife = ApexConfig.maxLifetimeTicks > 0 ? ApexConfig.maxLifetimeTicks : 200;
    }

    private void setFlight(Vec3 direction, double power) {
        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }
        Vec3 n = direction.normalize();
        this.accelerationPower = 0.02D;
        this.setDeltaMovement(n.scale(power));
        this.hasImpulse = true;
    }

    public void setWarhead(WarheadType warhead) {
        this.entityData.set(DATA_WARHEAD, warhead.ordinal());
    }

    public WarheadType getWarhead() {
        return WarheadType.byId(this.entityData.get(DATA_WARHEAD));
    }

    public void setTier(MissileTier tier) {
        this.entityData.set(DATA_TIER, tier.ordinal());
    }

    public MissileTier getTier() {
        return MissileTier.byId(this.entityData.get(DATA_TIER));
    }

    public void setLockedTarget(LivingEntity target) {
        this.lockedTarget = target;
        this.lockedTargetId = target.getUUID();
    }

    /**
     * Lofted rainbow arc toward {@code impact} (or a default range along {@code yaw}).
     * Pad launches start visually vertical then pitch into the arc; handheld shots
     * start at a shallow loft so they never snap 0→90°.
     */
    public void startArc(float yaw, @Nullable Vec3 impact, boolean fromPad) {
        this.launchYaw = yaw;
        this.launchYawKnown = true;
        this.launchPos = this.blockPosition();
        this.setYRot(yaw);
        this.setXRot(fromPad ? -90.0F : -38.0F);
        this.yRotO = yaw;
        this.xRotO = this.getXRot();

        MissileTier tier = this.getTier();
        this.arcStart = this.position();
        Vec3 horiz = new Vec3(-Math.sin(Math.toRadians(yaw)), 0.0D, Math.cos(Math.toRadians(yaw)));
        double range = tier.range();
        Vec3 end = this.arcStart.add(horiz.scale(range));
        if (impact != null) {
            Vec3 flat = new Vec3(impact.x - this.arcStart.x, 0.0D, impact.z - this.arcStart.z);
            double dist = flat.length();
            if (dist > 4.0D) {
                range = Mth.clamp(dist, 16.0D, 180.0D);
                horiz = flat.scale(1.0D / dist);
                end = new Vec3(impact.x, impact.y, impact.z);
            }
        }
        double ground = this.findGroundY(end.x, end.z, Math.max(this.arcStart.y, end.y));
        this.arcEnd = new Vec3(end.x, ground, end.z);
        if (impact != null && Math.hypot(impact.x - this.arcStart.x, impact.z - this.arcStart.z) > 4.0D) {
            this.arcEnd = new Vec3(end.x, end.y + 0.4D, end.z);
        }
        this.arcHeight = tier.arcHeight();
        this.arcDuration = LoftedArc.durationTicks(range, this.arcHeight);
        this.arcTicks = 0;
        this.arcFlight = true;
        this.arcInitialized = true;
        this.maxLife = Math.max(this.maxLife, this.arcDuration + 40);
        this.setNoGravity(true);
        this.accelerationPower = 0.0D;
        this.setFlight(horiz.scale(0.72D).add(0.0D, 0.70D, 0.0D), 0.55D);
    }

    public void setLaunchYaw(float yaw) {
        this.launchYaw = yaw;
        this.launchYawKnown = true;
        this.setYRot(yaw);
        this.yRotO = yaw;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WARHEAD, WarheadType.HE.ordinal());
        builder.define(DATA_TIER, MissileTier.T1.ordinal());
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.arcFlight) {
            if (!this.arcInitialized) {
                this.startArc(this.launchYawKnown ? this.launchYaw : this.getYRot(), this.arcEnd, false);
            }
            this.tickArc();
            if (this.exploded) {
                return;
            }
        } else if (!this.level().isClientSide && !this.arcFlight
                && (this.getWarhead().homing() || this.lockedTargetId != null)) {
            this.steerTowardTarget();
        }

        float yawBefore = this.getYRot();
        float pitchBefore = this.getXRot();
        super.tick();
        this.yRotO = yawBefore;
        this.xRotO = pitchBefore;
        MissileOrientation.smoothTowardsMotion(this, this.getDeltaMovement(), TURN_DEGREES_PER_TICK, true);

        if (this.tickCount >= this.maxLife) {
            this.detonate();
            return;
        }

        if (this.level().isClientSide) {
            Vec3 motion = this.getDeltaMovement();
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() - motion.x * 0.5D,
                    this.getY() - motion.y * 0.5D,
                    this.getZ() - motion.z * 0.5D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    private void tickArc() {
        LivingEntity locked = this.resolveLockedTarget();
        if (locked == null && this.getWarhead().homing()) {
            locked = this.findHomingTarget();
            if (locked != null) {
                this.setLockedTarget(locked);
            }
        }
        if (locked != null) {
            Vec3 want = locked.position();
            this.arcEnd = this.arcEnd.lerp(new Vec3(want.x, want.y, want.z), 0.045D);
        }

        this.arcTicks++;
        double t = this.arcTicks / (double) Math.max(1, this.arcDuration);
        if (t >= 1.0D) {
            this.detonate();
            return;
        }

        Vec3 next = LoftedArc.point(this.arcStart, this.arcEnd, this.arcHeight, t);
        Vec3 vel = next.subtract(this.position());
        if (vel.lengthSqr() < 1.0E-6D) {
            this.detonate();
            return;
        }
        this.accelerationPower = 0.0D;
        this.setDeltaMovement(vel);
        this.hasImpulse = true;
    }

    private double findGroundY(double x, double z, double fromY) {
        Level level = this.level();
        int start = Mth.floor(fromY) + 8;
        int min = level.getMinBuildHeight();
        for (int y = start; y >= min; y--) {
            BlockPos pos = BlockPos.containing(x, y, z);
            var state = level.getBlockState(pos);
            if (!state.isAir() && !state.canBeReplaced() && state.getDestroySpeed(level, pos) >= 0.0F) {
                return y + 1.0D;
            }
        }
        return fromY;
    }

    private void steerTowardTarget() {
        LivingEntity target = this.resolveLockedTarget();
        if (target == null) {
            target = this.findHomingTarget();
            if (target != null) {
                this.setLockedTarget(target);
            }
        }
        if (target == null) {
            return;
        }
        Vec3 desired = target.getEyePosition().subtract(this.position());
        if (desired.lengthSqr() < 0.0001D) {
            return;
        }
        desired = desired.normalize();
        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 0.0001D) {
            current = desired;
        } else {
            current = current.normalize();
        }
        Vec3 blended = MissileOrientation.rotateToward(current, desired, 8.0F);
        this.setFlight(blended, Math.max(this.accelerationPower, 0.16D));
    }

    @Nullable
    private LivingEntity resolveLockedTarget() {
        if (this.lockedTarget != null && this.lockedTarget.isAlive() && !this.lockedTarget.isRemoved()) {
            return this.lockedTarget;
        }
        if (this.lockedTargetId != null && this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.lockedTargetId);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                this.lockedTarget = living;
                return living;
            }
        }
        this.lockedTarget = null;
        return null;
    }

    @Nullable
    private LivingEntity findHomingTarget() {
        double range = ApexConfig.homingRange > 0 ? ApexConfig.homingRange : 32.0D;
        Vec3 motion = this.getDeltaMovement();
        Vec3 dir = motion.lengthSqr() < 0.0001D ? this.getLookAngle() : motion.normalize();
        Vec3 origin = this.position();
        AABB search = this.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        Entity owner = this.getOwner();

        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, search, entity ->
                entity.isAlive()
                        && entity != owner
                        && !this.ownedBy(entity)
                        && (owner == null || !entity.isAlliedTo(owner)))) {
            Vec3 to = candidate.getEyePosition().subtract(origin);
            double dist = to.length();
            if (dist < 0.8D || dist > range) {
                continue;
            }
            double dot = to.normalize().dot(dir);
            if (dot < 0.25D) {
                continue;
            }
            double score = dist / Math.max(dot, 0.05D);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.shouldIgnoreLaunchHit(result)) {
            return;
        }
        super.onHit(result);
        this.detonate();
    }

    private boolean shouldIgnoreLaunchHit(HitResult result) {
        if (this.tickCount < 10) {
            return true;
        }
        if (this.arcFlight && this.arcTicks < 12 && result.getType() == HitResult.Type.BLOCK) {
            return true;
        }
        if (this.launchPos != null && result.getType() == HitResult.Type.BLOCK && this.tickCount < 18) {
            return this.blockPosition().closerThan(this.launchPos, 3.0D);
        }
        return false;
    }

    private void detonate() {
        if (this.level().isClientSide || this.exploded) {
            return;
        }
        this.exploded = true;
        WarheadType warhead = this.getWarhead();
        MissileTier tier = this.getTier();
        float power = warhead.explosionPower() * tier.powerMultiplier()
                * (ApexConfig.powerMultiplier > 0 ? ApexConfig.powerMultiplier : 1.0F);
        boolean fire = warhead.ignites();
        Level.ExplosionInteraction interaction = ApexConfig.griefing
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;

        this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, fire, interaction);

        if (warhead == WarheadType.HE || warhead == WarheadType.HOMING || warhead == WarheadType.INCENDIARY) {
            this.explodeSatellites(power, fire, interaction);
        }

        if (warhead == WarheadType.BUNKER) {
            this.level().explode(this, this.getX(), this.getY() - 2.0D, this.getZ(),
                    power * 0.85F, false, interaction);
            this.explodeSatellites(power * 0.7F, false, interaction);
        }

        if (warhead == WarheadType.CLUSTER) {
            this.spawnBomblets();
        }

        if (warhead == WarheadType.INCENDIARY) {
            this.spillLava();
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MISSILE_EXPLODE.get(), SoundSource.BLOCKS, 1.4F,
                0.8F + this.random.nextFloat() * 0.25F);
        this.discard();
    }

    private void explodeSatellites(float power, boolean fire, Level.ExplosionInteraction interaction) {
        int count = 5 + this.getTier().ordinal() * 2;
        double spread = 3.2D + this.getTier().ordinal() * 1.1D;
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * spread;
            double oz = (this.random.nextDouble() - 0.5D) * spread;
            this.level().explode(this, this.getX() + ox, this.getY() - 0.4D, this.getZ() + oz,
                    Math.max(2.2F, power * 0.34F),
                    fire, interaction);
        }
    }

    private void spillLava() {
        if (!ApexConfig.griefing) {
            return;
        }
        Level level = this.level();
        BlockPos origin = this.blockPosition();
        int radius = 1 + this.getTier().ordinal();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy >= -2 - this.getTier().ordinal(); dy--) {
                    if (dx * dx + dz * dz > radius * radius + 1) {
                        continue;
                    }
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!level.getWorldBorder().isWithinBounds(pos)
                            || pos.getY() < level.getMinBuildHeight()
                            || pos.getY() >= level.getMaxBuildHeight()) {
                        continue;
                    }
                    var state = level.getBlockState(pos);
                    if (state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }
                    if (state.isAir() || state.canBeReplaced()) {
                        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void spawnBomblets() {
        int count = 5 + this.getTier().ordinal() * 3;
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * 0.8D;
            double oz = (this.random.nextDouble() - 0.5D) * 0.8D;
            Vec3 vel = new Vec3(ox, -0.4D, oz);
            MissileEntity bomblet = new MissileEntity(this.level(), this.getX(), this.getY() + 0.2D, this.getZ(), vel, WarheadType.BOMBLET);
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity living) {
                bomblet.setOwner(living);
            } else {
                bomblet.setOwner(owner);
            }
            bomblet.setTier(MissileTier.T1);
            bomblet.setMaxLife(35);
            this.level().addFreshEntity(bomblet);
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return switch (this.getWarhead()) {
            case INCENDIARY -> ParticleTypes.FLAME;
            case HOMING -> ParticleTypes.ELECTRIC_SPARK;
            case BUNKER -> ParticleTypes.LARGE_SMOKE;
            case CLUSTER, BOMBLET -> ParticleTypes.SMOKE;
            default -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
        };
    }

    @Override
    protected float getInertia() {
        return this.arcFlight ? 1.0F : 0.97F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.arcFlight ? 1.0F : 0.82F;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Warhead", this.getWarhead().ordinal());
        tag.putInt("Tier", this.getTier().ordinal());
        tag.putInt("MaxLife", this.maxLife);
        tag.putBoolean("ArcFlight", this.arcFlight);
        tag.putBoolean("ArcInitialized", this.arcInitialized);
        tag.putDouble("ArcStartX", this.arcStart.x);
        tag.putDouble("ArcStartY", this.arcStart.y);
        tag.putDouble("ArcStartZ", this.arcStart.z);
        tag.putDouble("ArcEndX", this.arcEnd.x);
        tag.putDouble("ArcEndY", this.arcEnd.y);
        tag.putDouble("ArcEndZ", this.arcEnd.z);
        tag.putDouble("ArcHeight", this.arcHeight);
        tag.putInt("ArcTicks", this.arcTicks);
        tag.putInt("ArcDuration", this.arcDuration);
        tag.putFloat("LaunchYaw", this.launchYaw);
        tag.putBoolean("LaunchYawKnown", this.launchYawKnown);
        if (this.launchPos != null) {
            tag.putLong("LaunchPos", this.launchPos.asLong());
        }
        if (this.lockedTargetId != null) {
            tag.putUUID("LockedTarget", this.lockedTargetId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setWarhead(WarheadType.byId(tag.getInt("Warhead")));
        this.setTier(MissileTier.byId(tag.getInt("Tier")));
        this.maxLife = tag.contains("MaxLife") ? tag.getInt("MaxLife") : 200;
        this.arcFlight = tag.getBoolean("ArcFlight") || tag.getBoolean("Ballistic") || tag.getBoolean("SiloLaunch");
        this.arcInitialized = tag.getBoolean("ArcInitialized");
        this.arcStart = new Vec3(tag.getDouble("ArcStartX"), tag.getDouble("ArcStartY"), tag.getDouble("ArcStartZ"));
        this.arcEnd = new Vec3(tag.getDouble("ArcEndX"), tag.getDouble("ArcEndY"), tag.getDouble("ArcEndZ"));
        this.arcHeight = tag.contains("ArcHeight") ? tag.getDouble("ArcHeight") : this.getTier().arcHeight();
        this.arcTicks = tag.getInt("ArcTicks");
        this.arcDuration = tag.contains("ArcDuration") ? tag.getInt("ArcDuration") : 120;
        this.launchYaw = tag.getFloat("LaunchYaw");
        this.launchYawKnown = tag.getBoolean("LaunchYawKnown");
        if (tag.contains("LaunchPos")) {
            this.launchPos = BlockPos.of(tag.getLong("LaunchPos"));
        }
        if (tag.hasUUID("LockedTarget")) {
            this.lockedTargetId = tag.getUUID("LockedTarget");
        }
        if (this.arcFlight && this.arcEnd.lengthSqr() < 1.0E-6D && tag.contains("CruiseTarget")) {
            BlockPos dest = BlockPos.of(tag.getLong("CruiseTarget"));
            this.arcEnd = Vec3.atCenterOf(dest);
            this.arcInitialized = false;
        }
    }

    public float getRenderYaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.yRotO, this.getYRot());
    }

    public float getRenderPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.xRotO, this.getXRot());
    }
}
