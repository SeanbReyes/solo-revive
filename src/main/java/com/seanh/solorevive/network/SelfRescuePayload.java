package com.seanh.solorevive.network;

import com.seanh.solorevive.SoloRevive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelfRescuePayload(boolean pressed)
        implements CustomPacketPayload {

    public static final Type<SelfRescuePayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            SoloRevive.MOD_ID,
                            "self_rescue"
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SelfRescuePayload
            > STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SelfRescuePayload::pressed,
                    SelfRescuePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
