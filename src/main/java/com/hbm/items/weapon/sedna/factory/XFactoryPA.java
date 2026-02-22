package com.hbm.items.weapon.sedna.factory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.WeaponQuality;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.AnimationEnums.GunAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;

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
						.offset(1, -0.0625 * 2.5, -0.25D)
						.canFire(LAMBDA_MELEE_CAN_FIRE).fire(LAMBDA_MELEE_FIRE))
				.pp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).rp(Lego.LAMBDA_STANDARD_CLICK_PRIMARY).decider(GunStateDecider.LAMBDA_STANDARD_DECIDER)
				.anim(LAMBDA_MELEE_ANIMS).orchestra(Orchestras.ORCHESTRA_DRILL)
				).setUnlocalizedName("gun_pa_melee");
	}
	
	public static BiFunction<ItemStack, GunAnimation, BusAnimation> LAMBDA_MELEE_ANIMS = (stack, type) -> {
		if(type == GunAnimation.EQUIP) return new BusAnimation()
			.addBus("EQUIP", new BusAnimationSequence().setPos(-1, 0, 0).addPos(0, 0, 0, 750, IType.SIN_DOWN));
		
		return new BusAnimation()
				.addBus("SWING", new BusAnimationSequence().setPos(-1, 0, 0).addPos(0, 0, 0, 750, IType.SIN_DOWN));
	};
	
	public static BiFunction<ItemStack, LambdaContext, Boolean> LAMBDA_MELEE_CAN_FIRE = (stack, ctx) -> { return true; };
	
	public static BiConsumer<ItemStack, LambdaContext> LAMBDA_MELEE_FIRE = (stack, ctx) -> {
		EntityPlayer player = ctx.getPlayer();
		ItemGunBaseNT.playAnimation(player, stack, ItemGunBaseNT.getPrimary(stack, 0) ? GunAnimation.CYCLE : GunAnimation.ALT_CYCLE, ctx.configIndex);
	};
	
	public static class ItemGunMelee extends ItemGunBaseNT {

		public ItemGunMelee(WeaponQuality quality, GunConfig... cfg) {
			super(quality, cfg);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }
	}
}
