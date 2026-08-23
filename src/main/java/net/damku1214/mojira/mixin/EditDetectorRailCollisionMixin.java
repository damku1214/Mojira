package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DetectorRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

/**
 * <p>
 *     Fixes MC-868 (Detector Rail switches junction before Minecart passes detector (happens only with minecarts of certain speed)).
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Detector rails detect collision with a minecart relative to the minecart's hitbox; this causes minecarts to collide with it
 *     when the minecart isn't even on the detector rail and cause unexpected behavior.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Change minecart detection so that the rail and minecart must have the same block position. <br>
 *     I am aware that this causes visual stuttering of the minecart in a setup that usually triggered the bug. <br>
 *     However, this is due to the minecart's interpolation during client rendering, and I decided to leave it be as it does not hinder server-side behavior. <br>
 *     Huge thanks to {@code FX - PR0CESS} for the solution code;
 *     their code can be found in the comments here: <a href="https://report.bugs.mojang.com/servicedesk/customer/portal/2/MC-868">LINK</a>
 * </p>
 */
@Mixin(DetectorRailBlock.class)
public abstract class EditDetectorRailCollisionMixin {
    @Shadow
    protected abstract <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(
            Level level, BlockPos pos, Class<T> type, Predicate<Entity> containerEntitySelector
    );

    @Redirect(method = "checkPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DetectorRailBlock;getInteractingMinecartOfType(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/lang/Class;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<AbstractMinecart> mojira$redirectGetMinecarts(DetectorRailBlock self, Level level, BlockPos pos, Class<AbstractMinecart> type, Predicate<AbstractMinecart> containerEntitySelector) {
        return getInteractingMinecartOfType(level, pos, type, e -> !MojiraConfig.CONFIG.MC_868.get() || e.blockPosition().equals(pos));
    }
}
