package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-201 (Field of Vision does not turn at all with a turning minecart).
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     The absence of code that executes expected behavior.
 * </p>
 * <p>
 *    -- SOLUTION -- <br>
 *    Add yaw to the passenger when the minecart's yaw changes. <br>
 *    Additional code has been added via {@link MinecartInitialYRotMixin}, which adjusts the minecart's yaw when spawned in initially. <br>
 *    This was done because without it, the minecart's yaw would abruptly change when first moved and consequently, the player's yaw would too.
 * </p>
 */
@Mixin(Entity.class)
public abstract class MinecartPassengerYRotMixin {
    @Inject(method = "setYRot", at = @At("HEAD"))
    private void mojira$setYRot(float yRot, CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_201.get()) return;

        Entity self = (Entity) (Object) this;
        Entity passenger = self.getFirstPassenger();
        if (self instanceof AbstractMinecart && passenger != null && self.getYRot() != yRot) {
            passenger.setYRot((passenger.getYRot() + (yRot - self.getYRot()) * (((AbstractMinecart) self).getBehavior() instanceof NewMinecartBehavior ? -1 : 1)) % 360.0F);
        }
    }
}