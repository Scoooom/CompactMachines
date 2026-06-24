package org.dave.compactmachines3.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.network.MessageRequestMachineList;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.reference.GuiIds;

import java.util.ArrayList;
import java.util.List;

public class ItemMachineBrowser extends Item {

    public ItemMachineBrowser() {
        this.setMaxStackSize(1);
        this.setCreativeTab(CompactMachines3.CREATIVE_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
                new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            player.openGui(CompactMachines3.instance, GuiIds.MACHINE_BROWSER.ordinal(),
                    world, (int) player.posX, (int) player.posY, (int) player.posZ);
        } else {
            PackageHandler.instance.sendToServer(new MessageRequestMachineList(player.getUniqueID()));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    // --- Favorites stored in item NBT ---

    public static List<Integer> getFavorites(ItemStack stack) {
        List<Integer> list = new ArrayList<>();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("favorites")) {
            for (int id : stack.getTagCompound().getIntArray("favorites")) {
                list.add(id);
            }
        }
        return list;
    }

    public static boolean isFavorite(ItemStack stack, int machineId) {
        return getFavorites(stack).contains(machineId);
    }

    public static void addFavorite(ItemStack stack, int machineId) {
        List<Integer> favs = getFavorites(stack);
        if (!favs.contains(machineId)) {
            favs.add(machineId);
            saveFavorites(stack, favs);
        }
    }

    public static void removeFavorite(ItemStack stack, int machineId) {
        List<Integer> favs = getFavorites(stack);
        favs.remove(Integer.valueOf(machineId));
        saveFavorites(stack, favs);
    }

    private static void saveFavorites(ItemStack stack, List<Integer> favs) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int[] arr = favs.stream().mapToInt(Integer::intValue).toArray();
        stack.getTagCompound().setTag("favorites", new NBTTagIntArray(arr));
    }

    public static int getLastViewedId(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("lastViewedId")) {
            return stack.getTagCompound().getInteger("lastViewedId");
        }
        return -1;
    }

    public static void setLastViewedId(ItemStack stack, int id) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("lastViewedId", id);
    }
}
