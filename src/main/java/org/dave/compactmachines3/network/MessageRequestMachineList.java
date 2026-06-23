package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.tile.TileEntityMachineBrowser;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageRequestMachineList implements IMessage {

    private UUID playerUUID;
    // Block pos is sent so server can load favorites from the TE (null if using portable item)
    private boolean hasBlockPos;
    private int blockX, blockY, blockZ, blockDim;

    public MessageRequestMachineList() {}

    public MessageRequestMachineList(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.hasBlockPos = false;
    }

    public MessageRequestMachineList(UUID playerUUID, BlockPos pos, int dim) {
        this.playerUUID = playerUUID;
        this.hasBlockPos = true;
        this.blockX = pos.getX();
        this.blockY = pos.getY();
        this.blockZ = pos.getZ();
        this.blockDim = dim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long most = buf.readLong();
        long least = buf.readLong();
        playerUUID = new UUID(most, least);
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
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeBoolean(hasBlockPos);
        if (hasBlockPos) {
            buf.writeInt(blockX);
            buf.writeInt(blockY);
            buf.writeInt(blockZ);
            buf.writeInt(blockDim);
        }
    }

    public static class Handler implements IMessageHandler<MessageRequestMachineList, IMessage> {
        @Override
        public IMessage onMessage(MessageRequestMachineList message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                List<Integer> favorites = new ArrayList<>();
                if (message.hasBlockPos) {
                    World world = FMLCommonHandler.instance().getMinecraftServerInstance()
                            .getWorld(message.blockDim);
                    if (world != null) {
                        TileEntity te = world.getTileEntity(new BlockPos(message.blockX, message.blockY, message.blockZ));
                        if (te instanceof TileEntityMachineBrowser) {
                            favorites = ((TileEntityMachineBrowser) te).getFavorites();
                        }
                    }
                }

                WorldSavedDataMachines wsd = WorldSavedDataMachines.getInstance();
                List<MessageMachineList.MachineEntry> entries = new ArrayList<>();

                for (Integer id : wsd.machinePositions.keySet()) {
                    TileEntityMachine machine = wsd.getMachine(id);
                    if (machine == null) continue;

                    UUID owner = machine.getOwner();
                    if (owner == null || !owner.equals(message.playerUUID)) continue;

                    String name = machine.getCustomName();
                    int sizeMeta = wsd.machineSizes.containsKey(id)
                            ? wsd.machineSizes.get(id).getMeta() : 0;
                    boolean isFav = favorites.contains(id);

                    entries.add(new MessageMachineList.MachineEntry(id, name, sizeMeta, isFav));
                }

                entries.sort((a, b) -> Integer.compare(a.id, b.id));
                PackageHandler.instance.sendTo(new MessageMachineList(entries), player);
            });
            return null;
        }
    }
}
