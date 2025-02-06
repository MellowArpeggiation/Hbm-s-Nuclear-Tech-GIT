package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.world.gen.NBTStructure;

import net.minecraft.util.ResourceLocation;

public class StructureManager {

	public static final NBTStructure martian = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/martian-base.nbt"));
	public static final NBTStructure duna_comms = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/duna-comms.nbt"));

	public static final NBTStructure nuke_sub = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/nuke-sub.nbt"));

	public static final NBTStructure vertibird = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/vertibird.nbt"));
	public static final NBTStructure crashed_vertibird = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/crashed-vertibird.nbt"));

	public static final NBTStructure trenches = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/trenches.nbt"));

	// public static final NBTStructure test_rot = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/test-rot.nbt"));
	public static final NBTStructure test_jigsaw = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/test-jigsaw.nbt"));
	public static final NBTStructure test_jigsaw_core = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/test-jigsaw-core.nbt"));
	public static final NBTStructure test_jigsaw_hall = new NBTStructure(new ResourceLocation(RefStrings.MODID, "structures/test-jigsaw-hall.nbt"));

}
