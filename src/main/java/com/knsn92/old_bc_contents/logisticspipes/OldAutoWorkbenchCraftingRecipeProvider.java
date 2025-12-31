package com.knsn92.old_bc_contents.logisticspipes;

import com.knsn92.old_bc_contents.tile.TileOldAutoWorkbench;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class OldAutoWorkbenchCraftingRecipeProvider implements ICraftingRecipeProvider {

    @Override
    public boolean canOpenGui(TileEntity tile) {
        return tile instanceof TileOldAutoWorkbench;
    }

    @Override
    public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
        if (!(tile instanceof TileOldAutoWorkbench)) {
            return false;
        } else {
            TileOldAutoWorkbench bench = (TileOldAutoWorkbench)tile;
            ItemStack result = bench.getCraftResult();
            if (result == null) {
                return false;
            } else {
                inventory.setInventorySlotContents(9, result);

                for(int i = 0; i < bench.craftMatrix.getSizeInventory(); ++i) {
                    ItemStack newStack = bench.craftMatrix.getStackInSlot(i) == null ? null : bench.craftMatrix.getStackInSlot(i).copy();
                    if (newStack != null && newStack.stackSize > 1) {
                        newStack.stackSize = 1;
                    }

                    inventory.setInventorySlotContents(i, newStack);
                }

                inventory.compact_first(9);
                return true;
            }
        }
    }
}
