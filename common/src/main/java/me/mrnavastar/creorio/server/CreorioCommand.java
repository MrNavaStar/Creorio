package me.mrnavastar.creorio.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.mrnavastar.creorio.networking.CreorioChunkListS2C;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CreorioCommand {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, CommandManager.RegistrationEnvironment selection) {
        dispatcher.register(CommandManager.literal("creorio").requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("stats").executes(CreorioCommand::stats))
                .then(CommandManager.literal("debug").requires(ServerCommandSource::isExecutedByPlayer).executes(CreorioCommand::debug))
        );
    }

    private static int stats(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().getServer().getWorlds().forEach(world -> {
            ctx.getSource().sendMessage(Text.of(world.getRegistryKey().getValue() + " : " + Creorio.getCreorioStorage(world).getChunks().size() + " chunks"));
        });
        return 1;
    }

    private static int debug(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Creorio.CHANNEL.sendToPlayer(player, new CreorioChunkListS2C(Creorio.getCreorioStorage(player.getServerWorld()).getChunks()));
        return 1;
    }
}