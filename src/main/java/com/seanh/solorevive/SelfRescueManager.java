package com.seanh.solorevive;

import com.seanh.solorevive.network.SelfRescueNetworking;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.RegistryObjects;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelfRescueManager {

    /*
     * El rescue normal de First Aid New dura 160 ticks.
     */
    private static final int RESCUE_DURATION_TICKS = 160;

    /*
     * El auto-rescate requiere 3 vendas.
     */
    private static final int REQUIRED_BANDAGES = 3;

    private static final Map<UUID, Integer> progress =
            new HashMap<>();

    private static final Map<UUID, Boolean> keyStates =
            new HashMap<>();

    private SelfRescueManager() {
    }

    public static void initialize() {

        SelfRescueNetworking.initialize();

        ServerTickEvents.END_SERVER_TICK.register(
                SelfRescueManager::tick
        );
    }

    public static void setKeyState(
            ServerPlayer player,
            boolean pressed
    ) {
        UUID uuid = player.getUUID();

        keyStates.put(uuid, pressed);

        /*
         * Soltar R cancela inmediatamente el progreso.
         */
        if (!pressed) {
            progress.remove(uuid);
        }
    }

    private static void tick(
            MinecraftServer server
    ) {

        /*
         * Si hay más de un jugador conectado,
         * First Aid New funciona completamente normal.
         */
        if (server.getPlayerList()
                .getPlayers()
                .size() != 1) {

            progress.clear();

            return;
        }

        ServerPlayer player =
                server.getPlayerList()
                        .getPlayers()
                        .getFirst();

        UUID uuid =
                player.getUUID();

        /*
         * La tecla debe estar siendo mantenida.
         */
        if (!keyStates.getOrDefault(uuid, false)) {

            progress.remove(uuid);

            return;
        }

        /*
         * Obtener el modelo real de First Aid New.
         */
        PlayerDamageModel damageModel =
                CommonUtils.getDamageModel(player)
                        instanceof PlayerDamageModel model
                                ? model
                                : null;

        if (damageModel == null) {

            progress.remove(uuid);

            return;
        }

        /*
         * El jugador debe estar en una condición
         * rescatable de First Aid New.
         */
        if (!damageModel.canBeRescued()) {

            progress.remove(uuid);

            return;
        }

        /*
         * Debe tener 3 vendas.
         */
        if (!hasBandages(
                player,
                REQUIRED_BANDAGES
        )) {

            progress.remove(uuid);

            return;
        }

        /*
         * Incrementar progreso.
         */
        int currentProgress =
                progress.getOrDefault(uuid, 0) + 1;

        progress.put(
                uuid,
                currentProgress
        );

        /*
         * Todavía no terminó.
         */
        if (currentProgress
                < RESCUE_DURATION_TICKS) {

            return;
        }

        /*
         * Comprobación final antes de consumir.
         */
        if (!hasBandages(
                player,
                REQUIRED_BANDAGES
        )) {

            progress.remove(uuid);

            return;
        }

        /*
         * Consumir 3 vendas.
         */
        removeBandages(
                player,
                REQUIRED_BANDAGES
        );

        /*
         * Usar la lógica oficial de First Aid New.
         *
         * Esto no crea una curación paralela.
         * First Aid New se encarga de la recuperación.
         */
        boolean rescued =
                damageModel.rescueFromCriticalState(
                        player,
                        null,
                        FirstAid.rescueWakeUpEnabled
                );

        if (rescued) {

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "You rescued yourself using 3 bandages."
                    )
            );
        }

        progress.remove(uuid);
    }

    private static boolean hasBandages(
            ServerPlayer player,
            int amount
    ) {

        int count = 0;

        for (
                int slot = 0;
                slot < player.getInventory()
                        .getContainerSize();
                slot++
        ) {

            ItemStack stack =
                    player.getInventory()
                            .getItem(slot);

            if (!stack.is(
                    RegistryObjects.BANDAGE.get()
            )) {
                continue;
            }

            count += stack.getCount();

            if (count >= amount) {
                return true;
            }
        }

        return false;
    }

    private static void removeBandages(
            ServerPlayer player,
            int amount
    ) {

        int remaining = amount;

        for (
                int slot = 0;
                slot < player.getInventory()
                        .getContainerSize()
                        && remaining > 0;
                slot++
        ) {

            ItemStack stack =
                    player.getInventory()
                            .getItem(slot);

            if (!stack.is(
                    RegistryObjects.BANDAGE.get()
            )) {
                continue;
            }

            int removed =
                    Math.min(
                            remaining,
                            stack.getCount()
                    );

            stack.shrink(removed);

            remaining -= removed;
        }
    }
}
