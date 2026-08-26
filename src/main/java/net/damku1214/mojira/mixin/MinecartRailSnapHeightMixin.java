package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-2714 (Minecarts pass through non-full-height blocks when snapping to rails)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     The code for correct rail snapping only exists when enabled the experimental minecart features.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Made the snapping height fix active regardless of usage of experimental features.
 * </p>
 */
@Mixin(AbstractMinecart.class)
public abstract class MinecartRailSnapHeightMixin {
    @Redirect(method = "getCurrentBlockPosOrRailBelow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;useExperimentalMovement(Lnet/minecraft/world/level/Level;)Z"))
    private boolean mojira$useExperimentalMovement(Level level) {
        return MojiraConfig.CONFIG.MC_2714.get();
    }
}
