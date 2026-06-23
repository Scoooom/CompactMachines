package org.dave.compactmachines3.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class TileEntityMachineBrowser extends TileEntity {

    private List<Integer> favorites = new ArrayList<>();
    private int lastViewedId = -1;

    public List<Integer> getFavorites() {
        return favorites;
    }

    public boolean isFavorite(int machineId) {
        return favorites.contains(machineId);
    }

    public void addFavorite(int machineId) {
        if (!favorites.contains(machineId)) {
            favorites.add(machineId);
            markDirty();
        }
    }

    public void removeFavorite(int machineId) {
        favorites.remove(Integer.valueOf(machineId));
        markDirty();
    }

    public int getLastViewedId() {
        return lastViewedId;
    }

    public void setLastViewedId(int id) {
        this.lastViewedId = id;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        favorites = new ArrayList<>();
        if (compound.hasKey("favorites")) {
            for (int id : compound.getIntArray("favorites")) {
                favorites.add(id);
            }
        }
        lastViewedId = compound.hasKey("lastViewedId") ? compound.getInteger("lastViewedId") : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        int[] arr = favorites.stream().mapToInt(Integer::intValue).toArray();
        compound.setTag("favorites", new NBTTagIntArray(arr));
        compound.setInteger("lastViewedId", lastViewedId);
        return compound;
    }
}
