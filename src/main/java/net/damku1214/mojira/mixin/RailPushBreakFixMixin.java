package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * <p>
 *     Fixes MC-2023 (Minecart rails break when moved with a block under certain conditions)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     {@link BaseRailBlock#neighborChanged} fires when the rail's supporting block is pushed before itself, causing the rail to break.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Made it so the rail does not break if the new block underneath it is a moving piston.
 * </p>
 */
@Mixin(BaseRailBlock.class)
public abstract class RailPushBreakFixMixin {
    @Inject(method = "shouldBeRemoved", at = @At("HEAD"), cancellable = true)
    private static void mojira$shouldBeRemoved(BlockPos pos, Level level, RailShape shape, CallbackInfoReturnable<Boolean> cir) {
        List<RailShape> ascendingShapes = List.of(RailShape.ASCENDING_EAST, RailShape.ASCENDING_WEST, RailShape.ASCENDING_NORTH, RailShape.ASCENDING_SOUTH);
        if (MojiraConfig.CONFIG.MC_2023.get() && !ascendingShapes.contains(shape) && !BaseRailBlock.canSupportRigidBlock(level, pos.below()) && level.getBlockState(pos.below()).getBlock() instanceof MovingPistonBlock) {
            cir.setReturnValue(false);
        }
    }
}
