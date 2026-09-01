package com.apexballistics.network;

import com.apexballistics.ApexBallistics;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public final class ApexNetwork {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ApexBallistics.id("main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    private ApexNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(CruiseLauncherFirePacket.class, 0)
                .encoder(CruiseLauncherFirePacket::encode)
                .decoder(CruiseLauncherFirePacket::decode)
                .consumerMainThread(CruiseLauncherFirePacket::handle)
                .add();
        CHANNEL.messageBuilder(CoordToolSavePacket.class, 1)
                .encoder(CoordToolSavePacket::encode)
                .decoder(CoordToolSavePacket::decode)
                .consumerMainThread(CoordToolSavePacket::handle)
                .add();
        CHANNEL.build();
    }
}
