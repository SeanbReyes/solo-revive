package com.seanh.solorevive.network;

import com.seanh.solorevive.SelfRescueManager;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SelfRescueNetworking {

    private static boolean initialized = false;

    private SelfRescueNetworking() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        PayloadTypeRegistry.playC2S()
                .register(
                        SelfRescuePayload.TYPE,
                        SelfRescuePayload.STREAM_CODEC
                );

        ServerPlayNetworking.registerGlobalReceiver(
                SelfRescuePayload.TYPE,
                (payload, context) -> {

                    context.server().execute(() -> {

                        SelfRescueManager.setKeyState(
                                context.player(),
                                payload.pressed()
                        );

                    });
                }
        );
    }
}
