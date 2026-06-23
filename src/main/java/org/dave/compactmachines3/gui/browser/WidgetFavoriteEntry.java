package org.dave.compactmachines3.gui.browser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.dave.compactmachines3.gui.framework.ISelectable;
import org.dave.compactmachines3.gui.framework.widgets.Widget;
import org.dave.compactmachines3.network.MessageMachineList;

public class WidgetFavoriteEntry extends Widget implements ISelectable {

    private MessageMachineList.MachineEntry entry;
    private boolean selected = false;

    public WidgetFavoriteEntry(MessageMachineList.MachineEntry entry) {
        this.entry = entry;
        this.setHeight(14);
    }

    public MessageMachineList.MachineEntry getEntry() {
        return entry;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean state) {
        this.selected = state;
    }

    @Override
    public void draw(GuiScreen screen) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        String label = entry.getDisplayName();
        // Truncate to fit sidebar width (subtract padding)
        int maxWidth = this.width - 4;
        if (fr.getStringWidth(label) > maxWidth) {
            label = fr.trimStringToWidth(label, maxWidth - fr.getStringWidth("...")) + "...";
        }
        int color = selected ? 0xFFFFFF00 : 0xFFDDDDDD;
        fr.drawString(label, 2, 3, color);
    }
}
