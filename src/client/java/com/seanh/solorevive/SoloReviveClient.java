package com.seanh.solorevive;

import com.seanh.solorevive.network.SelfRescuePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public final class SoloReviveClient
        implements ClientModInitializer {

    private static KeyMapping selfRescueKey;

    private static boolean previousPressed = false;

    private static int keepAliveTimer = 0;

    private static final KeyMapping.Category SOLO_REVIVE_CATEGORY =
            KeyMapping.Category.register(
                    "category.solo_revive"
            );

    @Override
    public void onInitializeClient() {

        selfRescueKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.solo_revive.self_rescue",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                SOLO_REVIVE_CATEGORY
                        )
                );

        ClientTickEvents.END_CLIENT_TICK.register(
                SoloReviveClient::tick
        );
    }

    private static void tick(Minecraft client) {

        if (client.player == null) {
            return;
        }

        boolean pressed =
                selfRescueKey.isDown();

        if (pressed != previousPressed) {

            previousPressed = pressed;

            ClientPlayNetworking.send(
                    new SelfRescuePayload(pressed)
            );

            keepAliveTimer = 0;

            return;
        }

        if (pressed) {

            keepAliveTimer++;

            if (keepAliveTimer >= 5) {

                ClientPlayNetworking.send(
                        new SelfRescuePayload(true)
                );

                keepAliveTimer = 0;
            }
        }
    }
}
