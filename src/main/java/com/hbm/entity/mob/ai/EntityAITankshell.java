package com.hbm.entity.mob.ai;

import com.hbm.entity.projectile.EntityArtilleryShell;
import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.entity.projectile.EntityRocket;
import com.hbm.handler.BulletConfigSyncingUtil;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;

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
			EntityBulletBaseNT bullet = new EntityBulletBaseNT(owner.worldObj, BulletConfigSyncingUtil.SHELL_NORMAL, owner, target, 1.0F, 0);
			bullet.setPosition(owner.posX, owner.posY + 3, owner.posZ);
			owner.worldObj.spawnEntityInWorld(bullet);
			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_fire", 25.0F, 1.0F);
			reloadTimer = reloadDelay;
		} else {
			reloadTimer--;
		}
		System.out.println(reloadTimer);
		if(reloadTimer == 20) {
			owner.worldObj.playSoundEffect(owner.posX, owner.posY, owner.posZ, "hbm:turret.jeremy_reload", 3.0F, 1.0F);

		}
	}

}