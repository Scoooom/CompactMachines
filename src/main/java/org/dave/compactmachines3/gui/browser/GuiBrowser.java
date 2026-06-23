package org.dave.compactmachines3.gui.browser;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.dave.compactmachines3.gui.framework.WidgetGuiContainer;

public class GuiBrowser extends WidgetGuiContainer {

    public GuiBrowser(GuiBrowserContainer container, EntityPlayer player,
            BlockPos browserBlockPos, int browserBlockDim) {
        super(container);

        this.xSize = 280;
        this.ySize = 220;

        this.gui = new GuiBrowserWidgetGui(this.xSize, this.ySize, player,
                browserBlockPos, browserBlockDim);
    }
}
