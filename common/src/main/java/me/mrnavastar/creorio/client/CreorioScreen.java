package me.mrnavastar.creorio.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CreorioScreen extends Screen {

    private final MinecraftClient client = MinecraftClient.getInstance();

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
        super.render(context, mouseX, mouseY, delta);

        this.renderBackground(context);
    }
}
