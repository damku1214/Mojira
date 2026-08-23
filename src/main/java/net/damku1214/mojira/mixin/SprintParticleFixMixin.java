package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class SprintParticleFixMixin {
    @Redirect(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;"))
    private BlockPos mojira$getOnPosLegacy(Entity instance) {
        return MojiraConfig.CONFIG.MC_1691.get() ? instance.getOnPos() : instance.getOnPosLegacy();
    }
}
