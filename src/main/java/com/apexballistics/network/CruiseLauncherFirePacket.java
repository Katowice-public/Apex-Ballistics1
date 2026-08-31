package com.apexballistics.network;

import com.apexballistics.menu.CruiseLauncherMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record CruiseLauncherFirePacket(int containerId, int x, int y, int z) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public static CruiseLauncherFirePacket decode(FriendlyByteBuf buf) {
        return new CruiseLauncherFirePacket(buf.readVarInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(CruiseLauncherFirePacket packet, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.containerMenu instanceof CruiseLauncherMenu menu && menu.containerId == packet.containerId()) {
            menu.handleLaunch(player, packet.x(), packet.y(), packet.z());
        }
    }
}
