package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerHydroponic extends ContainerBase {

	public ContainerHydroponic(InventoryPlayer invPlayer, IInventory inv) {
		super(invPlayer, inv);

		for(int i = 0; i < 3; i++) {
			addSlotToContainer(new Slot(inv, i, 8, 54 + i * 18));
		}
		for(int i = 0; i < 3; i++) {
			addSlotToContainer(new Slot(inv, i + 3, 26, 54 + i * 18));
		}

		playerInv(invPlayer, 8, 140, 198);
	}

}
