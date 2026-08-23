package net.damku1214.mojira.mixin;

import net.damku1214.mojira.Mojira;
import net.damku1214.mojira.MojiraConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-63 (Player data and region files not written/saved to disk simultaneously, causing item loss/duplication)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Player data saving and chunk data saving are asynchronous; upon dying, chunk data saves due to the addition of item entities
 *     but player data remains untouched. <br>
 *     If the game crashes in this state, the saved data now holds the 'new' chunk data and the 'old' player data,
 *     resulting in duplicated items.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     While not being a perfect and concrete solution, the current fix is to force player data to be saved upon death. <br>
 *     Theoretically, there are other instances where the chunk data can save while the player data doesn't
 *     (e.g., crashing after editing a container's content). <br>
 *     However, most attempts of recreating this issue requires the game to pause,
 *     which then triggers an auto save, keeping both chunk and player data up to date again. <br>
 *     The only major case where this auto save is avoidable is when the player exits the window on the death screen,
 *     where leaving the window does not pause the game and therefore does not trigger an auto save.
 * </p>
 */
@Mixin(ServerPlayer.class)
public abstract class PlayerDeathDataSaveMixin {
    @Inject(method = "die", at = @At("TAIL"))
    private void mojira$die(DamageSource source, CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_63.get()) return;

        ServerPlayer player = (ServerPlayer) (Object) this;
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        try {
            PlayerList playerList = server.getPlayerList();
            mojira$save(playerList, player);
        } catch (Exception e) {
            Mojira.LOGGER.error("Failed to force player data save on death for {} (MC-63 mitigation); falling back to normal autosave timing.", player.getGameProfile().name());
        }
    }

    @Unique
    private void mojira$save(PlayerList playerList, ServerPlayer player) {
        playerList.getPlayerIo().save(player);
        ServerStatsCounter stats = playerList.getPlayerStats(player);
        stats.save();

        PlayerAdvancements advancements = playerList.getPlayerAdvancements(player);
        advancements.save();
    }
}
