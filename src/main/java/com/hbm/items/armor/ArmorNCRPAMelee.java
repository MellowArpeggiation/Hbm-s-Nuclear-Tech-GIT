package com.hbm.items.armor;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ArmorNCRPAMelee implements IPAMelee {

	public void setupFirstPerson(ItemStack stack) { }
	
	public void renderFirstPerson(ItemStack stack) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ncrpa_arm);
		
		GL11.glTranslated(0, -1.5, 0.5);
		double scale = 0.125D;
		GL11.glScaled(scale, scale, scale);
		
		double forwardTilt = 60;
		double offsetOutward = 3;
		double roll = 60;
		
		GL11.glPushMatrix();
		GL11.glRotated(forwardTilt, 1, 0, 0);
		
		GL11.glTranslated(offsetOutward, 0, 0);
		GL11.glTranslated(6, 8, 0);
		GL11.glRotated(roll, 0, 1, 0);
		GL11.glRotated(10, 0, 0, 1);
		GL11.glTranslated(-6, -8, 0);
		ResourceManager.armor_ncr.renderPart("LeftArm");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		
		//GL11.glTranslated(7, 0, 4);
		
		GL11.glRotated(forwardTilt, 1, 0, 0);
		
		GL11.glTranslated(-offsetOutward, 0, 0);
		GL11.glTranslated(-6, 8, 0);
		GL11.glRotated(-90, 0, 0, 1);
		GL11.glRotated(-roll - 30, 0, 1, 0);
		GL11.glTranslated(6, -8, 0);
		ResourceManager.armor_ncr.renderPart("RightArm");
		GL11.glPopMatrix();
	}
}
