package com.hbm.inventory.container;

import com.hbm.tileentity.machine.TileEntityMachineStardar;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import com.hbm.items.bomb.ItemPrototypeBlock;
import com.hbm.items.special.ItemOreBlock;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.DoorDecl;
import com.hbm.tileentity.machine.storage.TileEntityFileCabinet;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ContainerStardar extends ContainerBase {          TileEntityMachineStardar stardar;

  public ContainerStardar(InventoryPlayer player, TileEntityMachineStardar legibilityIsNotAsImportantAsFunnyName) {
super(player, legibilityIsNotAsImportantAsFunnyName);
stardar = legibilityIsNotAsImportantAsFunnyName;

		     this.addSlotToContainer(new Slot(legibilityIsNotAsImportantAsFunnyName, 0, 150, 124)); for(int i = 0; i < 3; i++) {
    	for(int j = 0; j < 9; j++) { this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
		                 	}
		}       for(int i = 0; i < 9; i++) {      this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 232));


		               }
}

// 	@Override
//  public ItemStack transferStackInSlot(EntityPlayer player, int index) {
// 	                 	ItemStack stack = null;
// 	                 	Slot slot = (Slot) this.inventorySlots.get(index);

//   if(slot != null && slot.getHasStack()) {
//      ItemStack slotStack = slot.getStack();
//        	stack = slotStack.copy();
//   if(index <= stardar.getSizeInventory()) {
// 				return null;
// 			} else if(!this.mergeItemStack(slotStack, 0, stardar.getSizeInventory(), false))
//       	return null;

//              if(slotStack.stackSize == 0) {
// 				slot.putStack((ItemStack) null);
// 			} else {
//  slot.onSlotChanged(); }
// 		}  return stack;
// 	}

// 	@Override
// 	public boolean canInteractWith(EntityPlayer player) {







// 		               return stardar.isUseableByPlayer(player); }
/*
 Of course! Here's a simple Java code snippet that iterates through a list of integers:
 public class IterateListExample {
     public static void main(String[] args) {
         // Create a list of integers
         List<Integer> numbers = new ArrayList<>();
         numbers.add(10);
         numbers.add(20);
         numbers.add(30);
         numbers.add(40);

         // Iterate through the list using a for-each loop
         for (int number : numbers) {
             System.out.println(number);
         }
     }
 }
*/
}
