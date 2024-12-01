package me.mrnavastar.creorio;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CreorioCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, CommandManager.RegistrationEnvironment selection) {
        dispatcher.register(CommandManager.literal("creorio")
                .then(CommandManager.literal("stats").executes(CreorioCommand::stats))
        );
    }

    private static Text getMessage(ServerWorld world) {
        return Text.of(world.getRegistryKey().getValue() + " currently has " + Creorio.getCreorioStorage(world).getChunks().size() + " chunks loaded by creorio");
    }

    //TODO: Make this work in the server console
    private static int stats(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player != null) {
            player.sendMessage(getMessage(player.getServerWorld()));
            return 1;
        }
        return 1;
    }
}
