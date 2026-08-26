package net.damku1214.mojira.mixin;

import com.mojang.datafixers.util.Pair;
import net.damku1214.mojira.MojiraConfig;
import net.damku1214.mojira.mixin.accessor.MinecartBehaviorAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <p>
 *     Supplementary Mixin for bug MC-2714 (Minecarts pass through non-full-height blocks when snapping to rails)
 * </p>
 * <p>
 *     This is created to fix the discrepancy between the client and server-side positions by applying the new, experimental rail position detection to the renderer as well.
 * </p>
 */
@Mixin(OldMinecartBehavior.class)
public abstract class MinecartUpdateClientPosMixin {
    @Redirect(method = "getPosOffs", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private BlockState mojira$posOffsState(Level instance, BlockPos pos, double x, double y, double z) {
        return MojiraConfig.CONFIG.MC_2714.get() ? instance.getBlockState(BlockPos.containing(x, y - 0.1 - 1.0E-5F, z)) : instance.getBlockState(pos);
    }
    @Redirect(method = "getPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private BlockState mojira$posState(Level instance, BlockPos pos, double x, double y, double z) {
        return MojiraConfig.CONFIG.MC_2714.get() ? instance.getBlockState(BlockPos.containing(x, y - 0.1 - 1.0E-5F, z)) : instance.getBlockState(pos);
    }

    @ModifyVariable(method = "getPosOffs", at = @At(value = "NEW", target = "Lnet/minecraft/core/BlockPos;", ordinal = 1), name = "yt")
    private int mojira$posOffsYT(int yt, double x, double y, double z) {
        OldMinecartBehavior self = (OldMinecartBehavior) (Object) this;
        boolean isOnRail = self.level().getBlockState(BlockPos.containing(x, y - 0.1 - 1.0E-5F, z)).is(BlockTags.RAILS);
        return MojiraConfig.CONFIG.MC_2714.get() && isOnRail ? Mth.floor(y - 0.1 - 1.0E-5F) : yt;
    }
    @ModifyVariable(method = "getPos", at = @At(value = "NEW", target = "Lnet/minecraft/core/BlockPos;", ordinal = 1), name = "yt")
    private int mojira$posYT(int yt, double x, double y, double z) {
        OldMinecartBehavior self = (OldMinecartBehavior) (Object) this;
        boolean isOnRail = self.level().getBlockState(BlockPos.containing(x, y - 0.1 - 1.0E-5F, z)).is(BlockTags.RAILS);
        return MojiraConfig.CONFIG.MC_2714.get() && isOnRail ? Mth.floor(y - 0.1 - 1.0E-5F) : yt;
    }
}
