package org.dave.compactmachines3.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.browser.GuiBrowser;
import org.dave.compactmachines3.gui.browser.GuiBrowserContainer;
import org.dave.compactmachines3.gui.machine.GuiMachine;
import org.dave.compactmachines3.gui.machine.GuiMachineContainer;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;
import org.dave.compactmachines3.reference.GuiIds;

public class GuiHandler implements IGuiHandler {
    public static void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(CompactMachines3.instance, new GuiHandler());
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIds.MACHINE_VIEW.ordinal() || ID == GuiIds.MACHINE_ADMIN.ordinal()) {
            return new GuiMachineContainer(world, new BlockPos(x, y, z), player);
        } else if (ID == GuiIds.MACHINE_BROWSER.ordinal()) {
            return new GuiBrowserContainer(player);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GuiIds.PSD_GUIDE.ordinal()) {
            return new GuiPSDScreen();
        } else if (ID == GuiIds.MACHINE_VIEW.ordinal()) {
            return new GuiMachine(new GuiMachineContainer(world, new BlockPos(x, y, z), player), false);
        } else if (ID == GuiIds.MACHINE_ADMIN.ordinal()) {
            return new GuiMachine(new GuiMachineContainer(world, new BlockPos(x, y, z), player), true);
        } else if (ID == GuiIds.MACHINE_BROWSER.ordinal()) {
            // x,y,z = block pos when opened from block; player foot pos when opened from item
            // We pass both and let the GUI decide; block browser sets a non-null TE pos
            BlockPos pos = new BlockPos(x, y, z);
            boolean isBlock = world.getTileEntity(pos) instanceof org.dave.compactmachines3.tile.TileEntityMachineBrowser;
            return new GuiBrowser(
                    new GuiBrowserContainer(player),
                    player,
                    isBlock ? pos : null,
                    isBlock ? world.provider.getDimension() : -1);
        }

        return null;
    }
}
