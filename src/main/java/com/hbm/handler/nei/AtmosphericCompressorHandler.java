package com.hbm.handler.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawStringC;

import java.util.HashMap;
import java.util.Map.Entry;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Atmosphere.FluidEntry;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.Clock;
import com.hbm.util.InventoryUtil;
import com.hbm.util.i18n.I18nUtil;

import codechicken.nei.NEIServerUtils;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

public class AtmosphericCompressorHandler extends NEIUniversalHandler {

	public AtmosphericCompressorHandler() {
		super("Atmosphere Extraction", ModBlocks.machine_atmo_vent, getRecipes());
	}

	@Override
	public String getKey() {
		return "ntmAtmoCompressor";
	}

	public static HashMap<Object, ItemStack[]> getRecipes() {
		HashMap<Object, ItemStack[]> map = new HashMap<>();

		for(SolarSystem.Body bodyEnum : SolarSystem.Body.values()) {
			CelestialBody body = bodyEnum.getBody();
			if(body == null) continue;

			CBT_Atmosphere atmosphere = body.getDefaultTrait(CBT_Atmosphere.class);
			if(atmosphere == null) continue;

			ItemStack[] outputs = new ItemStack[atmosphere.fluids.size()];
			for(int i = 0; i < outputs.length; i++) {
				FluidEntry entry = atmosphere.fluids.get(i);
				outputs[i] = ItemFluidIcon.make(entry.fluid, entry.pressure);
			}

			map.put(body, outputs);
		}

		return map;
	}

	private HashMap<Integer, CelestialBody> recipeInputCache = new HashMap<>();

	@Override
	public void drawBackground(int recipe) {
		super.drawBackground(recipe);

		CelestialBody body = recipeInputCache.get(recipe);
		changeTexture(body.texture);

		double uvOffset = (double)(Clock.get_ms() % 4000) / 4000;

		double minX = 32;
		double minY = 12;
		double maxX = minX + 32;
		double maxY = minY + 32;

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(minX, maxY, 0, 0 + uvOffset, 1);
		tessellator.addVertexWithUV(maxX, maxY, 0, 1 + uvOffset, 1);
		tessellator.addVertexWithUV(maxX, minY, 0, 1 + uvOffset, 0);
		tessellator.addVertexWithUV(minX, minY, 0, 0 + uvOffset, 0);
		tessellator.draw();

		drawStringC(I18nUtil.resolveKey("body." + body.name), 16, 48, 64, 12, 0x000000, false);
	}


	// Gotta skip trying to read the recipe inputs because they are not ItemStacks

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {

		if(outputId.equals(getKey())) {

			outer: for(Entry<Object, Object> recipe : recipes.entrySet()) {
				ItemStack[][] ins = new ItemStack[0][0];
				ItemStack[][] outs = InventoryUtil.extractObject(recipe.getValue());

				for(ItemStack[] array : outs) for(ItemStack stack : array) if(stack.getItem() == ModItems.item_secret) continue outer;

				recipeInputCache.put(arecipes.size(), (CelestialBody) recipe.getKey());
				arecipes.add(new RecipeSet(ins, outs, recipe.getKey()));
			}

		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {

		outer: for(Entry<Object, Object> recipe : recipes.entrySet()) {
			ItemStack[][] ins = new ItemStack[0][0];
			ItemStack[][] outs = InventoryUtil.extractObject(recipe.getValue());

			for(ItemStack[] array : outs) for(ItemStack stack : array) if(stack.getItem() == ModItems.item_secret) continue outer;

			match:
			for(ItemStack[] array : outs) {
				for(ItemStack stack : array) {
					if(NEIServerUtils.areStacksSameTypeCrafting(stack, result)) {
						recipeInputCache.put(arecipes.size(), (CelestialBody) recipe.getKey());
						arecipes.add(new RecipeSet(ins, outs, recipe.getKey()));
						break match;
					}
				}
			}
		}
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		// nop
	}

}
