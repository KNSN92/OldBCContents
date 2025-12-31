package com.knsn92.old_bc_contents.tile;

import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.gui.ContainerDummy;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.utils.CraftingUtils;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TileOldAutoWorkbench extends TileBuildCraft implements ISidedInventory {
    
    public static final int SLOT_RESULT = 9;
    private static final int[] SLOTS = Utils.createSlotArray(0, 11);

    public IRecipe recipeCache = null;

    public InventoryCrafting craftMatrix;
    public IInventory craftResult;
    public IInventory displayCraftResult;

    private static class LocalContainer extends ContainerDummy {
        TileOldAutoWorkbench tile;

        public LocalContainer(TileOldAutoWorkbench t) {
            this.tile = t;
        }

        @Override
        public final void onCraftMatrixChanged(IInventory inv) {
            super.onCraftMatrixChanged(inv);
            tile.refreshDisplayCraftResult();
            tile.markDirty();
        }
    }

    public void refreshDisplayCraftResult() {
        ItemStack stack;
        if(this.isRemainCraftResult()) {
            stack = this.craftResult.getStackInSlot(0);
        }else {
            stack = this.getCraftResult();
        }
        this.displayCraftResult.setInventorySlotContents(0, stack);
        if(this.hasWorldObj()) {
            this.getWorldObj().markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public ItemStack getCraftResult() {
        if(this.recipeCache == null || !this.recipeCache.matches(this.craftMatrix, this.worldObj)) {
            this.recipeCache = CraftingUtils.findMatchingRecipe(this.craftMatrix, this.worldObj);
        }
        return this.recipeCache == null ? null : this.recipeCache.getCraftingResult(this.craftMatrix);
    }

    public boolean isRemainCraftResult() {
        return this.craftResult.getStackInSlot(0) != null;
    }

    public TileOldAutoWorkbench() {
        this.craftMatrix = new InventoryCrafting(new LocalContainer(this), 3, 3);
        this.craftResult = new InventoryBasic("", false, 1);
        this.displayCraftResult = new InventoryBasic("", false, 1);
    }


    @Override
    public int[] getAccessibleSlotsFromSide(int slot) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return slot < SLOT_RESULT;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == SLOT_RESULT;
    }

    @Override
    public int getSizeInventory() {
        return 11;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        ItemStack stack = null;
        if(slot < SLOT_RESULT) {
            stack = this.craftMatrix.getStackInSlot(slot);
        }else if(slot == SLOT_RESULT) {
            if(this.isRemainCraftResult()) {
                stack = this.craftResult.getStackInSlot(0);
            } else if(this.canCraft()) {
                stack = this.getCraftResult();
            }
        }
        return stack == null ? null : stack.copy();
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack;
        if(slot < SLOT_RESULT) {
            stack = this.craftMatrix.decrStackSize(slot, count);
        }else if(slot == SLOT_RESULT) {
            if(!this.isRemainCraftResult() && this.canCraft()) {
                this.craftResult.setInventorySlotContents(0, this.craft());
            }
            stack = this.craftResult.decrStackSize(0, count);
        }else {
            return null;
        }
        this.refreshDisplayCraftResult();
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if(slot < SLOT_RESULT) {
            return this.craftMatrix.getStackInSlotOnClosing(slot);
        }else if(slot == SLOT_RESULT) {
            return this.craftResult.getStackInSlotOnClosing(0);
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if(slot < SLOT_RESULT) {
            if(stack != null && stack.stackSize > 0) {
                ItemStack slotStack = this.craftMatrix.getStackInSlot(slot);
                if(slotStack != null) {
                    stack.stackSize -=slotStack.stackSize;
                }
                distributeItemsToCraftMatrix(stack);
            }else {
                this.craftMatrix.setInventorySlotContents(slot, null);
            }
        }else if(slot == SLOT_RESULT) {
            if(!isRemainCraftResult() && this.canCraft()) {
                this.craftResult.setInventorySlotContents(0, this.craft());
            }

            if (stack == null || StackHelper.canStacksMerge(this.getCraftResult(), stack)) {
                this.craftResult.setInventorySlotContents(0, stack);
            }
        }else {
            return;
        }
        this.refreshDisplayCraftResult();
    }

    @Override
    public String getInventoryName() { return ""; }

    @Override
    public boolean hasCustomInventoryName() { return false; }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(this.xCoord, this.yCoord, this.zCoord) <= 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.craftMatrix.markDirty();
        this.craftResult.markDirty();
        this.displayCraftResult.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        InvUtils.readInvFromNBT(this.craftMatrix, "matrix", data);

        if(data.hasKey("result")) {
            this.craftResult.setInventorySlotContents(0, ItemStack.loadItemStackFromNBT(data.getCompoundTag("result")));
        }

        this.refreshDisplayCraftResult();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        InvUtils.writeInvToNBT(this.craftMatrix, "matrix", data);

        if(this.craftResult.getStackInSlot(0) != null) {
            NBTTagCompound bufferItemCompound = new NBTTagCompound();
            this.craftResult.getStackInSlot(0).writeToNBT(bufferItemCompound);
            data.setTag("result", bufferItemCompound);
        }
    }

    @Override
    public void writeData(ByteBuf stream) {
        super.writeData(stream);
        NetworkUtils.writeStack(stream, this.craftResult.getStackInSlot(0));
    }

    @Override
    public void readData(ByteBuf stream) {
        super.readData(stream);
        this.craftResult.setInventorySlotContents(0, NetworkUtils.readStack(stream));
    }

    private static class SlotStackSize {
        public int slot;
        public int stackSize;

        public static SlotStackSize of(int slot, int stackSize) {
            SlotStackSize s3 = new SlotStackSize();
            s3.slot = slot;
            s3.stackSize = stackSize;
            return s3;
        }

        public static int compare(SlotStackSize o1, SlotStackSize o2) {
            return Integer.compare(o1.stackSize, o2.stackSize);
        }
    }

    private void distributeItemsToCraftMatrix(ItemStack stack) {
        int remainStackSize = stack.stackSize;

        PriorityQueue<SlotStackSize> insertSlotQueue = new PriorityQueue<>(SlotStackSize::compare);

        for(int craftSlot = 0; craftSlot < this.craftMatrix.getSizeInventory(); craftSlot++) {
            ItemStack craftSlotStack = this.craftMatrix.getStackInSlot(craftSlot);
            if(StackHelper.canStacksMerge(stack, craftSlotStack)) {
                insertSlotQueue.add(SlotStackSize.of(craftSlot, craftSlotStack.stackSize));
            }
        }

        final int maxStackSize = Math.min(stack.getItem().getItemStackLimit(stack), this.getInventoryStackLimit());
        final int canInsertStackSize = maxStackSize * insertSlotQueue.size() - insertSlotQueue.stream().mapToInt(v -> v.stackSize).sum();

        if(canInsertStackSize < remainStackSize && !this.worldObj.isRemote) {
            ItemStack overflowStack = stack.copy();
            overflowStack.stackSize = remainStackSize - canInsertStackSize;
            InvUtils.dropItems(this.worldObj, overflowStack, this.xCoord, this.yCoord + 1, this.zCoord);
        }
        remainStackSize = Math.min(remainStackSize, canInsertStackSize);

        for(int i = 0; i < remainStackSize; i++) {
            SlotStackSize slotToStackSize = insertSlotQueue.poll();
            if(slotToStackSize != null) slotToStackSize.stackSize += 1;
            insertSlotQueue.add(slotToStackSize);
        }

        for(SlotStackSize slotToStackSize : insertSlotQueue) {
            ItemStack craftStack = this.craftMatrix.getStackInSlot(slotToStackSize.slot);
            if(craftStack != null) {
                craftStack.stackSize = slotToStackSize.stackSize;
                this.craftMatrix.setInventorySlotContents(slotToStackSize.slot, craftStack);
            }
        }
    }

    public boolean canCraft() {
        ItemStack resultStack = this.getCraftResult();
        if(resultStack == null) return false;

        StackPointer[] pointers = new StackPointer[this.craftMatrix.getSizeInventory()];
        for(int slot = 0; slot < this.craftMatrix.getSizeInventory(); slot++) {
            ItemStack slotStack = this.craftMatrix.getStackInSlot(slot);
            if(slotStack == null) continue;
            if(slotStack.stackSize > 1) {
                StackPointer slotPointer = new StackPointer(this.craftMatrix, slot, slotStack);
                slotPointer.decrStackSize(1);
                pointers[slot] = slotPointer;
            }else {
                StackPointer nearbyPointer = this.getNearbyItem(slotStack);
                if(nearbyPointer == null) {
                    StackPointer.resetPointers(pointers);
                    return false;
                }
                nearbyPointer.decrStackSize(1);
                pointers[slot] = nearbyPointer;
            }
        }
        StackPointer.resetPointers(pointers);
        return true;
    }

    private ItemStack craft() {
        ItemStack resultStack = this.getCraftResult();
        if(resultStack == null) return null;

        StackPointer[] pointers = new StackPointer[this.craftMatrix.getSizeInventory()];
        for(int slot = 0; slot < this.craftMatrix.getSizeInventory(); slot++) {
            ItemStack slotStack = this.craftMatrix.getStackInSlot(slot);
            if(slotStack == null) continue;
            if(slotStack.stackSize > 1) {
                StackPointer slotPointer = new StackPointer(this.craftMatrix, slot, slotStack);
                slotPointer.decrStackSize(1);
                pointers[slot] = slotPointer;
            }else {
                StackPointer nearbyPointer = this.getNearbyItem(slotStack);
                if(nearbyPointer == null) {
                    StackPointer.resetPointers(pointers);
                    return null;
                }
                nearbyPointer.decrStackSize(1);
                pointers[slot] = nearbyPointer;
            }
        }

        for(StackPointer p : pointers) {
            if(p == null) continue;

            if(p.itemStack.getItem().getContainerItem() != null) {
                ItemStack containerItemStack = p.itemStack.getItem().getContainerItem(p.itemStack);
                p.setInventorySlotContents(containerItemStack);
            }
        }

        return resultStack;
    }

    public void craftManually() {
        for(int i = 0; i < this.craftMatrix.getSizeInventory(); i++) {
            ItemStack slotStack = this.craftMatrix.decrStackSize(i, 1);
            if(slotStack != null && slotStack.getItem().getContainerItem() != null) {
                ItemStack containerItemStack = slotStack.getItem().getContainerItem(slotStack);
                if(this.craftMatrix.getStackInSlot(i) == null) {
                    this.craftMatrix.setInventorySlotContents(i, containerItemStack);
                }else if(!this.worldObj.isRemote) {
                    InvUtils.dropItems(this.worldObj, slotStack, this.xCoord, this.yCoord + 1, this.zCoord);
                }
            }
        }
        this.markDirty();
    }

    private static class StackPointer {

        public static void resetPointers(StackPointer[] pointers) {
            for(StackPointer p : pointers) {
                if(p != null) p.resetPointer();
            }
        }

        public final IInventory inv;
        public final int slot;
        public final ItemStack itemStack;

        public StackPointer(IInventory inv, int slot, ItemStack item) {
            this.inv = inv;
            this.slot = slot;
            this.itemStack = item;
        }

        public void decrStackSize(int count) {
            inv.decrStackSize(this.slot, count);
        }

        public void setInventorySlotContents(ItemStack stack) {
            inv.setInventorySlotContents(this.slot, stack);
        }

        public ItemStack getStackInSlot() {
            return inv.getStackInSlot(this.slot);
        }

        public void resetPointer() {
            if (this.getStackInSlot() == null) {
                this.setInventorySlotContents(this.itemStack);
            } else {
                this.getStackInSlot().stackSize++;
            }
            this.inv.markDirty();
        }
    }

    private StackPointer getNearbyItem(ItemStack itemStack) {
        StackPointer pointer = null;

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            pointer = getNearbyItemFromOrientation(itemStack, dir);
            if(pointer != null) break;
        }

        return pointer;
    }

    private StackPointer getNearbyItemFromOrientation (ItemStack stack, ForgeDirection dir) {
        int x = xCoord + dir.offsetX;
        int y = yCoord + dir.offsetY;
        int z = zCoord + dir.offsetZ;

        TileEntity tile = worldObj.getTileEntity(x, y, z);
        if(tile == null) return null;
        if(tile instanceof TileOldAutoWorkbench) return null;

        if(tile instanceof ISidedInventory) {
            ISidedInventory inventory = (ISidedInventory)InvUtils.getInventory((ISidedInventory)tile);

            for(int slot : inventory.getAccessibleSlotsFromSide(dir.ordinal())) {
                ItemStack slotStack = inventory.getStackInSlot(slot);
                if(slotStack == null) continue;
                if(stack.stackSize <= 0) continue;
                if(!inventory.canExtractItem(slot, slotStack, dir.ordinal())) continue;
                if(!StackHelper.isCraftingEquivalent(stack, slotStack, true)) continue;

                return new StackPointer(inventory, slot, slotStack);
            }
        } else if (tile instanceof IInventory) {
            IInventory inventory = InvUtils.getInventory((IInventory) tile);

            for (int slot = 0; slot < inventory.getSizeInventory(); ++slot) {
                ItemStack slotStack = inventory.getStackInSlot(slot);
                if(slotStack == null) continue;
                if(slotStack.stackSize <= 0) continue;
                if(!StackHelper.isCraftingEquivalent(stack, slotStack, true)) continue;

                return new StackPointer(inventory, slot, slotStack);
            }
        }

        return null;
    }
}
