package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <p>
 *     Supplementary Mixin for bug MC-779 (Some entities are displayed outside of their spawner).
 * </p>
 * <p>
 *     This is created to strip some tags of some entities when put in a spawner that cause them to render outside of the spawner.
 * </p>
 */
@Mixin(BaseSpawner.class)
public abstract class SpawnerIgnoreTagsMixin {
    @Inject(method = "getOrCreateDisplayEntity", at = @At("RETURN"))
    private void mojira$getOrCreateDisplayEntity(Level level, BlockPos pos, CallbackInfoReturnable<Entity> cir) {
        if (!MojiraConfig.CONFIG.MC_779.get()) return;

        Entity entity = cir.getReturnValue();
        if (entity instanceof AbstractMinecart minecart) {
            minecart.setDisplayOffset(0);
        } else if (entity instanceof WitherBoss wither) {
            wither.setInvulnerableTicks(0);
        }
    }
}