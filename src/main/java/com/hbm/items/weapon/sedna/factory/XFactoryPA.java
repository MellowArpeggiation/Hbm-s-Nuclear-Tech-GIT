package com.hbm.items.weapon.sedna.factory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.hbm.items.ModItems;
import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.GunState;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.AnimationEnums.GunAnimation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class XFactoryPA {

	public static void init() {

		ModItems.gun_pa_melee = new ItemGunMelee(WeaponQuality.UTILITY, new GunConfig()
				.draw(10).inspect(55).crosshair(Crosshair.NONE)
				.rec(new Receiver(0)
						.dmg(10F).delay(20).jam(0)
						.offset(1, -0.0625 * 2.5, -0.25D))
				.pp(LAMBDA_CLICK_PRIMARY).ps(LAMBDA_CLICK_SENONDARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
				.anim(LAMBDA_MELEE_ANIMS).orchestra(ORCHESTRA)
				).setUnlocalizedName("gun_pa_melee");
	}

	public static BiConsumer<ItemStack, LambdaContext> ORCHESTRA = (stack, ctx) -> {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.orchestra(stack, ctx);
	};
	
	public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MELEE_ANIMS = (stack, type) -> {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) return component.playAnim(stack, type);
		return null;
	};
	
	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_PRIMARY = (stack, ctx) -> {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.clickPrimary(stack, ctx);
	};
	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_CLICK_SENONDARY = (stack, ctx) -> {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.clickSecondary(stack, ctx);
	};
	
	public static void doSwing(ItemStack stack, LambdaContext ctx, GunAnimation anim, int cooldown) {

		EntityPlayer player = ctx.getPlayer();
		int index = ctx.configIndex;
		GunState state = ItemGunBaseNT.getState(stack, index);

		if(state == GunState.IDLE) {
			ItemGunBaseNT.playAnimation(player, stack, anim, ctx.configIndex);
			ItemGunBaseNT.setState(stack, index, GunState.COOLDOWN);
			ItemGunBaseNT.setTimer(stack, index, cooldown);
		}
	}
	
	public static class ItemGunMelee extends ItemGunBaseNT {

		public ItemGunMelee(WeaponQuality quality, GunConfig... cfg) {
			super(quality, cfg);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }
	}
}
