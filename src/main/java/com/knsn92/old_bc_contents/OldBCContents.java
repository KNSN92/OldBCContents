package com.knsn92.old_bc_contents;

import buildcraft.BuildCraftCore;
import buildcraft.core.BCRegistry;
import buildcraft.core.lib.engines.RenderEngine;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.render.RenderingEntityBlocks;
import com.knsn92.old_bc_contents.block.BlockOldAutoWorkbench;
import com.knsn92.old_bc_contents.block.BlockOldWoodEngine;
import com.knsn92.old_bc_contents.gui.ContainerOldAutoWorkbench;
import com.knsn92.old_bc_contents.gui.GuiOldAutoWorkbench;
import com.knsn92.old_bc_contents.logisticspipes.OldAutoWorkbenchCraftingRecipeProvider;
import com.knsn92.old_bc_contents.tile.TileOldAutoWorkbench;
import com.knsn92.old_bc_contents.tile.TileOldWoodEngine;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.asm.wrapper.LogisticsWrapperHandler;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mod(modid = OldBCContents.MOD_ID, name = OldBCContents.NAME, version = OldBCContents.VERSION, dependencies = "required-after:BuildCraft|Core")
public class OldBCContents {

    public static OldBCContents instance;

    public static final String MOD_ID = "OldBCContents";
    public static final String NAME = "OldBCContents";
    public static final String VERSION = "0.0.3";

    public static BlockOldWoodEngine blockOldWoodEngine = new BlockOldWoodEngine();
    public static BlockOldAutoWorkbench blockOldAutoWorkbench = new BlockOldAutoWorkbench();

    {
        OldBCContents.instance = this;
    }

    @Mod.EventHandler
    private void preInit(FMLPreInitializationEvent e) {
        BCRegistry.INSTANCE.registerBlock(blockOldWoodEngine, true);
        BCRegistry.INSTANCE.registerTileEntity(TileOldWoodEngine.class, "com.knsn92.old_bc_contents.tile.TileOldWoodEngine");

        BCRegistry.INSTANCE.registerBlock(blockOldAutoWorkbench, true);
        BCRegistry.INSTANCE.registerTileEntity(TileOldAutoWorkbench.class, "com.knsn92.old_bc_contents.tile.TileOldAutoWorkbench");
    }

    @Mod.EventHandler
    private void init(FMLInitializationEvent e) {
        initRender();
        loadRecipe();
        registerGUI();
    }

    @Mod.EventHandler
    private void postInit(FMLPostInitializationEvent e) {
        if(Loader.isModLoaded("LogisticsPipes")) {
            loadLogisticsPipesIntegration();
        }
    }

    private static void loadLogisticsPipesIntegration() {
        SimpleServiceLocator.addCraftingRecipeProvider(
            LogisticsWrapperHandler.getWrappedRecipeProvider(
                OldBCContents.MOD_ID,
                "OldAutoWorkbench",
                OldAutoWorkbenchCraftingRecipeProvider.class
            )
        );
    }

    private void registerGUI() {
        NetworkRegistry.INSTANCE.registerGuiHandler(OldBCContents.instance, new IGuiHandler() {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                if(!world.blockExists(x, y, z)){
                    return null;
                }
                if(ID == 0) {
                    return new ContainerOldAutoWorkbench(player.inventory, (TileOldAutoWorkbench) world.getTileEntity(x, y, z), world, x, y, z);
                }
                return null;
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                if(!world.blockExists(x, y, z)){
                    return null;
                }
                if(ID == 0) {
                    return new GuiOldAutoWorkbench(player.inventory, (TileOldAutoWorkbench) world.getTileEntity(x, y, z), world, x, y, z);
                }
                return null;
            }
        });
    }

    private static void loadRecipe() {

        BCRegistry.INSTANCE.addCraftingRecipe(new ItemStack(blockOldAutoWorkbench), " g ", "gwg", " g ", 'w', "craftingTableWood", 'g', "gearWood");

        BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(blockOldWoodEngine),
                new ItemStack(BuildCraftCore.engineBlock, 1, 0)
        );
        BCRegistry.INSTANCE.addShapelessRecipe(new ItemStack(BuildCraftCore.engineBlock, 1, 0),
                new ItemStack(blockOldWoodEngine)
        );
    }

    @SideOnly(Side.CLIENT)
    private static void initRender() {
        TileEngineBase engineTile = (TileEngineBase) blockOldWoodEngine.createTileEntity(null, 0);
        engineTile.blockType = blockOldWoodEngine;
        engineTile.blockMetadata = 0;
        RenderingEntityBlocks.blockByEntityRenders.put(new RenderingEntityBlocks.EntityRenderIndex(blockOldWoodEngine, 0), new RenderEngine(engineTile));
    }
}
