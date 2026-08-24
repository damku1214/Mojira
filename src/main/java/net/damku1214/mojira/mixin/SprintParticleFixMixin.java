package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <p>
 *     Fixes MC-1691 (Sprinting / falling on thin blocks uses the block below for the particles)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     {@link Entity#spawnSprintParticle()} checks the block position beneath the player with {@link Entity#getOnPosLegacy()}
 *     instead of {@link Entity#getOnPos()}.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Redirect {@code getOnPosLegacy} to {@code getOnPos} as it should be.
 * </p>
 */
@Mixin(Entity.class)
public abstract class SprintParticleFixMixin {
    @Redirect(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;"))
    private BlockPos mojira$getOnPosLegacy(Entity instance) {
        return MojiraConfig.CONFIG.MC_1691.get() ? instance.getOnPos() : instance.getOnPosLegacy();
    }
}
