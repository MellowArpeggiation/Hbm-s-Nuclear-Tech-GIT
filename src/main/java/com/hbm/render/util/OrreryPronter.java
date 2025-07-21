package com.hbm.render.util;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SolarSystem;
import com.hbm.dim.SolarSystem.OrreryMetric;
import com.hbm.util.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class OrreryPronter {

	public static void render(Minecraft mc, World world, float partialTicks) {
		Tessellator tessellator = Tessellator.instance;
		List<OrreryMetric> metrics = SolarSystem.calculatePositionsOrrery(world, partialTicks);

		for(OrreryMetric metric : metrics) {
			GL11.glPushMatrix();
			{

				GL11.glTranslated(metric.position.xCoord, metric.position.zCoord, metric.position.yCoord);
				GL11.glScaled(metric.body.radiusKm / 600, metric.body.radiusKm / 600, metric.body.radiusKm / 600);

				mc.renderEngine.bindTexture(metric.body.texture);
				tessellator.disableColor();
				RenderUtil.renderBlock(tessellator);

			}
			GL11.glPopMatrix();

			int color = 0xff8800;

			tessellator.startDrawing(3);
			tessellator.setColorOpaque_I(color);

			for(int i = 1; i < metric.orbitalPath.length; i++) {
				Vec3 from = metric.orbitalPath[i-1];
				Vec3 to = metric.orbitalPath[i];

				tessellator.addVertex(from.xCoord, from.zCoord, from.yCoord);
				tessellator.addVertex(to.xCoord, to.zCoord, to.yCoord);
			}

			Vec3 first = metric.orbitalPath[0];
			Vec3 last = metric.orbitalPath[metric.orbitalPath.length - 1];

			tessellator.addVertex(last.xCoord, last.zCoord, last.yCoord);
			tessellator.addVertex(first.xCoord, first.zCoord, first.yCoord);

			tessellator.draw();
		}
	}

}