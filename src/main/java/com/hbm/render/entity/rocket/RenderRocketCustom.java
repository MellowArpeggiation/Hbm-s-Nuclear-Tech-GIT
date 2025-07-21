package com.hbm.render.entity.rocket;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.handler.RocketStruct;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.MissilePronter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderRocketCustom extends Render {
	private static final Shader shadeder =  new Shader(new ResourceLocation(RefStrings.MODID, "shaders/blackholed.frag"));
	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float f, float interp) {
        if(!(entity instanceof EntityRideableRocket)) return;
        EntityRideableRocket rocketEntity = (EntityRideableRocket) entity;
        RocketStruct rocket = rocketEntity.getRocket();

        GL11.glPushMatrix();
        {

            GL11.glTranslated(x, y, z);
            GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * interp - 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * interp, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * interp - 90.0F, 0.0F, -1.0F, 0.0F);

            MissilePronter.prontRocket(rocket, rocketEntity, Minecraft.getMinecraft().getTextureManager(), true, interp);
            
            if(rocket.stages.size() <= 0) {
            	if(rocketEntity.getState() == RocketState.LANDING) {
            		if(rocketEntity.posY > 200) {
                   		GL11.glPushMatrix();
                		GL11.glDisable(GL11.GL_CULL_FACE);
                		long time = rocketEntity.worldObj.getTotalWorldTime();


                		Shader shader = shadeder;

                		shader.use();
                		GL11.glShadeModel(GL11.GL_SMOOTH);
                		int textureUnit = 0;
                		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                		bindTexture(noise);

                		GL11.glPushMatrix();

                		GL11.glRotatef(0.0F, 0, 1, 0);
                		GL11.glScalef(4, 24, 4);
                		GL11.glTranslatef(0F, 0.9F, 0F);

                		GL11.glColor4d(0, 0, 0, 0.5);
            			shader.setUniform1f("iTime", time + interp);
            			shader.setUniform1i("iChannel1", 0);


                		ResourceManager.hemisphere.renderAll();


                		shader.stop();

                		GL11.glPopMatrix();

                		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
                		
                		GL11.glEnable(GL11.GL_CULL_FACE);


                		GL11.glPopMatrix();
            		}

            	}
            }
        }
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.universal;
    }
    
}
