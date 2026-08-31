package com.apexballistics.entity;

import com.apexballistics.ApexConfig;
import com.apexballistics.registry.ModEntities;
import com.apexballistics.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CruiseMissileEntity extends AbstractHurtingProjectile {
    private int maxLife = 800;
    private int boostTicks;
    private boolean exploded;
    @Nullable
    private BlockPos target;

    public CruiseMissileEntity(EntityType<? extends CruiseMissileEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.14D;
    }

    public CruiseMissileEntity(Level level, double x, double y, double z, Vec3 direction) {
        super(ModEntities.CRUISE_MISSILE.get(), x, y, z, direction.normalize(), level);
        this.setFlight(direction, 0.16D);
        this.maxLife = ApexConfig.cruiseMaxLifetimeTicks > 0 ? ApexConfig.cruiseMaxLifetimeTicks : 800;
    }

    private void setFlight(Vec3 direction, double power) {
        this.accelerationPower = power;
        this.setDeltaMovement(direction.normalize().scale(power));
        this.hasImpulse = true;
    }

    public void setCruiseTarget(BlockPos target) {
        this.target = target.immutable();
        this.boostTicks = 16;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.target != null) {
            Vec3 dest = Vec3.atCenterOf(this.target);
            Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
            if (center.distanceToSqr(dest) < 2.25D) {
                this.detonate();
                return;
            }
            if (this.boostTicks > 0) {
                this.boostTicks--;
                this.setFlight(new Vec3(0.0D, 1.0D, 0.0D), 0.2D);
            } else {
                Vec3 to = dest.subtract(this.position());
                if (to.lengthSqr() > 0.0001D) {
                    this.setFlight(to, 0.22D);
                }
            }
        }

        super.tick();

        if (this.tickCount >= this.maxLife) {
            this.detonate();
            return;
        }

        if (this.level().isClientSide) {
            Vec3 motion = this.getDeltaMovement();
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX() - motion.x,
                    this.getY() + 0.4D - motion.y,
                    this.getZ() - motion.z,
                    0.0D, 0.02D, 0.0D);
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.2D, this.getZ(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.tickCount < 10 && result.getType() == HitResult.Type.BLOCK) {
            return;
        }
        super.onHit(result);
        this.detonate();
    }

    private void detonate() {
        if (this.level().isClientSide || this.exploded) {
            return;
        }
        this.exploded = true;
        float power = ApexConfig.cruiseExplosionPower * (ApexConfig.powerMultiplier > 0 ? ApexConfig.powerMultiplier : 1.0F);
        Level.ExplosionInteraction interaction = ApexConfig.griefing
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;

        this.level().explode(this, this.getX(), this.getY(), this.getZ(), power, false, interaction);
        this.level().explode(this, this.getX(), this.getY() - 3.0D, this.getZ(), power * 0.9F, false, interaction);
        this.level().explode(this, this.getX(), this.getY() - 6.0D, this.getZ(), power * 0.7F, false, interaction);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.level().explode(this,
                    this.getX() + direction.getStepX() * 4.0D,
                    this.getY() - 1.0D,
                    this.getZ() + direction.getStepZ() * 4.0D,
                    power * 0.55F, false, interaction);
        }

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.MISSILE_EXPLODE.get(), SoundSource.BLOCKS, 2.0F,
                0.55F + this.random.nextFloat() * 0.15F);
        this.discard();
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
    }

    @Override
    protected float getInertia() {
        return 0.98F;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 384.0D * 384.0D;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MaxLife", this.maxLife);
        tag.putInt("BoostTicks", this.boostTicks);
        if (this.target != null) {
            tag.putLong("Target", this.target.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.maxLife = tag.contains("MaxLife") ? tag.getInt("MaxLife") : 800;
        this.boostTicks = tag.getInt("BoostTicks");
        this.target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        if (owner instanceof LivingEntity living) {
            this.setRot(living.getYRot(), living.getXRot());
        }
    }
}
