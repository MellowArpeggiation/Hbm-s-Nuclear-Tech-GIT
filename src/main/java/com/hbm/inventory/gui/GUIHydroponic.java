package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerHydroponic;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityHydroponic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIHydroponic extends GuiInfoContainer {

	private static final ResourceLocation texture = new ResourceLocation( RefStrings.MODID + ":textures/gui/machine/gui_hydrobay.png");

	TileEntityHydroponic hydro;

	public GUIHydroponic(InventoryPlayer invPlayer, TileEntityHydroponic hydro) {
		super(new ContainerHydroponic(invPlayer, hydro));
		this.hydro = hydro;

		xSize = 176;
		ySize = 222;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

}
