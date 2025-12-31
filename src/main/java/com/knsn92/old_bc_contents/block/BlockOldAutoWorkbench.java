package com.knsn92.old_bc_contents.block;

import buildcraft.api.transport.IItemPipe;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import com.knsn92.old_bc_contents.OldBCContents;
import com.knsn92.old_bc_contents.tile.TileOldAutoWorkbench;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockOldAutoWorkbench extends BlockBuildCraft {

    public BlockOldAutoWorkbench() {
        super(Material.wood);
        this.setBlockName("oldAutoWorkbench");
        this.setHardness(1.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileOldAutoWorkbench();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9)) {
            return true;
        } else if (entityplayer.isSneaking()) {
            return false;
        } else if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
            return false;
        } else {
            if (!world.isRemote) {
                entityplayer.openGui(OldBCContents.instance, 0, world, x, y, z);
            }

            return true;
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
        preDestroyBlock(world, x, y, z);
        super.breakBlock(world, x, y, z, block, par6);
    }

    private static void preDestroyBlock(World world, int i, int j, int k) {
        if (!(BlockUtils.getTileEntity(world, i, j, k) instanceof TileOldAutoWorkbench)) return;
        TileOldAutoWorkbench tile = (TileOldAutoWorkbench) BlockUtils.getTileEntity(world, i, j, k);
        if(!world.isRemote) {
            if (tile.craftResult.getStackInSlot(0) != null) {
                InvUtils.dropItems(world, tile.craftResult, i, j, k);
            }
            InvUtils.dropItems(world, tile.craftMatrix, i, j, k);
            InvUtils.wipeInventory(tile);
        }
        tile.destroy();
    }
}
