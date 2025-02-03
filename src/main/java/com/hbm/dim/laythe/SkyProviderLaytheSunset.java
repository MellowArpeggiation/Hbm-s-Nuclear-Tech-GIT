package com.hbm.dim.laythe;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SkyProviderCelestial;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class SkyProviderLaytheSunset extends SkyProviderCelestial {
	private static final Shader shaeder =  new Shader(new ResourceLocation(RefStrings.MODID, "shaders/fle.frag"));
	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	public SkyProviderLaytheSunset() {
		super();
	}

	@Override
	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc) {
		Tessellator tessellator = Tessellator.instance;

		float[] sunsetColor = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
		float time = ((float)world.getWorldTime() + partialTicks) * 0.2F;



		
		if(sunsetColor != null) {
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO); // The magic sauce

			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;
			byte segments = 16;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			
			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
	
				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0D, 150.0D, 0.0D);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F);
	
				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellator.addVertex((double)(sinAngle * 160.0F), (double)(cosAngle * 160.0F), (double)(-cosAngle * 90.0F * sunsetColor[3]));
				}
	
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(135.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -60, 0);
	
				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0D, 100.0D, 0.0D);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F);
	
				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					
					tessellator.addVertex((double)(sinAngle * 100.0F), (double)(cosAngle * 100.0F), (double)(-cosAngle * 90.0F));
				}
	
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{			
	
				GL11.glRotatef(135.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -30, 0);
	
				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0D, 80.0D, 0.0D);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1] * 0.2F, sunsetColor[2], 0.0F);
	
				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					
					tessellator.addVertex((double)(sinAngle * 100.0F), (double)(cosAngle * 100.0F), (double)(-cosAngle * 90.0F));
				}
	
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			
		}
		
		//TODO: move this to skyprovider celestial so that way any planet can be compromised :P
		GL11.glPushMatrix();
		Shader shader = shaeder;
		double size = 2;
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glDepthMask(true);
		shader.use();
		GL11.glScaled(194.5, 40.5, 94.5);
		GL11.glRotated(90, 0, 0, 1);
		int textureUnit = 0;

		mc.renderEngine.bindTexture(noise);
		ResourceManager.sphere_v2.renderAll();

		GL11.glPushMatrix();

		// Fix orbital plane
		GL11.glRotatef(-90.0F, 0, 1, 0);
		
		shader.setTime((time * 0.05F));
		shader.setTextureUnit(textureUnit);
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
		tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
		tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
		tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
		tessellator.draw();


		shader.stop();
		GL11.glDepthMask(false);

		GL11.glPopMatrix();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		
		GL11.glPopMatrix();
	}

}
