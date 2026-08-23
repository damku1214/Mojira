package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-14 (Minecarts that occupy the same space maintain their momentum).
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     A minecart colliding with another minecart pushes both itself and the other minecart in opposite directions. <br>
 *     Upon collision, both minecarts push each other twice, meaning that the pushes ultimately cancel
 *     (e.g., if a minecart pushes itself by -1 and the other minecart by 1, upon collision of minecart A and B,
 *     A will push A by -1 and B by 1; B will push B by -1 and A by 1. Note that the net push amount for both minecarts are now 0).
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Only push the other minecart during collisions when the minecarts are inside each other;
 *     cases which the minecarts are far from each other are left the same to keep minecart collision behavior identical.
 * </p>
 */
@Mixin(AbstractMinecart.class)
public abstract class MinecartCollisionMomentumMixin {
    @Inject(method = "pushOtherMinecart", at = @At("HEAD"), cancellable = true)
    private void pushOtherMinecart(AbstractMinecart otherMinecart, double xa, double za, CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_14.get()) return;

        AbstractMinecart self = (AbstractMinecart) (Object) this;

        double xo;
        double zo;
        if (AbstractMinecart.useExperimentalMovement(self.level())) {
            xo = self.getDeltaMovement().x;
            zo = self.getDeltaMovement().z;
        } else {
            xo = otherMinecart.getX() - self.getX();
            zo = otherMinecart.getZ() - self.getZ();
        }

        Vec3 dir = new Vec3(xo, 0.0, zo).normalize();
        Vec3 facing = new Vec3(Mth.cos(self.getYRot() * (float) (Math.PI / 180.0)), 0.0, Mth.sin(self.getYRot() * (float) (Math.PI / 180.0))).normalize();
        double dot = Math.abs(dir.dot(facing));
        if (!(dot < 0.8F) || AbstractMinecart.useExperimentalMovement(self.level())) {
            Vec3 movement = self.getDeltaMovement();
            Vec3 entityMovement = otherMinecart.getDeltaMovement();
            if (otherMinecart.isFurnace() && !self.isFurnace()) {
                self.setDeltaMovement(movement.multiply(0.2, 1.0, 0.2));
                self.push(entityMovement.x - xa, 0.0, entityMovement.z - za);
                otherMinecart.setDeltaMovement(entityMovement.multiply(0.95, 1.0, 0.95));
            } else if (!otherMinecart.isFurnace() && self.isFurnace()) {
                otherMinecart.setDeltaMovement(entityMovement.multiply(0.2, 1.0, 0.2));
                otherMinecart.push(movement.x + xa, 0.0, movement.z + za);
                self.setDeltaMovement(movement.multiply(0.95, 1.0, 0.95));
            } else {
                double xdd = (entityMovement.x + movement.x) / 2.0;
                double zdd = (entityMovement.z + movement.z) / 2.0;
                self.setDeltaMovement(movement.multiply(0.2, 1.0, 0.2));
                if (Math.sqrt(xo * xo + zo * zo) >= 0.9) self.push(xdd - xa, 0.0, zdd - za);
                otherMinecart.setDeltaMovement(entityMovement.multiply(0.2, 1.0, 0.2));
                otherMinecart.push(xdd + xa, 0.0, zdd + za);
            }
        }
        ci.cancel();
    }
}