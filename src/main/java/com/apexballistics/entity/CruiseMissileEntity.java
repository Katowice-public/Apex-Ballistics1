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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CruiseMissileEntity extends AbstractHurtingProjectile {
    private static final float TURN_DEGREES_PER_TICK = 4.5F;
    private static final float HEADING_DEGREES_PER_TICK = 6.5F;
    private static final int BOOST_PITCH_TICKS = 32;

    private int maxLife = 800;
    private boolean exploded;
    private BallisticFlight.Phase phase = BallisticFlight.Phase.BOOST;
    private double cruiseAltitude = 120.0D;
    private float launchYaw;
    private Vec3 smoothedDir = new Vec3(0.0D, 1.0D, 0.0D);
    @Nullable
    private BlockPos target;
    @Nullable
    private BlockPos launchPos;

    public CruiseMissileEntity(EntityType<? extends CruiseMissileEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.08D;
    }

    public CruiseMissileEntity(Level level, double x, double y, double z, Vec3 direction) {
        super(ModEntities.CRUISE_MISSILE.get(), x, y, z, direction.normalize(), level);
        this.setFlight(direction, BallisticFlight.speed(BallisticFlight.Phase.BOOST));
        this.maxLife = ApexConfig.cruiseMaxLifetimeTicks > 0 ? ApexConfig.cruiseMaxLifetimeTicks : 800;
        this.launchPos = BlockPos.containing(x, y, z);
        this.cruiseAltitude = BallisticFlight.cruiseAltitude(level, y, y, ApexConfig.cruiseAltitudeBonus);
        this.smoothedDir = direction.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 1.0D, 0.0D) : direction.normalize();
    }

    private void setFlight(Vec3 direction, double power) {
        this.accelerationPower = Math.min(0.12D, power * 0.12D);
        this.setDeltaMovement(direction.normalize().scale(power));
        this.hasImpulse = true;
    }

    public void setCruiseTarget(BlockPos target) {
        this.target = target.immutable();
        this.phase = BallisticFlight.Phase.BOOST;
        if (this.level() != null) {
            this.cruiseAltitude = BallisticFlight.cruiseAltitude(
                    this.level(), this.getY(), target.getY(), ApexConfig.cruiseAltitudeBonus);
        }
    }

    public void setLaunchYaw(float yaw) {
        this.launchYaw = yaw;
        this.setYRot(yaw);
        this.yRotO = yaw;
        double yawRad = Math.toRadians(yaw);
        this.smoothedDir = new Vec3(-Math.sin(yawRad) * 0.77D, 0.64D, Math.cos(yawRad) * 0.77D).normalize();
        this.setFlight(this.smoothedDir, BallisticFlight.speed(BallisticFlight.Phase.BOOST));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.target != null) {
            Vec3 dest = Vec3.atCenterOf(this.target);
            Vec3 pos = this.position();
            if (pos.distanceToSqr(dest) < 4.0D && this.phase == BallisticFlight.Phase.DIVE) {
                this.detonate();
                return;
            }
            this.phase = BallisticFlight.nextPhase(this.phase, pos, dest, this.cruiseAltitude);
            Vec3 desired = BallisticFlight.desiredDirection(this.phase, pos, dest, this.cruiseAltitude);
            if (this.phase == BallisticFlight.Phase.BOOST && this.tickCount < BOOST_PITCH_TICKS) {
                double t = Mth.clamp(this.tickCount / (double) BOOST_PITCH_TICKS, 0.0D, 1.0D);
                t = t * t * (3.0D - 2.0D * t);
                double yawRad = Math.toRadians(this.launchYaw);
                Vec3 takeoff = new Vec3(-Math.sin(yawRad) * 0.77D, 0.64D, Math.cos(yawRad) * 0.77D).normalize();
                desired = takeoff.lerp(new Vec3(0.0D, 1.0D, 0.0D), t).normalize();
            }
            this.smoothedDir = MissileOrientation.rotateToward(this.smoothedDir, desired, HEADING_DEGREES_PER_TICK);
            this.setFlight(this.smoothedDir, BallisticFlight.speed(this.phase));
        }

        float yawBefore = this.getYRot();
        float pitchBefore = this.getXRot();
        super.tick();
        this.yRotO = yawBefore;
        this.xRotO = pitchBefore;
        MissileOrientation.smoothTowardsMotion(this, this.getDeltaMovement(), TURN_DEGREES_PER_TICK, false);

        if (this.tickCount >= this.maxLife) {
            this.detonate();
            return;
        }

        if (this.level().isClientSide) {
            Vec3 motion = this.getDeltaMovement();
            Vec3 tail = this.position().subtract(motion.normalize().scale(1.6D));
            this.level().addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    tail.x, tail.y, tail.z, 0.0D, 0.02D, 0.0D);
            this.level().addParticle(ParticleTypes.FLAME,
                    tail.x, tail.y, tail.z, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                    tail.x, tail.y, tail.z, 0.0D, 0.04D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.shouldIgnoreHit(result)) {
            return;
        }
        super.onHit(result);
        this.detonate();
    }

    private boolean shouldIgnoreHit(HitResult result) {
        if (result.getType() != HitResult.Type.BLOCK) {
            return this.tickCount < 12;
        }
        if (this.phase == BallisticFlight.Phase.BOOST) {
            return true;
        }
        if (this.tickCount < 20) {
            return true;
        }
        if (result instanceof BlockHitResult blockHit && this.launchPos != null) {
            BlockPos hit = blockHit.getBlockPos();
            return hit.closerThan(this.launchPos, 3.0D) && hit.getY() <= this.launchPos.getY() + 2;
        }
        return false;
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
        return 0.96F;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 512.0D * 512.0D;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MaxLife", this.maxLife);
        tag.putInt("Phase", this.phase.ordinal());
        tag.putDouble("CruiseAltitude", this.cruiseAltitude);
        tag.putFloat("LaunchYaw", this.launchYaw);
        tag.putDouble("SmoothX", this.smoothedDir.x);
        tag.putDouble("SmoothY", this.smoothedDir.y);
        tag.putDouble("SmoothZ", this.smoothedDir.z);
        if (this.target != null) {
            tag.putLong("Target", this.target.asLong());
        }
        if (this.launchPos != null) {
            tag.putLong("LaunchPos", this.launchPos.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.maxLife = tag.contains("MaxLife") ? tag.getInt("MaxLife") : 800;
        int phaseId = tag.getInt("Phase");
        BallisticFlight.Phase[] phases = BallisticFlight.Phase.values();
        this.phase = phaseId >= 0 && phaseId < phases.length ? phases[phaseId] : BallisticFlight.Phase.BOOST;
        this.cruiseAltitude = tag.contains("CruiseAltitude") ? tag.getDouble("CruiseAltitude") : 120.0D;
        this.launchYaw = tag.getFloat("LaunchYaw");
        if (tag.contains("SmoothX")) {
            this.smoothedDir = new Vec3(tag.getDouble("SmoothX"), tag.getDouble("SmoothY"), tag.getDouble("SmoothZ"));
        }
        this.target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
        this.launchPos = tag.contains("LaunchPos") ? BlockPos.of(tag.getLong("LaunchPos")) : null;
    }
}
