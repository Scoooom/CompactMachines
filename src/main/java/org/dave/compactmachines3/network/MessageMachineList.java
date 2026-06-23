package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.gui.browser.GuiBrowserData;

import java.util.ArrayList;
import java.util.List;

public class MessageMachineList implements IMessage {

    public static class MachineEntry {
        public int id;
        public String name;
        public int sizeMeta;
        public boolean favorite;

        public MachineEntry() {}

        public MachineEntry(int id, String name, int sizeMeta, boolean favorite) {
            this.id = id;
            this.name = name;
            this.sizeMeta = sizeMeta;
            this.favorite = favorite;
        }

        public String getDisplayName() {
            return (name != null && !name.isEmpty()) ? name : "#" + id;
        }
    }

    private List<MachineEntry> entries;

    public MessageMachineList() {
        entries = new ArrayList<>();
    }

    public MessageMachineList(List<MachineEntry> entries) {
        this.entries = entries;
    }

    public List<MachineEntry> getEntries() {
        return entries;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MachineEntry e = new MachineEntry();
            e.id = buf.readInt();
            e.name = ByteBufUtils.readUTF8String(buf);
            e.sizeMeta = buf.readInt();
            e.favorite = buf.readBoolean();
            entries.add(e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entries.size());
        for (MachineEntry e : entries) {
            buf.writeInt(e.id);
            ByteBufUtils.writeUTF8String(buf, e.name != null ? e.name : "");
            buf.writeInt(e.sizeMeta);
            buf.writeBoolean(e.favorite);
        }
    }

    public static class Handler implements IMessageHandler<MessageMachineList, IMessage> {
        @Override
        public IMessage onMessage(MessageMachineList message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                GuiBrowserData.updateEntries(message.entries);
            });
            return null;
        }
    }
}
