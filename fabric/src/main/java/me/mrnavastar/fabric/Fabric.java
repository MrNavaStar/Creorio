package me.mrnavastar.fabric;

import net.fabricmc.api.ModInitializer;

import me.mrnavastar.creorio.server.Creorio;

public final class Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Creorio.init();
    }
}
