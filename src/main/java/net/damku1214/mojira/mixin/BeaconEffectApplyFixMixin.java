package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * <p>
 *     Fixes MC-2440 (Beacons keep higher level effects when partially destroyed)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     There is no code in {@link BeaconBlockEntity#applyEffects(Level, BlockPos, int, Holder, Holder)} that checks that the beacon's level is valid for its primary effect.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Added code that does so.
 * </p>
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconEffectApplyFixMixin {
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void mojira$applyEffects(Level level, BlockPos worldPosition, int levels, @Nullable Holder<MobEffect> primaryPower, @Nullable Holder<MobEffect> secondaryPower, CallbackInfo ci) {
        Map<Holder<MobEffect>, Integer> effect2LevelMap = Map.of(
                MobEffects.SPEED, 1,
                MobEffects.HASTE, 1,
                MobEffects.RESISTANCE, 2,
                MobEffects.JUMP_BOOST, 2,
                MobEffects.STRENGTH, 3
        );
        if (MojiraConfig.CONFIG.MC_2440.get() && levels < effect2LevelMap.get(primaryPower)) ci.cancel();
    }
}
