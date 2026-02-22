package com.hbm.render.item.weapon.sedna;

import org.lwjgl.opengl.GL11;

import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemRenderPAMelee extends ItemRenderWeaponBase {
	
	@Override public boolean isAkimbo(EntityLivingBase entity) { return true; }

	@Override
	public void setupFirstPerson(ItemStack stack) {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.setupFirstPerson(stack);
	}
	
	@Override
	public void renderFirstPerson(ItemStack stack) {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.renderFirstPerson(stack);
	}

	@Override public void setupThirdPerson(ItemStack stack) { }
	@Override public void setupThirdPersonAkimbo(ItemStack stack) { }

	@Override
	public void setupInv(ItemStack stack) {
		GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glScaled(1, 1, -1);
		GL11.glTranslated(8, 8, 0);
		double scale = 2.5D;
		GL11.glScaled(scale, scale, scale);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -12.5D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glTranslated(0, -0.5, 1);
	}

	@Override
	public void renderInv(ItemStack stack) {
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.maresleg_tex);

		GL11.glPushMatrix();
		GL11.glRotated(225, 0, 0, 1);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(25, 1, 0, 0);
		GL11.glRotated(45, 0, 1, 0);
		GL11.glTranslated(-1, 0, 0);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		GL11.glPopMatrix();

		GL11.glTranslated(0, 0, 5);
		GL11.glPushMatrix();
		GL11.glRotated(225, 0, 0, 1);
		GL11.glRotated(-90, 0, 1, 0);
		GL11.glRotated(-90, 1, 0, 0);
		GL11.glRotated(25, 1, 0, 0);
		GL11.glRotated(-45, 0, 1, 0);
		GL11.glTranslated(1, 0, 0);
		ResourceManager.maresleg.renderPart("Gun");
		ResourceManager.maresleg.renderPart("Lever");
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
	}
}
