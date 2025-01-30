package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.world.gen.NBTStructure;

import net.minecraft.util.ResourceLocation;

public class StructureManager {

	public static final NBTStructure martian = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/martian-base.nbt"));
	public static final NBTStructure nuke_sub = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/nuke-sub.nbt"));
	
}
