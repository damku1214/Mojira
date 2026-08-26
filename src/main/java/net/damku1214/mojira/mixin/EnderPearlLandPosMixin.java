package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * <p>
 *     Fixes MC-2164 (Ender pearls can teleport entities inside solid blocks)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Ender pearls teleport the player directly to its position, causing the player to clip in the blocks it hit and subsequently being able to pass through them.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Added boundary checks on the ender pearl and modify the player's teleported position accordingly.
 * </p>
 */
@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlLandPosMixin {
    @ModifyVariable(method = "onHit", at = @At("STORE"), name = "teleportPos")
    private Vec3 mojira$teleportPos(Vec3 teleportPos) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (!MojiraConfig.CONFIG.MC_2164.get() || self.getOwner() == null) return teleportPos;

        double[] totalOffsets = mojira$totalOffsets(self, self.getOwner());
        return new Vec3(teleportPos.x + totalOffsets[0], teleportPos.y + totalOffsets[1], teleportPos.z + totalOffsets[2]);
    }

    @ModifyVariable(method = "onHit", at = @At("STORE"), name = "event")
    private EntityTeleportEvent.EnderPearl mojira$event(EntityTeleportEvent.EnderPearl event) {
        ThrownEnderpearl self = (ThrownEnderpearl) (Object) this;
        if (!MojiraConfig.CONFIG.MC_2164.get() || event.getHitResult() == null) return event;

        double[] totalOffsets = mojira$totalOffsets(self, event.getPlayer());
        return EventHooks.onEnderPearlLand(event.getPlayer(), event.getTargetX() + totalOffsets[0], event.getTargetY() + totalOffsets[1], event.getTargetZ() + totalOffsets[2], event.getPearlEntity(), event.getAttackDamage(), event.getHitResult());
    }

    @Unique
    private double[] mojira$totalOffsets(ThrownEnderpearl self, Entity owner) {
        double totalXOffset = 0;
        double totalYOffset = 0;
        double totalZOffset = 0;
        double ownerBBRadius = owner.getBbWidth() / 2.0;
        for (double xOffset = -ownerBBRadius; xOffset <= ownerBBRadius; xOffset += owner.getBbWidth()) {
            for (double zOffset = -ownerBBRadius; zOffset <= ownerBBRadius; zOffset += owner.getBbWidth()) {
                if (mojira$isBoundaryBlockPosOccupied(self, xOffset, 0, zOffset)) {
                    totalXOffset -= xOffset;
                    totalZOffset -= zOffset;
                }
            }
        }

        for (double yOffset = 0; yOffset <= owner.getBbHeight(); yOffset += 0.1) {
            if (
                    mojira$isBoundaryBlockPosOccupied(self, 0, yOffset, 0) &&
                            ((mojira$isBoundaryBlockPosOccupied(self, ownerBBRadius, yOffset, ownerBBRadius) &&
                                    mojira$isBoundaryBlockPosOccupied(self, ownerBBRadius, yOffset, -ownerBBRadius) &&
                                    mojira$isBoundaryBlockPosOccupied(self, -ownerBBRadius, yOffset, ownerBBRadius) &&
                                    mojira$isBoundaryBlockPosOccupied(self, -ownerBBRadius, yOffset, -ownerBBRadius)) ||
                            !(mojira$isBoundaryBlockPosOccupied(self, ownerBBRadius, -self.getBbHeight(), ownerBBRadius) ||
                                    mojira$isBoundaryBlockPosOccupied(self, ownerBBRadius, -self.getBbHeight(), -ownerBBRadius) ||
                                    mojira$isBoundaryBlockPosOccupied(self, -ownerBBRadius, -self.getBbHeight(), ownerBBRadius) ||
                                    mojira$isBoundaryBlockPosOccupied(self, -ownerBBRadius, -self.getBbHeight(), -ownerBBRadius)))
            ) {
                totalYOffset = yOffset - owner.getBbHeight();
                break;
            }
        }

        return new double[]{Mth.clamp(totalXOffset, -ownerBBRadius, ownerBBRadius), totalYOffset, Mth.clamp(totalZOffset, -ownerBBRadius, ownerBBRadius)};
    }

    @Unique
    private boolean mojira$isBoundaryBlockPosOccupied(ThrownEnderpearl self, double xOffset, double yOffset, double zOffset) {
        Vec3 pos = new Vec3(self.position().x + xOffset, self.position().y + yOffset, self.position().z + zOffset);
        BlockPos blockPos = BlockPos.containing(pos);
        VoxelShape blockShape = self.level().getBlockState(blockPos).getCollisionShape(self.level(), blockPos);
        Vec3 offsetFromBlock = new Vec3(pos.x - blockPos.getX(), pos.y - blockPos.getY(), pos.z - blockPos.getZ());
        return !blockShape.isEmpty() && blockShape.bounds().contains(offsetFromBlock);
    }
}
