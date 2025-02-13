package com.xyazh.kanake.entity;

import com.xyazh.kanake.damage.MagicDamage;
import com.xyazh.kanake.particle.ModParticles;
import com.xyazh.kanake.util.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class EntityBlockingLookAt extends EntityShoot {
    public Entity target = null;
    public int lockAge = 5;
    public double speed = 2.0;

    public EntityBlockingLookAt(World worldIn) {
        super(worldIn);
    }

    @Override
    public void entityInit() {
    }

    public void findTarget() {
        AxisAlignedBB aabb = new AxisAlignedBB(
                this.posX + 16, this.posY + 16, this.posZ + 16,
                this.posX - 16, this.posY - 16, this.posZ - 16);
        Entity target = null;
        for (Entity entity :
                this.world.getEntitiesWithinAABB(Entity.class, aabb, (e) -> e instanceof IProjectile || e instanceof EntityFireball || e instanceof EntityShulkerBullet)) {
            if(entity instanceof EntityBlockingLookAt){
                if(((EntityBlockingLookAt) entity).target==this.target){
                    continue;
                }
            }
            if(target == null){
                target = entity;
                continue;
            }
            Vec3d target1Pos = new Vec3d(entity.posX, entity.posY, entity.posZ);
            Vec3d target2Pos = new Vec3d(target.posX, target.posY, target.posZ);
            Vec3d thisPos = new Vec3d(this.posX, this.posY, this.posZ);
            target1Pos.sub(thisPos);
            target2Pos.sub(thisPos);
            if(target1Pos.length() < target2Pos.length()){
                target = entity;
            }
        }
        this.target = target;
    }

    public boolean hasNoGravity() {
        return this.target != null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            for (int i = 0; i <= 10; i++) {
                this.world.spawnParticle(ModParticles.MAGIC_PARTICLES, posX, posY, posZ, 0, 0, 0);
                this.world.spawnParticle(ModParticles.MAGIC_PARTICLES1, posX, posY, posZ, 0, 0, 0);
            }
        }
        if(this.collided){
            this.setDeadParticle();
            this.setDead();
        }
        if (this.lockAge > 0) {
            this.lockAge -= 1;
        } else if (this.target != null) {
            Vec3d targetPos = new Vec3d(this.target.posX, this.target.posY, this.target.posZ);
            Vec3d thisPos = new Vec3d(this.posX, this.posY, this.posZ);
            this.speed += 0.002;
            Vec3d motion = new Vec3d();
            motion.sub(targetPos, thisPos);
            motion.normalize();
            this.forward.set(motion);
            motion.mul(this.speed * 1.5);
            this.motionX = motion.x;
            this.motionY = motion.y;
            this.motionZ = motion.z;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            if (this.target.isDead) {
                this.target = null;
            }
        } else {
            this.findTarget();
        }
        if (this.onHurt()) {
            this.setDead();
            this.setDeadParticle();
        }
    }

    protected void setDeadParticle(){
        if (world.isRemote) {
            for (int i = 0; i <= 100; i++) {
                this.world.spawnParticle(ModParticles.TEST_PARTICLES2, posX, posY, posZ, 0, 0, 0);
            }
        }
    }

    protected boolean onHurt() {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(posX + 0.4, posY + 0.4, posZ + 0.4, posX - 0.4, posY - 0.4, posZ - 0.4);
        List<Entity> entitys = world.getEntitiesWithinAABB(
                Entity.class,
                axisAlignedBB,
                (e) -> (e instanceof IProjectile || e instanceof EntityFireball || e instanceof EntityShulkerBullet)||(e instanceof EntityLivingBase));
        boolean flag = false;
        for (Entity entity : entitys) {
            if(entity instanceof IProjectile){
                if(entity instanceof EntityBlockingLookAt){
                    if(((EntityBlockingLookAt) entity).target==this.target){
                        continue;
                    }
                }
                flag = true;
                entity.setDead();
            } else if(entity instanceof EntityLivingBase){
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if(entityLivingBase.equals(this.shootingEntity)){
                    continue;
                }
                flag = true;
                MagicDamage damageSource = new MagicDamage();
                damageSource.setAttacker(this.shootingEntity);
                entityLivingBase.attackEntityFrom(damageSource, 4);
            }else{
                flag = true;
                entity.setDead();
            }
        }
        return flag;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }
}
