package com.seanh.solorevive;

import com.seanh.solorevive.network.SelfRescueNetworking;
import com.seanh.solorevive.network.SelfRescuePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import com.mojang.blaze3d.platform.InputConstants;

import org.lwjgl.glfw.GLFW;

public final class SoloReviveClient
        implements ClientModInitializer {

    private static KeyMapping selfRescueKey;

    private static boolean previousPressed = false;

    @Override
    public void onInitializeClient() {

        selfRescueKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.solo_revive.self_rescue",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_R,
                                "category.solo_revive"
                        )
                );

        ClientTickEvents.END_CLIENT_TICK.register(
                SoloReviveClient::tick
        );
    }

    private static void tick(Minecraft client) {

        LocalPlayer player = client.player;

        if (player == null) {
            return;
        }

        boolean pressed =
                selfRescueKey.isDown();

        /*
         * Solo enviamos un paquete cuando el estado
         * cambia de pulsado a soltado o viceversa.
         */
        if (pressed == previousPressed) {
            return;
        }

        previousPressed = pressed;

        ClientPlayNetworking.send(
                new SelfRescuePayload(pressed)
        );
    }
}
