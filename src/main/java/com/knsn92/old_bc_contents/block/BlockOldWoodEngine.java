package com.knsn92.old_bc_contents.block;

import buildcraft.core.lib.engines.BlockEngineBase;
import com.knsn92.old_bc_contents.tile.TileOldWoodEngine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class BlockOldWoodEngine extends BlockEngineBase {

    private static final String TEXTURE_PREFIX = "oldbccontents:engineOldWood";

    public BlockOldWoodEngine() {
        this.setBlockName("oldWoodEngine");
    }

    @Override
    public String getTexturePrefix(int meta, boolean addPrefix) {
        return addPrefix ? TEXTURE_PREFIX.replaceAll(":", ":textures/blocks/") : TEXTURE_PREFIX;
    }

    @Override
    public String getUnlocalizedName(int metadata) {
        return "tile.engineOldWood";
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileOldWoodEngine();
    }

    @Override
    public boolean hasEngine(int metadata) {
        return metadata == 0;
    }
}
