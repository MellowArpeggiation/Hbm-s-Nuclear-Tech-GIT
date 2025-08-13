package com.hbm.items.armor;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.handler.ArmorModHandler;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemModHeavyBoots extends ItemArmorMod {

	public ItemModHeavyBoots() {
		super(ArmorModHandler.boots_only, false, false, false, true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(EnumChatFormatting.BLUE + "Increases fall speed in low gravity");
		list.add(EnumChatFormatting.BLUE + "Activated by crouching");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
		list.add(EnumChatFormatting.GOLD + "Can be worn on its own!");
		list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "We take no responsibility for any deaths that may");
		list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "occur while using these boots without a space suit.");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(EnumChatFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (" + I18nUtil.resolveKey("armor.fastFall") + ")");
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		// if crouching in air, apply extra gravity until we match the overworld
		if(player.isSneaking() && !player.onGround && !player.isInWater()) {
			float gravity = CelestialBody.getGravity(player);
			if(gravity > 1.5F) return;
			if(gravity == 0) return;
			if(gravity < 0.2F) gravity = 0.2F;

			player.motionY /= 0.98F;
			player.motionY += (gravity / 20F);
			player.motionY -= (AstronomyUtil.STANDARD_GRAVITY / 20F);
			player.motionY *= 0.98F;
		}
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		if(!(entity instanceof EntityPlayer))
			return;

		ItemStack boots = ArmorModHandler.pryMods(armor)[ArmorModHandler.boots_only];

		if(boots == null)
			return;

		onArmorTick(entity.worldObj, (EntityPlayer)entity, boots);
		ArmorModHandler.applyMod(armor, boots);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return armorType == 3;
	}

}
