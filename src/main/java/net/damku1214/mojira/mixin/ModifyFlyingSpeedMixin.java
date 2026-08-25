package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <p>
 *     Fixes MC-2112 (Adjusted speed does not affect sprint-jumping/falling/floating with levitation/floating with NoGravity)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     {@link Player#getFlyingSpeed()} does not consider the player's movement speed attribute in its calculations.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Add said calculations as needed. Note that sprint-jumping does still add a little bit of more speed than on ground, <br>
 *     due to the instantaneous burst of speed sprinting gives. However, this is a far better improvement compared to original behavior.
 * </p>
 */
@Mixin(Player.class)
public abstract class ModifyFlyingSpeedMixin {
    @Inject(method = "getFlyingSpeed", at = @At("TAIL"), cancellable = true)
    private void mojira$getFlyingSpeed(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        double speedMultiplier = self.getAttributeValue(Attributes.MOVEMENT_SPEED) / self.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        cir.setReturnValue(MojiraConfig.CONFIG.MC_2112.get() ? cir.getReturnValue() * (float)speedMultiplier : cir.getReturnValue());
    }
}
