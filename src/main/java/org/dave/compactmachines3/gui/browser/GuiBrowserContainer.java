package org.dave.compactmachines3.gui.browser;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class GuiBrowserContainer extends Container {

    public final EntityPlayer player;

    public GuiBrowserContainer(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
