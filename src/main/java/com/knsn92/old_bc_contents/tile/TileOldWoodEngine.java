package com.knsn92.old_bc_contents.tile;

import buildcraft.core.lib.engines.TileEngineBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

public class TileOldWoodEngine extends TileEngineBase {

    public static final ResourceLocation OLD_TRUNK_BLUE_TEXTURE = new ResourceLocation("oldbccontents:textures/blocks/engine/trunk_blue.png");
    public static final ResourceLocation OLD_TRUNK_GREEN_TEXTURE = new ResourceLocation("oldbccontents:textures/blocks/engine/trunk_green.png");
    public static final ResourceLocation OLD_TRUNK_YELLOW_TEXTURE = new ResourceLocation("oldbccontents:textures/blocks/engine/trunk_yellow.png");
    public static final ResourceLocation OLD_TRUNK_RED_TEXTURE = new ResourceLocation("oldbccontents:textures/blocks/engine/trunk_red.png");
    public static final ResourceLocation OLD_TRUNK_OVERHEAT_TEXTURE = new ResourceLocation("oldbccontents:textures/blocks/engine/trunk_overheat.png");

    private boolean hasSent = false;

    public TileOldWoodEngine() {}

    public ResourceLocation getTrunkTexture(TileEngineBase.EnergyStage stage) {
        return overrideTrunkTexture(stage == EnergyStage.RED && (double)this.progress < 0.5 ? EnergyStage.YELLOW : stage);
    }

    private ResourceLocation overrideTrunkTexture(EnergyStage stage) {
        switch (stage) {
            case OVERHEAT:
                return OLD_TRUNK_OVERHEAT_TEXTURE;
            case BLUE:
                return OLD_TRUNK_BLUE_TEXTURE;
            case GREEN:
                return OLD_TRUNK_GREEN_TEXTURE;
            case YELLOW:
                return OLD_TRUNK_YELLOW_TEXTURE;
            case RED:
            default:
                return OLD_TRUNK_RED_TEXTURE;
        }
    }

    protected TileEngineBase.EnergyStage computeEnergyStage() {
        double energyLevel = this.getEnergyLevel();
        if (energyLevel < 0.33000001311302185) {
            return EnergyStage.BLUE;
        } else if (energyLevel < 0.6600000262260437) {
            return EnergyStage.GREEN;
        } else {
            return energyLevel < 0.75 ? EnergyStage.YELLOW : EnergyStage.RED;
        }
    }

    public int getCurrentOutputLimit() {
        return 10;
    }

    public float getPistonSpeed() {
        if (!this.worldObj.isRemote) {
            return Math.max(0.08F * this.getHeatLevel(), 0.01F);
        } else {
            switch (this.getEnergyStage()) {
                case GREEN:
                    return 0.02F;
                case YELLOW:
                    return 0.04F;
                case RED:
                    return 0.08F;
                default:
                    return 0.01F;
            }
        }
    }

    public void engineUpdate() {
        super.engineUpdate();
        if (this.isRedstonePowered && this.worldObj.getTotalWorldTime() % 16L == 0L) {
            this.addEnergy(10);
        }

    }

    public boolean isBurning() {
        return this.isRedstonePowered;
    }

    public int getMaxEnergy() {
        return 1000;
    }

    public int getIdealOutput() {
        return 10;
    }

    public int getEnergyStored(ForgeDirection side) {
        return 0;
    }

    public int getMaxEnergyStored(ForgeDirection side) {
        return 0;
    }

    protected void sendPower() {
        if (this.progressPart == 2 && !this.hasSent) {
            this.hasSent = true;
            super.sendPower();
        } else if (this.progressPart != 2) {
            this.hasSent = false;
        }

    }
}
