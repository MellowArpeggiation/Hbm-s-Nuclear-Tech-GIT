package com.hbm.dim;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.thatmo.WorldProviderThatmo;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteWar;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

public class SkyProviderCelestial extends IRenderHandler {
	
	private static final ResourceLocation planetTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png");
	private static final ResourceLocation flareTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/sunspike.png");
	private static final ResourceLocation nightTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night.png");
	private static final ResourceLocation digammaStar = new ResourceLocation(RefStrings.MODID, "textures/misc/space/star_digamma.png");
	private static final ResourceLocation ringTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/rings.png");

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private static final ResourceLocation ThatmoShield = new ResourceLocation("hbm:textures/particle/cens.png");
	private static final ResourceLocation flash = new ResourceLocation("hbm:textures/misc/space/flare.png");
	
	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	protected static final Shader planetShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));

	public static boolean displayListsInitialized = false;
	public static int glSkyList;
	public static int glSkyList2;

	public SkyProviderCelestial() {
		if (!displayListsInitialized) {
			initializeDisplayLists();
		}
	}

	private void initializeDisplayLists() {
		glSkyList = GLAllocation.generateDisplayLists(2);
		glSkyList2 = glSkyList + 1;

		final Tessellator tessellator = Tessellator.instance;
		final byte byte2 = 64;
		final int i = 256 / byte2 + 2;

		GL11.glNewList(glSkyList, GL11.GL_COMPILE);
		{
			float f = 16F;
			tessellator.startDrawingQuads();

			for(int j = -byte2 * i; j <= byte2 * i; j += byte2) {
				for(int l = -byte2 * i; l <= byte2 * i; l += byte2) {
					tessellator.addVertex(j + 0, f, l + 0);
					tessellator.addVertex(j + byte2, f, l + 0);
					tessellator.addVertex(j + byte2, f, l + byte2);
					tessellator.addVertex(j + 0, f, l + byte2);
				}
			}

			tessellator.draw();
		}
		GL11.glEndList();

		GL11.glNewList(glSkyList2, GL11.GL_COMPILE);
		{
			float f = -16F;
			tessellator.startDrawingQuads();

			for(int k = -byte2 * i; k <= byte2 * i; k += byte2) {
				for(int i1 = -byte2 * i; i1 <= byte2 * i; i1 += byte2) {
					tessellator.addVertex(k + byte2, f, i1 + 0);
					tessellator.addVertex(k + 0, f, i1 + 0);
					tessellator.addVertex(k + 0, f, i1 + byte2);
					tessellator.addVertex(k + byte2, f, i1 + byte2);
				}
			}

			tessellator.draw();
		}
		GL11.glEndList();

		displayListsInitialized = true;
	}

	private static int lastBrightestPixel = 0;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		float fogIntensity = 0;

		if(world.provider instanceof WorldProviderCelestial) {
			// Without mixins, we have to resort to some very wacky ways of checking that the lightmap needs to be updated
			// fortunately, thanks to torch flickering, we can just check to see if the brightest pixel has been modified
			if(lastBrightestPixel != mc.entityRenderer.lightmapColors[255] + mc.entityRenderer.lightmapColors[250]) {
				if(((WorldProviderCelestial)world.provider).updateLightmap(mc.entityRenderer.lightmapColors)) {
					mc.entityRenderer.lightmapTexture.updateDynamicTexture();
				}

				lastBrightestPixel = mc.entityRenderer.lightmapColors[255] + mc.entityRenderer.lightmapColors[250];
			}

			fogIntensity = ((WorldProviderCelestial) world.provider).fogDensity() * 30;
		}

		CelestialBody body = CelestialBody.getBody(world);
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

		boolean hasAtmosphere = atmosphere != null;

		float pressure = hasAtmosphere ? (float)atmosphere.getPressure() : 0.0F;
		float visibility = hasAtmosphere ? MathHelper.clamp_float(2.0F - pressure, 0.1F, 1.0F) : 1.0F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3 skyColor = world.getSkyColor(mc.renderViewEntity, partialTicks);

		float skyR = (float) skyColor.xCoord;
		float skyG = (float) skyColor.yCoord;
		float skyB = (float) skyColor.zCoord;

		// Diminish sky colour when leaving the atmosphere
		if(mc.renderViewEntity.posY > 300) {
			double curvature = MathHelper.clamp_float((800.0F - (float)mc.renderViewEntity.posY) / 500.0F, 0.0F, 1.0F);
			skyR *= curvature;
			skyG *= curvature;
			skyB *= curvature;
		}

		if(mc.gameSettings.anaglyph) {
			float[] anaglyphColor = applyAnaglyph(skyR, skyG, skyB);
			skyR = anaglyphColor[0];
			skyG = anaglyphColor[1];
			skyB = anaglyphColor[2];
		}

		float planetR = skyR;
		float planetG = skyG;
		float planetB = skyB;

		if(fogIntensity > 0.01F) {
			Vec3 fogColor = world.getFogColor(partialTicks);
			planetR = (float)BobMathUtil.clampedLerp(skyR, fogColor.xCoord, fogIntensity);
			planetG = (float)BobMathUtil.clampedLerp(skyG, fogColor.yCoord, fogIntensity);
			planetB = (float)BobMathUtil.clampedLerp(skyB, fogColor.zCoord, fogIntensity);
		}

		Vec3 planetTint = Vec3.createVectorHelper(planetR, planetG, planetB);

		Tessellator tessellator = Tessellator.instance;

		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(skyR, skyG, skyB);
		GL11.glCallList(glSkyList);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.disableStandardItemLighting();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		float starBrightness = world.getStarBrightness(partialTicks) * visibility;
		float celestialAngle = world.getCelestialAngle(partialTicks);

		// Handle any special per-body sunset rendering
		renderSunset(partialTicks, world, mc);

		renderStars(partialTicks, world, mc, starBrightness, celestialAngle, body.axialTilt);

		
		GL11.glPushMatrix();
		{

			GL11.glRotatef(body.axialTilt, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// Draw DIGAMMA STAR
			renderDigamma(partialTicks, world, mc, celestialAngle);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			double sunSize = SolarSystem.calculateSunSize(body);
			double coronaSize = sunSize * (3 - MathHelper.clamp_float(pressure, 0.0F, 1.0F));

			renderSun(partialTicks, world, mc, sunSize, coronaSize, visibility, pressure);
			
			float blendAmount = hasAtmosphere ? MathHelper.clamp_float(1 - world.getSunBrightnessFactor(partialTicks), 0.25F, 1F) : 1F;

			double longitude = 0;
			CelestialBody tidalLockedBody = body.tidallyLockedTo != null ? CelestialBody.getBody(body.tidallyLockedTo) : null;

			if(tidalLockedBody != null) {
				longitude = SolarSystem.calculateSingleAngle(world, partialTicks, body, tidalLockedBody) + celestialAngle * 360.0 + 60.0;
			}

			// Get our orrery of bodies
			List<AstroMetric> metrics = SolarSystem.calculateMetricsFromBody(world, partialTicks, longitude, body);
			
			renderCelestials(partialTicks, world, mc, metrics, celestialAngle, tidalLockedBody, planetTint, visibility, blendAmount, null, 32);

			GL11.glEnable(GL11.GL_BLEND);

			if(visibility > 0.2F) {
				// JEFF BOZOS WOULD LIKE TO KNOW YOUR LOCATION
				// ... to send you a pakedge :)))
				if(world.provider.dimensionId == 0) {
					renderSatellite(partialTicks, world, mc, celestialAngle, 1916169, new float[] { 1.0F, 0.534F, 0.385F });
				}
	
				// Light up the sky
				for(Map.Entry<Integer, Satellite> entry : SatelliteSavedData.getClientSats().entrySet()) {
					//System.out.println(entry.getValue().getInterp());
					
					if(entry.getValue() instanceof SatelliteWar) {
						GL11.glColor3f(skyR, skyG, skyB);
	
						GL11.glPushMatrix();

						GL11.glScaled(5, 5, 5);

						SatelliteWar war = (SatelliteWar) entry.getValue();
						//SatelliteWar war1 = SatelliteWar.clietnwar;
						//float fuick = entry.getValue().getInterp();


						
						GL11.glTranslated(-Math.round(entry.getKey() / 1000.0) + 30, -Math.round(entry.getKey() / 1000.0), 10); 


						GL11.glPushMatrix();						
						GL11.glTranslated(1, 5.5, 0); 
						GL11.glScaled(3, 3, 3);
						mc.renderEngine.bindTexture(flash);
						ResourceManager.plane.renderAll();
						
						GL11.glColor4d(1, 1, 1, 0.2);

						//System.out.println(entry.getValue().getInterp());
						mc.renderEngine.bindTexture(texture);
						ResourceManager.plane.renderAll();
						GL11.glPopMatrix();

						GL11.glPushMatrix();
						GL11.glTranslated(1, 5.5, 0); 
						BeamPronter.prontBeam(Vec3.createVectorHelper(0, 36 + entry.getValue().getInterp() , 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)0.2 * 0.2F, 0.3F );
						BeamPronter.prontBeam(Vec3.createVectorHelper(0, 36, 0), EnumWaveType.SPIRAL, EnumBeamType.SOLID, 0x202060, 0x202060, 0, 1, 0F, 6, (float)0.2 * 0.6F, 0.3F );
						BeamPronter.prontBeam(Vec3.createVectorHelper(0, 36, 0), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x202060, 0x202060, (int)(world.getTotalWorldTime() / 5) % 1000, 25, 0.2F, 6, (float)0.2 * 0.1F, 0.3F );
						GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_CULL_FACE);

						GL11.glPopMatrix();



						GL11.glEnable(GL11.GL_DEPTH_TEST); 
						GL11.glDisable(GL11.GL_BLEND);
						GL11.glRotated(-90, 0, 0, 1);

						GL11.glDepthRange(0.0, 1.0);

						//GL11.glDepthMask(false);
						
						mc.renderEngine.bindTexture(ResourceManager.sat_rail_tex);
						ResourceManager.sat_rail.renderAll();
						
						GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
						GL11.glEnable(GL11.GL_BLEND);
						GL11.glPopMatrix();
						
	
						
					}
					renderSatellite(partialTicks, world, mc, celestialAngle, entry.getKey(), entry.getValue().getColor());
					
				}
			}

		}
		GL11.glPopMatrix();
		render3DModel(partialTicks, world, mc);
	    CBT_War war = CelestialBody.getTrait(mc.getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().getMinecraft().theWorld, CBT_War.class);

	    if (war != null) {
	        for (int i = 0; i < war.getProjectiles().size(); i++) {
	            CBT_War.Projectile projectile = war.getProjectiles().get(i);
	            float flash = projectile.getFlashtime();
	            int anim = projectile.getAnimtime();
	            if (projectile.getTravel() <= 0) {
	                projectile.impact();
	                float alpd = 1.0F - Math.min(1.0F, flash / 100);


	                GL11.glPushMatrix(); 
	                render3DModel(partialTicks, world, mc);

	                GL11.glTranslated(projectile.getTranslateX() + 30, 55, projectile.getTranslateZ() + 50); 
	                GL11.glScaled(flash, flash, flash);
	                GL11.glRotated(90.0, -10.0, -1.0, 50.0);
	                GL11.glRotated(20.0, -0.0, -1.0, 1.0);

	                GL11.glColor4d(1, 1, 1, alpd);

	                mc.renderEngine.bindTexture(this.texture);
	                ResourceManager.plane.renderAll();
	                

	                GL11.glPopMatrix();
	                

	                GL11.glPushMatrix(); 

	                GL11.glTranslated(projectile.getTranslateX() + 30, 55, projectile.getTranslateZ() + 50); 
	                GL11.glScaled(flash * 0.4f, flash * 0.4f, flash * 0.4f);
	                GL11.glRotated(90.0, -10.0, -1.0, 50.0);
	                GL11.glRotated(20.0, -0.0, -1.0, 1.0);
	                GL11.glColor4d(1, 1, 1, alpd);
	                
	                mc.renderEngine.bindTexture(this.ThatmoShield);
	                ResourceManager.plane.renderAll();

	                GL11.glPopMatrix();
	            }
	        }
	    }
		
		if(body.hasRings) {
			GL11.glPushMatrix();
			{

				GL11.glRotatef(body.axialTilt - body.ringTilt, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -100, 0);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
	
				renderRings(partialTicks, world, mc, body.ringTilt, body.ringColor, 200, visibility);
	
			}
			GL11.glPopMatrix();
		}
		
		renderSpecialEffects(partialTicks, world, mc);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);
			
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);

		Vec3 pos = mc.thePlayer.getPosition(partialTicks);
		double heightAboveHorizon = pos.yCoord - world.getHorizon();

		if(heightAboveHorizon < 0.0D) {
			GL11.glPushMatrix();
			{

				GL11.glTranslatef(0.0F, 12.0F, 0.0F);
				GL11.glCallList(glSkyList2);

			}
			GL11.glPopMatrix();

			float f8 = 1.0F;
			float f9 = -((float) (heightAboveHorizon + 65.0D));
			float opposite = -f8;

			tessellator.startDrawingQuads();
			tessellator.setColorRGBA_I(0, 255);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, f9, f8);
			tessellator.addVertex(f8, f9, -f8);
			tessellator.addVertex(-f8, f9, -f8);
			tessellator.addVertex(-f8, f9, f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, -f8);
			tessellator.addVertex(-f8, opposite, f8);
			tessellator.addVertex(f8, opposite, f8);
			tessellator.addVertex(f8, opposite, -f8);
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GL11.glColor3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			GL11.glColor3f(skyR, skyG, skyB);
		}

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, -((float) (heightAboveHorizon - 16.0D)), 0.0F);
			GL11.glCallList(glSkyList2);

		}
		GL11.glPopMatrix();
		
		double sc = 1 / (pos.yCoord / 1000);
		double uvOffset = (pos.xCoord / 1024) % 1;
		GL11.glPushMatrix();
		{

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_BLEND);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			float sunBrightness = world.getSunBrightness(partialTicks);
	
			GL11.glColor4f(sunBrightness, sunBrightness, sunBrightness, ((float)pos.yCoord - 200.0F) / 300.0F);
			mc.renderEngine.bindTexture(body.texture);
			GL11.glRotated(180, 1, 0, 0);
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-115 * sc, 100.0D, -115 * sc, 0.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, -115 * sc, 1.0D + uvOffset, 0.0D);
			tessellator.addVertexWithUV(115 * sc, 100.0D, 115 * sc, 1.0D + uvOffset, 1.0D);
			tessellator.addVertexWithUV(-115 * sc, 100.0D, 115 * sc, 0.0D + uvOffset, 1.0D);
			tessellator.draw();

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_BLEND);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		}
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);

	}
	
	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc) {
		Tessellator tessellator = Tessellator.instance;
		
		float[] sunsetColor = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

		if(sunsetColor != null) {
			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
	
				tessellator.startDrawing(6);
				tessellator.setColorRGBA_F(anaglyphColor[0], anaglyphColor[1], anaglyphColor[2], sunsetColor[3]);
				tessellator.addVertex(0.0, 100.0, 0.0);
				tessellator.setColorRGBA_F(sunsetColor[0], sunsetColor[1], sunsetColor[2], 0.0F);
				byte segments = 16;
	
				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * 3.1415927F * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellator.addVertex((double)(sinAngle * 120.0F), (double)(cosAngle * 120.0F), (double)(-cosAngle * 40.0F * sunsetColor[3]));
				}
	
				tessellator.draw();

			}
			GL11.glPopMatrix();
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

	protected void renderStars(float partialTicks, WorldClient world, Minecraft mc, float starBrightness, float celestialAngle, float axialTilt) {
		Tessellator tessellator = Tessellator.instance;

		if(starBrightness > 0.0F) {
			GL11.glPushMatrix();
			{
				GL11.glRotatef(axialTilt, 1.0F, 0.0F, 0.0F);

				mc.renderEngine.bindTexture(nightTexture);
	
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
	
				float starBrightnessAlpha = starBrightness * 0.6f;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);
				
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
	
				GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);
				
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 4);
				
				GL11.glPushMatrix();
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 1);
				GL11.glPopMatrix();
				
				GL11.glPushMatrix();
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 0);
				GL11.glPopMatrix();
				
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 5);
				
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 2);
				
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 3);

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();
		}
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, double sunSize, double coronaSize, float visibility, float pressure) {
		Tessellator tessellator = Tessellator.instance;

		if(SolarSystem.kerbol.shader != null && SolarSystem.kerbol.hasTrait(CBT_Destroyed.class)) {
			// BLACK HOLE SUN
			// WON'T YOU COME
			// AND WASH AWAY THE RAIN

			Shader shader = SolarSystem.kerbol.shader;
			double shaderSize = sunSize * SolarSystem.kerbol.shaderScale;

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			shader.use();

			float time = ((float)world.getWorldTime() + partialTicks) / 20.0F;
			int textureUnit = 0;

			mc.renderEngine.bindTexture(noise);

			GL11.glPushMatrix();

			// Fix orbital plane
			GL11.glRotatef(-90.0F, 0, 1, 0);
	
			shader.setTime(time);
			shader.setTextureUnit(textureUnit);
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-shaderSize, 100.0D, -shaderSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(shaderSize, 100.0D, -shaderSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(shaderSize, 100.0D, shaderSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-shaderSize, 100.0D, shaderSize, 0.0D, 1.0D);
			tessellator.draw();
	
			shader.stop();

			GL11.glPopMatrix();

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		} else {
			// Some blanking to conceal the stars
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);

			tessellator.startDrawingQuads();
			tessellator.addVertex(-sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, -sunSize);
			tessellator.addVertex(sunSize, 99.9D, sunSize);
			tessellator.addVertex(-sunSize, 99.9D, sunSize);
			tessellator.draw();

			// Draw the MIGHTY SUN
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			mc.renderEngine.bindTexture(SolarSystem.kerbol.texture);
			CelestialBody body = CelestialBody.getBody(world);
			CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

			float[] sunColor = {1.0F, 1.0F, 1.0F};

			// Adjust the sun colour based on atmospheric composition
			if(atmosphere != null) {
				for(FluidEntry entry : atmosphere.fluids) {
					// Chlorines all redden the sun by absorbing blue and green
					if(entry.fluid == Fluids.TEKTOAIR
					|| entry.fluid == Fluids.CHLORINE
					|| entry.fluid == Fluids.CHLOROMETHANE
					|| entry.fluid == Fluids.RADIOSOLVENT
					|| entry.fluid == Fluids.CCL) {
						float absorption = MathHelper.clamp_float(1.0F - (float)entry.pressure * 0.5F, 0.0F, 1.0F);
						sunColor[1] *= absorption;
						sunColor[2] *= absorption;
					}
				}
			}

			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], visibility);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-sunSize, 100.0D, -sunSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, -sunSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(sunSize, 100.0D, sunSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-sunSize, 100.0D, sunSize, 0.0D, 1.0D);
			tessellator.draw();

			// Draw a big ol' spiky flare! Less so when there is an atmosphere
			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], 1 - MathHelper.clamp_float(pressure, 0.0F, 1.0F) * 0.75F);
			mc.renderEngine.bindTexture(flareTexture);

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-coronaSize, 100.0D, -coronaSize, 0.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 100.0D, -coronaSize, 1.0D, 0.0D);
			tessellator.addVertexWithUV(coronaSize, 100.0D, coronaSize, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-coronaSize, 100.0D, coronaSize, 0.0D, 1.0D);
			tessellator.draw();
		}
	}

	protected void renderCelestials(float partialTicks, WorldClient world, Minecraft mc, List<AstroMetric> metrics, float celestialAngle, CelestialBody tidalLockedBody, Vec3 planetTint, float visibility, float blendAmount, CelestialBody orbiting, float maxSize) {
		Tessellator tessellator = Tessellator.instance;
		float blendDarken = 0.1F;

		double transitionMinSize = 0.1D;
		double transitionMaxSize = 0.5D;

		for(AstroMetric metric : metrics) {

			// Ignore self
			if(metric.distance == 0)
				continue;
			
			boolean orbitingThis = metric.body == orbiting;

			double uvOffset = orbitingThis ? 1 - ((((double)world.getWorldTime() + partialTicks) / 1024) % 1) : 0;
			float axialTilt = orbitingThis ? 0 : metric.body.axialTilt;

			GL11.glPushMatrix();
			{

				double size = MathHelper.clamp_double(metric.apparentSize, 0, maxSize);
				boolean renderPoint = size < transitionMaxSize;
				boolean renderBody = size > transitionMinSize;

				if(metric.body == tidalLockedBody) {
					GL11.glRotated(celestialAngle * -360.0 - 60.0, 1.0, 0.0, 0.0);
				} else {
					GL11.glRotated(metric.angle, 1.0, 0.0, 0.0);
				}
				GL11.glRotatef(axialTilt + 90.0F, 0.0F, 1.0F, 0.0F);

				if(renderBody) {
					// Draw the back half of the ring (obscured by body)
					if(metric.body.hasRings) {
						GL11.glPushMatrix();
						{

							GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
							mc.renderEngine.bindTexture(ringTexture);

							GL11.glDisable(GL11.GL_CULL_FACE);
	
							double ringSize = size * metric.body.ringSize;
	
							GL11.glTranslatef(0.0F, 100.0F, 0.0F);
							GL11.glRotated(-metric.angle, 0, 0, 1);
							GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
							GL11.glRotated(metric.angle, 0, 1, 0);
	
							tessellator.startDrawingQuads();
							tessellator.addVertexWithUV(-ringSize, 0, -ringSize, 0.0D, 0.0D);
							tessellator.addVertexWithUV(ringSize, 0, -ringSize, 1.0D, 0.0D);
							tessellator.addVertexWithUV(ringSize, 0, 0, 1.0D, 0.5D);
							tessellator.addVertexWithUV(-ringSize, 0, 0, 0.0D, 0.5D);
							tessellator.draw();

							GL11.glEnable(GL11.GL_CULL_FACE);

						}
						GL11.glPopMatrix();
					}

					GL11.glDisable(GL11.GL_BLEND);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);
					mc.renderEngine.bindTexture(metric.body.texture);
					
					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D + uvOffset, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D + uvOffset, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, size, 1.0D + uvOffset, 1.0D);
					tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D + uvOffset, 1.0D);
					tessellator.draw();


					GL11.glEnable(GL11.GL_BLEND);
					
					// Draw a shader on top to render celestial phase
					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					planetShader.use();
					planetShader.setTime((float)-metric.phase);
					planetShader.setOffset((float)uvOffset);
					
					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
					tessellator.draw();

					planetShader.stop();


					GL11.glDisable(GL11.GL_TEXTURE_2D);
					
					// Draw another layer on top to blend with the atmosphere
					GL11.glColor4d(planetTint.xCoord - blendDarken, planetTint.yCoord - blendDarken, planetTint.zCoord - blendDarken, (1 - blendAmount * visibility));
					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
					tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
					tessellator.draw();

					GL11.glEnable(GL11.GL_TEXTURE_2D);


					// Draw the front half of the ring (unobscured)
					if(metric.body.hasRings) {
						GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
						mc.renderEngine.bindTexture(ringTexture);

						double ringSize = size * metric.body.ringSize;

						GL11.glDisable(GL11.GL_CULL_FACE);

						GL11.glTranslatef(0.0F, 100.0F, 0.0F);
						GL11.glRotated(-metric.angle, 0, 0, 1);
						GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
						GL11.glRotated(metric.angle, 0, 1, 0);

						tessellator.startDrawingQuads();
						tessellator.addVertexWithUV(-ringSize, 0, 0, 0.0D, 0.5D);
						tessellator.addVertexWithUV(ringSize, 0, 0, 1.0D, 0.5D);
						tessellator.addVertexWithUV(ringSize, 0, ringSize, 1.0D, 1.0D);
						tessellator.addVertexWithUV(-ringSize, 0, ringSize, 0.0D, 1.0D);
						tessellator.draw();

						GL11.glEnable(GL11.GL_CULL_FACE);
					}
				}

				if(renderPoint) {
					float alpha = MathHelper.clamp_float((float)size * 100.0F, 0.0F, 1.0F);
					alpha *= 1 - BobMathUtil.remap01_clamp((float)size, (float)transitionMinSize, (float)transitionMaxSize);
					GL11.glColor4f(metric.body.color[0], metric.body.color[1], metric.body.color[2], alpha * visibility);
					mc.renderEngine.bindTexture(planetTexture);
					
					tessellator.startDrawingQuads();
					tessellator.addVertexWithUV(-1.0D, 100.0D, -1.0D, 0.0D, 0.0D);
					tessellator.addVertexWithUV(1.0D, 100.0D, -1.0D, 1.0D, 0.0D);
					tessellator.addVertexWithUV(1.0D, 100.0D, 1.0D, 1.0D, 1.0D);
					tessellator.addVertexWithUV(-1.0D, 100.0D, 1.0D, 0.0D, 1.0D);
					tessellator.draw();
				}

			}
			GL11.glPopMatrix();
		}
	}

	protected void renderRings(float partialTicks, WorldClient world, Minecraft mc, float ringTilt, float[] ringColor, float ringSize, float visibility) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2], visibility);
		mc.renderEngine.bindTexture(ringTexture);

		double offset = -20.0D;

		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(offset, -ringSize, -ringSize, 0.0D, 0.0D);
		tessellator.addVertexWithUV(offset, ringSize, -ringSize, 1.0D, 0.0D);
		tessellator.addVertexWithUV(offset, ringSize, ringSize, 1.0D, 1.0D);
		tessellator.addVertexWithUV(offset, -ringSize, ringSize, 0.0D, 1.0D);
		tessellator.draw();
	}

	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle) {
		Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		{

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			float brightness = (float) Math.sin(celestialAngle * Math.PI);
			brightness *= brightness;
			GL11.glColor4f(brightness, brightness, brightness, brightness);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);

			mc.renderEngine.bindTexture(digammaStar);

			float digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().thePlayer);
			float var12 = 1F * (1 + digamma * 0.25F);
			double dist = 100D - digamma * 2.5;

			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-var12, dist, -var12, 0.0D, 0.0D);
			tessellator.addVertexWithUV(var12, dist, -var12, 0.0D, 1.0D);
			tessellator.addVertexWithUV(var12, dist, var12, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-var12, dist, var12, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	// Does anyone even play with 3D glasses anymore?
	protected float[] applyAnaglyph(float... colors) {
		float r = (colors[0] * 30.0F + colors[1] * 59.0F + colors[2] * 11.0F) / 100.0F;
		float g = (colors[0] * 30.0F + colors[1] * 70.0F) / 100.0F;
		float b = (colors[0] * 30.0F + colors[2] * 70.0F) / 100.0F;

		return new float[] { r, g, b };
	}

	protected void renderSatellite(float partialTicks, WorldClient world, Minecraft mc, float celestialAngle, long seed, float[] color) {
		Tessellator tessellator = Tessellator.instance;

		double ticks = (double)(System.currentTimeMillis() % (600 * 50)) / 50;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(celestialAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F + (float)(seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float)(seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float)(seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GL11.glRotated((ticks / 600.0D) * 360.0D, 1.0F, 0.0F, 0.0F);
			
			GL11.glColor4f(color[0], color[1], color[2], 1F);
			
			mc.renderEngine.bindTexture(planetTexture);
			
			float size = 0.5F;
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-size, 100.0, -size, 0.0D, 0.0D);
			tessellator.addVertexWithUV(size, 100.0, -size, 0.0D, 1.0D);
			tessellator.addVertexWithUV(size, 100.0, size, 1.0D, 1.0D);
			tessellator.addVertexWithUV(-size, 100.0, size, 1.0D, 0.0D);
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}
	
	// is just drawing a big cube with UVs prepared to draw a gradient
	private void renderSkyboxSide(Tessellator tessellator, int side) {
		double u = side % 3 / 3.0D;
		double v = side / 3 / 2.0D;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, u, v);
		tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, u, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, u + 0.3333333333333333D, v + 0.5D);
		tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, u + 0.3333333333333333D, v);
		tessellator.draw();
	}

	protected void renderSpecialEffects(float partialTicks, WorldClient world, Minecraft mc) {

	}
	
	protected void render3DModel(float partialTicks, WorldClient world, Minecraft mc) {
	
	}

}