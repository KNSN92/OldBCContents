package com.knsn92.old_bc_contents.gui;

import buildcraft.core.lib.gui.BuildCraftContainer;
import buildcraft.core.proxy.CoreProxy;
import com.knsn92.old_bc_contents.OldBCContents;
import com.knsn92.old_bc_contents.tile.TileOldAutoWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;
import net.minecraft.world.World;

public class ContainerOldAutoWorkbench extends BuildCraftContainer {

    private static class SlotAutoCrafting extends Slot {

        private final EntityPlayer thePlayer;
        private final TileOldAutoWorkbench tile;

        public SlotAutoCrafting(EntityPlayer player, TileOldAutoWorkbench tile, int i, int j, int k) {
            super(tile.displayCraftResult, i, j, k);
            this.thePlayer = player;
            this.tile = tile;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack decrStackSize(int p_75209_1_) {
            return this.tile.displayCraftResult.getStackInSlot(0);
        }

        @Override
        public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
            if(tile.isRemainCraftResult()) {
                tile.craftResult.setInventorySlotContents(0, null);
            } else {
                if(stack != null) {
                    CoreProxy.proxy.onCraftingPickup(this.thePlayer.worldObj, this.thePlayer, stack);

                    if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table)) {
                        this.thePlayer.addStat(AchievementList.buildWorkBench, 1);
                    }
                    if (stack.getItem() instanceof ItemPickaxe) {
                        this.thePlayer.addStat(AchievementList.buildPickaxe, 1);
                    }
                    if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace)) {
                        this.thePlayer.addStat(AchievementList.buildFurnace, 1);
                    }
                    if (stack.getItem() instanceof ItemHoe) {
                        this.thePlayer.addStat(AchievementList.buildHoe, 1);
                    }
                    if (stack.getItem() == Items.bread) {
                        this.thePlayer.addStat(AchievementList.makeBread, 1);
                    }
                    if (stack.getItem() == Items.cake) {
                        this.thePlayer.addStat(AchievementList.bakeCake, 1);
                    }
                    if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe) stack.getItem()).func_150913_i() != Item.ToolMaterial.WOOD) {
                        this.thePlayer.addStat(AchievementList.buildBetterPickaxe, 1);
                    }
                    if (stack.getItem() instanceof ItemSword) {
                        this.thePlayer.addStat(AchievementList.buildSword, 1);
                    }
                    if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table)) {
                        this.thePlayer.addStat(AchievementList.enchantments, 1);
                    }
                    if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf)) {
                        this.thePlayer.addStat(AchievementList.bookcase, 1);
                    }
                }
                tile.craftManually();
            }
            tile.refreshDisplayCraftResult();
        }
    }


    private final TileOldAutoWorkbench tile;

    private final World worldObj;

    private final int posX;
    private final int posY;
    private final int posZ;

    public ContainerOldAutoWorkbench(InventoryPlayer invPlayer, TileOldAutoWorkbench t, World worldObj, int posX, int posY, int posZ)
    {
        super(t.getSizeInventory());
        this.tile = t;
        this.worldObj = worldObj;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;

        int l;
        int i1;

        for (l = 0; l < 3; ++l)
        {
            for (i1 = 0; i1 < 3; ++i1)
            {
                this.addSlotToContainer(new Slot(tile.craftMatrix, i1 + l * 3, 30 + i1 * 18, 17 + l * 18));
            }
        }

        this.addSlotToContainer(new SlotAutoCrafting(invPlayer.player, tile, 0, 124, 35));

        for (l = 0; l < 3; ++l)
        {
            for (i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(invPlayer, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }

        for (l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(invPlayer, l, 8 + l * 18, 142));
        }

    }

    public final void onCraftMatrixChanged(IInventory inv) {
        this.tile.refreshDisplayCraftResult();
        this.tile.markDirty();
        super.onCraftMatrixChanged(inv);
    }

    public boolean canInteractWith(EntityPlayer p_75145_1_)
    {
        return this.worldObj.getBlock(this.posX, this.posY, this.posZ) == OldBCContents.blockOldAutoWorkbench &&
                p_75145_1_.getDistanceSq((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this, or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slotIndex == 0) {
                if (!this.mergeItemStack(itemstack1, 10, 46, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (slotIndex >= 10 && slotIndex < 37) {
                if (!this.mergeItemStack(itemstack1, 37, 46, false)) {
                    return null;
                }
            } else if (slotIndex >= 37 && slotIndex < 46) {
                if (!this.mergeItemStack(itemstack1, 10, 37, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 10, 46, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }

    public boolean func_94530_a(ItemStack stack, Slot slot)
    {
        return slot.inventory != tile.displayCraftResult && super.func_94530_a(stack, slot);
    }

}
