package me.mrnavastar.fabric.client;

import me.mrnavastar.creorio.client.CreorioClient;
import net.fabricmc.api.ClientModInitializer;

public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CreorioClient.init();
    }
}
