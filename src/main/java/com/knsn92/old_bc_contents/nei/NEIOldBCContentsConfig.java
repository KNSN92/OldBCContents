package com.knsn92.old_bc_contents.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.recipe.DefaultOverlayHandler;
import com.knsn92.old_bc_contents.OldBCContents;
import com.knsn92.old_bc_contents.gui.GuiOldAutoWorkbench;

public class NEIOldBCContentsConfig implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.registerGuiOverlay(GuiOldAutoWorkbench.class, "crafting");
        API.registerGuiOverlayHandler(GuiOldAutoWorkbench.class, new DefaultOverlayHandler(), "crafting");
    }

    @Override
    public String getName() {
        return OldBCContents.NAME;
    }

    @Override
    public String getVersion() {
        return OldBCContents.VERSION;
    }
}
