package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.item.ItemMachineBrowser;
import org.dave.compactmachines3.tile.TileEntityMachineBrowser;

import java.util.UUID;

/**
 * Sent client→server when player clicks the star button.
 * Server updates TE or item NBT, then re-sends the machine list.
 * hasBlockPos=true  → block browser (update TE favorites)
 * hasBlockPos=false → portable item (update held item NBT)
 */
public class MessageToggleFavorite implements IMessage {

    private int machineId;
    private boolean hasBlockPos;
    private int blockX, blockY, blockZ, blockDim;

    public MessageToggleFavorite() {}

    /** For portable item: no block pos */
    public MessageToggleFavorite(int machineId) {
        this.machineId = machineId;
        this.hasBlockPos = false;
    }

    /** For block browser */
    public MessageToggleFavorite(int machineId, BlockPos pos, int dim) {
        this.machineId = machineId;
        this.hasBlockPos = true;
        this.blockX = pos.getX();
        this.blockY = pos.getY();
        this.blockZ = pos.getZ();
        this.blockDim = dim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        machineId = buf.readInt();
        hasBlockPos = buf.readBoolean();
        if (hasBlockPos) {
            blockX = buf.readInt();
            blockY = buf.readInt();
            blockZ = buf.readInt();
            blockDim = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(machineId);
        buf.writeBoolean(hasBlockPos);
        if (hasBlockPos) {
            buf.writeInt(blockX);
            buf.writeInt(blockY);
            buf.writeInt(blockZ);
            buf.writeInt(blockDim);
        }
    }

    public static class Handler implements IMessageHandler<MessageToggleFavorite, IMessage> {
        @Override
        public IMessage onMessage(MessageToggleFavorite message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;

            if (message.hasBlockPos) {
                // Block browser — update TE favorites
                World world = FMLCommonHandler.instance().getMinecraftServerInstance()
                        .getWorld(message.blockDim);
                if (world != null) {
                    TileEntity te = world.getTileEntity(
                            new BlockPos(message.blockX, message.blockY, message.blockZ));
                    if (te instanceof TileEntityMachineBrowser) {
                        TileEntityMachineBrowser browser = (TileEntityMachineBrowser) te;
                        if (browser.isFavorite(message.machineId)) {
                            browser.removeFavorite(message.machineId);
                        } else {
                            browser.addFavorite(message.machineId);
                        }
                        // Re-send full list with updated favorites
                        UUID uuid = player.getUniqueID();
                        PackageHandler.instance.sendTo(
                                new MessageRequestMachineList(uuid,
                                        new BlockPos(message.blockX, message.blockY, message.blockZ),
                                        message.blockDim),
                                player);
                        // Re-trigger the list build by invoking the handler directly
                        new MessageRequestMachineList.Handler().onMessage(
                                new MessageRequestMachineList(uuid,
                                        new BlockPos(message.blockX, message.blockY, message.blockZ),
                                        message.blockDim),
                                ctx);
                    }
                }
            } else {
                // Portable item — update held item NBT
                ItemStack held = player.getHeldItemMainhand();
                if (held.getItem() instanceof ItemMachineBrowser) {
                    if (ItemMachineBrowser.isFavorite(held, message.machineId)) {
                        ItemMachineBrowser.removeFavorite(held, message.machineId);
                    } else {
                        ItemMachineBrowser.addFavorite(held, message.machineId);
                    }
                    // Re-send list
                    new MessageRequestMachineList.Handler().onMessage(
                            new MessageRequestMachineList(player.getUniqueID()), ctx);
                }
            }

            return null;
        }
    }
}
