package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-711 (Tile ticks of connected redstone components might be executed in the wrong order when unloading/reloading chunks)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Redstone diodes had different behaviors of changing its state depending on its current state.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Changed redstone diode behavior such that it is consistent no matter its current state. <br>
 *     As I am not a redstone expert, this change mmy or may not break certain circuits outside my knowledge. <br>
 *     If you find a circuit that breaks due to this fix, please report it to the GitHub Issues tab.
 * </p>
 */
@Mixin(DiodeBlock.class)
public abstract class DiodeNeighborChunkCheckMixin {
    @Shadow
    protected abstract boolean shouldTurnOn(Level level, BlockPos pos, BlockState state);
    @Shadow
    protected abstract int getDelay(BlockState state);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void mojira$tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_711.get()) return;

        DiodeBlock self = (DiodeBlock) (Object) this;
        if (!self.isLocked(level, pos, state)) {
            boolean on = state.getValue(DiodeBlock.POWERED);
            boolean shouldTurnOn = shouldTurnOn(level, pos, state);
            level.setBlock(pos, state.setValue(DiodeBlock.POWERED, !on), Block.UPDATE_CLIENTS);
            if (on == shouldTurnOn) {
                level.scheduleTick(pos, self, getDelay(state), TickPriority.VERY_HIGH);
            }
        }

        ci.cancel();
    }
}