package me.mrnavastar.creorio.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

public class CreorioScreen extends Screen {

    public static void setup() {
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (CreorioClient.SCREEN_KEY.wasPressed()) client.setScreenAndRender(new CreorioScreen());
        });
    }

    protected CreorioScreen() {
        super(Text.of("Creorio"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        //this.renderBackground(context);

        ChunkRenderer.render(context.getMatrices(), new ChunkPos(0, 0));
    }
}
