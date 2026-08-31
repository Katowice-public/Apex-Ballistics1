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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class MissileEntity extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Integer> DATA_WARHEAD =
            SynchedEntityData.defineId(MissileEntity.class, EntityDataSerializers.INT);

    private int maxLife = 200;
    private int boostTicks;
    private boolean siloLaunch;
    private boolean exploded;
    @Nullable
    private BlockPos cruiseTarget;
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
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        this.assignDirectionalMovement(direction.normalize(), 0.16D);
        this.maxLife = ApexConfig.maxLifetimeTicks > 0 ? ApexConfig.maxLifetimeTicks : 200;
    }

    public MissileEntity(Level level, double x, double y, double z, Vec3 direction, WarheadType warhead) {
        super(ModEntities.MISSILE.get(), x, y, z, direction.normalize(), level);
        this.setWarhead(warhead);
        this.assignDirectionalMovement(direction.normalize(), 0.14D);
        this.maxLife = ApexConfig.maxLifetimeTicks > 0 ? ApexConfig.maxLifetimeTicks : 200;
    }

    public void setWarhead(WarheadType warhead) {
        this.entityData.set(DATA_WARHEAD, warhead.ordinal());
    }

    public WarheadType getWarhead() {
        return WarheadType.byId(this.entityData.get(DATA_WARHEAD));
    }

    public void setLockedTarget(LivingEntity target) {
        this.lockedTarget = target;
        this.lockedTargetId = target.getUUID();
    }

    public void setSiloLaunch(boolean siloLaunch, @Nullable BlockPos cruiseTarget) {
        this.siloLaunch = siloLaunch;
        this.cruiseTarget = cruiseTarget;
        this.boostTicks = siloLaunch ? 14 : 0;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WARHEAD, WarheadType.HE.ordinal());
    }

    @Override
    public void tick() {
        if (this.siloLaunch && this.boostTicks > 0) {
            this.boostTicks--;
            this.assignDirectionalMovement(new Vec3(0.0D, 1.0D, 0.0D), 0.18D);
            if (this.boostTicks == 0) {
                Vec3 cruise = this.cruiseDirection();
                this.assignDirectionalMovement(cruise, 0.16D);
            }
        } else if (!this.level().isClientSide && (this.getWarhead().homing() || this.lockedTargetId != null)) {
            this.steerTowardTarget();
        }

        super.tick();

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

    private Vec3 cruiseDirection() {
        if (this.cruiseTarget != null) {
            Vec3 to = Vec3.atCenterOf(this.cruiseTarget).subtract(this.position());
            if (to.lengthSqr() > 0.0001D) {
                return to.normalize();
            }
        }
        return this.getDeltaMovement().lengthSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : this.getDeltaMovement().normalize();
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
        Vec3 blended = current.scale(0.72D).add(desired.scale(0.28D)).normalize();
        this.assignDirectionalMovement(blended, Math.max(this.accelerationPower, 0.16D));
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
        super.onHit(result);
        this.detonate();
    }

    private void detonate() {
        if (this.level().isClientSide || this.exploded) {
            return;
        }
        this.exploded = true;
        WarheadType warhead = this.getWarhead();
        float power = warhead.explosionPower() * (ApexConfig.powerMultiplier > 0 ? ApexConfig.powerMultiplier : 1.0F);
        boolean fire = warhead.ignites();
        Level.ExplosionInteraction interaction = ApexConfig.griefing
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;

        this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, fire, interaction);

        if (warhead == WarheadType.BUNKER) {
            this.level().explode(this, this.getX(), this.getY() - 2.0D, this.getZ(),
                    power * 0.85F, false, interaction);
        }

        if (warhead == WarheadType.CLUSTER) {
            this.spawnBomblets();
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MISSILE_EXPLODE.get(), SoundSource.BLOCKS, 1.4F,
                0.8F + this.random.nextFloat() * 0.25F);
        this.discard();
    }

    private void spawnBomblets() {
        for (int i = 0; i < 5; i++) {
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
        return 0.97F;
    }

    @Override
    protected float getLiquidInertia() {
        return 0.82F;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D * 256.0D;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Warhead", this.getWarhead().ordinal());
        tag.putInt("MaxLife", this.maxLife);
        tag.putInt("BoostTicks", this.boostTicks);
        tag.putBoolean("SiloLaunch", this.siloLaunch);
        if (this.cruiseTarget != null) {
            tag.putLong("CruiseTarget", this.cruiseTarget.asLong());
        }
        if (this.lockedTargetId != null) {
            tag.putUUID("LockedTarget", this.lockedTargetId);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setWarhead(WarheadType.byId(tag.getInt("Warhead")));
        this.maxLife = tag.contains("MaxLife") ? tag.getInt("MaxLife") : 200;
        this.boostTicks = tag.getInt("BoostTicks");
        this.siloLaunch = tag.getBoolean("SiloLaunch");
        if (tag.contains("CruiseTarget")) {
            this.cruiseTarget = BlockPos.of(tag.getLong("CruiseTarget"));
        }
        if (tag.hasUUID("LockedTarget")) {
            this.lockedTargetId = tag.getUUID("LockedTarget");
        }
    }

    public float getRenderYaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.yRotO, this.getYRot());
    }

    public float getRenderPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.xRotO, this.getXRot());
    }
}
