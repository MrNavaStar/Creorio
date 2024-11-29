package me.mrnavastar.creorio.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrnavastar.creorio.Creorio;
import me.mrnavastar.creorio.inf.IChunkTicketManager;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.collection.SortedArraySet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(ChunkTicketManager.class)
public class ChunkTicketManagerMixin implements IChunkTicketManager {

    @Shadow @Final private Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition;

    @Unique
    public boolean creorio$isLoadedByCreorio(long l) {
        return Optional.ofNullable(ticketsByPosition.get(l))
                .map(tickets -> tickets.stream()
                        .anyMatch(ticket -> ticket.getType().equals(Creorio.TICKET)))
                .orElse(false);
    }

    //TODO: Check if these mixins are needed or not
    /*@ModifyReturnValue(method = "shouldTickEntities", at = @At("RETURN"))
    private boolean shouldTickEntities(boolean original, @Local(argsOnly = true) long pos) {
        if (original) return true;
        return creorio$isLoadedByCreorio(pos);
    }

    @ModifyReturnValue(method = "shouldTickBlocks", at = @At("RETURN"))
    private boolean shouldTickBlocks(boolean original, @Local(argsOnly = true) long pos) {
        if (original) return true;
        return creorio$isLoadedByCreorio(pos);
    }*/
}
