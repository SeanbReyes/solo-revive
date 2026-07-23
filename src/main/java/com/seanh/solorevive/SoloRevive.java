package com.seanh.solorevive;

import net.fabricmc.api.ModInitializer;

public final class SoloRevive implements ModInitializer {

    public static final String MOD_ID = "solo_revive";

    @Override
    public void onInitialize() {
        SelfRescueManager.initialize();
    }
}
