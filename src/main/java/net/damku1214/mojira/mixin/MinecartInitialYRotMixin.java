package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.damku1214.mojira.mixin.accessor.MinecartBehaviorAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Supplementary Mixin for bug MC-201 (Field of Vision does not turn at all with a turning minecart).
 * </p>
 * <p>
 *     This is created to fix the sudden change of yaw of a minecart when first moved
 *     which prevents sudden changes in the passenger's yaw introduced by the bug fix.
 * </p>
 */
@Mixin(OldMinecartBehavior.class)
public abstract class MinecartInitialYRotMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void mojira$tick(CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_201.get()) return;

        OldMinecartBehavior self = (OldMinecartBehavior) (Object) this;
        AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).mojira$getMinecart();
        if (!self.level().isClientSide() && minecart.isFirstTick() && minecart.isOnRails()) {
            BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
            BlockState state = self.level().getBlockState(railPos);
            if (state.getBlock() instanceof BaseRailBlock railBlock) {
                switch (railBlock.getRailDirection(state, self.level(), railPos, minecart)) {
                    case NORTH_SOUTH, ASCENDING_SOUTH, ASCENDING_NORTH -> minecart.setYRot(90.0f);
                    case SOUTH_WEST -> minecart.setYRot(45.0f);
                    case NORTH_WEST -> minecart.setYRot(135.0f);
                    case NORTH_EAST -> minecart.setYRot(-135.0f);
                    case SOUTH_EAST -> minecart.setYRot(-45.0f);
                }
            }
        }
    }
}
