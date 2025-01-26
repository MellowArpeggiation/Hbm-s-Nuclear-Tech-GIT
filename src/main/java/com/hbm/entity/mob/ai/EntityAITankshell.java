package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityRocket;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.items.weapon.sedna.factory.XFactory12ga;
import com.hbm.items.weapon.sedna.factory.XFactory40mm;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.items.weapon.sedna.factory.XFactoryRocket;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;
import scala.collection.mutable.ArrayBuilder.ofBoolean;

public class EntityAITankshell extends EntityAIBase {
	
	private EntityCreature owner;
    private EntityLivingBase target;
    private int delay;
    private int timer;
    private int AttackDistance;
    private boolean artilleryMode;
    private int reloadTimer;
    private int reloadDelay;

	public EntityAITankshell(EntityCreature owner, boolean checkSight, boolean nearbyOnly, int delay, int switchAttackDistance, int reloadDelay) {
		this.owner = owner;
		this.delay = delay;
		this.timer = delay;
		this.AttackDistance = switchAttackDistance;
		this.artilleryMode = true;
		this.reloadTimer = reloadDelay;
		this.reloadDelay = reloadDelay;
	}

	@Override
	public boolean shouldExecute() {
        EntityLivingBase entity = this.owner.getAttackTarget();

        if(entity == null) {
            return false;
        } else {
            this.target = entity;
            double dist = Vec3.createVectorHelper(target.posX - owner.posX, target.posY - owner.posY, target.posZ - owner.posZ).lengthVector();
            if(dist > AttackDistance) {
                artilleryMode = true;
            } else {
                artilleryMode = false;
            }

            return dist > 2 && dist < 50;
        }
        
	}
	
	@Override
    public boolean continueExecuting() {
        return this.shouldExecute() || !this.owner.getNavigator().noPath();
    }

	@Override
    public void updateTask() {
		timer--;
		if(timer <= 0) {
			if(artilleryMode) {
				fireArtilleryShell();
			}
			timer = delay;
		}
		this.owner.rotationYaw = this.owner.rotationYawHead;
    }

	
	private void fireArtilleryShell() {
		if(reloadTimer <= 0) {
            double dist = Vec3.createVectorHelper(target.posX - owner.posX, target.posY - owner.posY, target.posZ - owner.posZ).lengthVector();
            System.out.println(dist);
            double radYaw = Math.toRadians(owner.rotationYaw);
            double radPitch = Math.toRadians(owner.rotationPitch);

            double forwardX = -Math.sin(radYaw) * Math.cos(radPitch);
            double forwardY = -Math.sin(radPitch);
            double forwardZ = Math.cos(radYaw) * Math.cos(radPitch);

            double spawnDistance = 3.0;
            double spawnX = owner.posX + forwardX * spawnDistance;
            double spawnY = owner.posY + 3 + forwardY * spawnDistance;
            double spawnZ = owner.posZ + forwardZ * spawnDistance;
			if(dist > 50) {
		        EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactoryRocket.rocket_qd[1], 4, 0.01F, spawnX, spawnY, spawnZ);
		        bullet.setPosition(spawnX, spawnY, spawnZ);
		        owner.worldObj.spawnEntityInWorld(bullet);	        
			} else {
				for (int i = 0; i < 4; i++) {
	            EntityBulletBaseMK4 bullet = new EntityBulletBaseMK4(owner, XFactory12ga.g12_explosive, 4, 0.01F, spawnX, spawnY, spawnZ);
	            bullet.setPosition(spawnX, spawnY, spawnZ);
	            owner.worldObj.spawnEntityInWorld(bullet);
				}
			}

			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_fire", 25.0F, 1.0F);
			reloadTimer = reloadDelay;
		} else {
			reloadTimer--;
		}
		//System.out.println(reloadTimer);
		if(reloadTimer == 20) {
			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_reload", 3.0F, 1.0F);

		}
	}

}