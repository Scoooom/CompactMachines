package org.dave.compactmachines3.gui.browser;

import net.minecraft.client.Minecraft;
import org.dave.compactmachines3.gui.framework.WidgetGuiContainer;
import org.dave.compactmachines3.network.MessageMachineList;

import java.util.ArrayList;
import java.util.List;

public class GuiBrowserData {

    public static List<MessageMachineList.MachineEntry> entries = new ArrayList<>();
    public static int currentIndex = 0;

    public static void updateEntries(List<MessageMachineList.MachineEntry> newEntries) {
        entries = new ArrayList<>(newEntries);
        // Keep currentIndex in bounds
        if (entries.isEmpty()) {
            currentIndex = 0;
        } else if (currentIndex >= entries.size()) {
            currentIndex = entries.size() - 1;
        }

        // Fire data update event if GUI is open
        if (Minecraft.getMinecraft().currentScreen instanceof WidgetGuiContainer) {
            WidgetGuiContainer gui = (WidgetGuiContainer) Minecraft.getMinecraft().currentScreen;
            gui.fireDataUpdateEvent();
        }
    }

    public static MessageMachineList.MachineEntry getCurrentEntry() {
        if (entries.isEmpty() || currentIndex < 0 || currentIndex >= entries.size()) {
            return null;
        }
        return entries.get(currentIndex);
    }

    public static void selectById(int machineId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id == machineId) {
                currentIndex = i;
                return;
            }
        }
    }

    public static List<MessageMachineList.MachineEntry> getFavorites() {
        List<MessageMachineList.MachineEntry> favs = new ArrayList<>();
        for (MessageMachineList.MachineEntry e : entries) {
            if (e.favorite) favs.add(e);
        }
        return favs;
    }

    public static boolean currentIsFavorite() {
        MessageMachineList.MachineEntry e = getCurrentEntry();
        return e != null && e.favorite;
    }
}
