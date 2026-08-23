package net.damku1214.mojira.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.damku1214.mojira.MojiraConfig;
import net.damku1214.mojira.tag.MojiraEntityTypeTags;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * <p>
 *     Fixes MC-779 (Some entities are displayed outside of their spawner).
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Some entities' models extrude out of their hitboxes, causing the hitbox-relative scaling code to not ensure that the entity is
 *     inside the spawner at all times.
 * </p>
 * <p>
 *    -- SOLUTION -- <br>
 *    Add specific orientation edits to certain entities before the entity is rendered in the spawner.
 * </p>
 */
@Mixin(SpawnerRenderer.class)
public abstract class TransformSpawnerEntityMixin {
    @Inject(method = "submitEntityInSpawner", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private static void mojira$submitEntityInSpawner(
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            EntityRenderState displayEntity,
            EntityRenderDispatcher entityRenderer,
            float spin,
            float scale,
            CameraRenderState camera,
            CallbackInfo ci
    ) {
        List<TagKey<EntityType<?>>> tags = displayEntity.entityType.getTags().toList();

        if ((MojiraConfig.CONFIG.MC_779.get() || MojiraConfig.CONFIG.MC_195599.get()) && displayEntity.entityType == EntityTypes.LIGHTNING_BOLT) {
            poseStack.scale(0.01f, 0.01f, 0.01f);
        }

        if (!MojiraConfig.CONFIG.MC_779.get()) return;

        // Non-living entities
        if (tags.contains(EntityTypeTags.ARROWS)) {
            poseStack.translate(0, 0.5f, 0);
        } else if (tags.contains(Tags.EntityTypes.BOATS)) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.END_CRYSTAL) {
            poseStack.scale(0.9f, 0.9f, 0.9f);
            poseStack.translate(0, 1.0f, 0);
        } else if (tags.contains(MojiraEntityTypeTags.BLOCK_LIKES)) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.FIREBALL) {
            poseStack.scale(0.9f, 0.9f, 0.9f);
        } else if (displayEntity.entityType == EntityTypes.DRAGON_FIREBALL) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        } else if (tags.contains(Tags.EntityTypes.MINECARTS)) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.PAINTING) {
            poseStack.scale(0.35f, 0.35f, 0.35f);
            poseStack.translate(0, 1.0f, 0);
        } else if (displayEntity.entityType == EntityTypes.TRIDENT) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(60.0f));
        }

        // Passive Mobs
        else if (displayEntity.entityType == EntityTypes.AXOLOTL) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (tags.contains(MojiraEntityTypeTags.CATS)) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
        } else if (tags.contains(MojiraEntityTypeTags.SQUIDS)) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.translate(0, 1.0f, 0);
        } else if (tags.contains(MojiraEntityTypeTags.GHASTS)) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, 3.5f, 0);
        } else if (displayEntity.entityType == EntityTypes.HORSE) {
            poseStack.scale(0.9f, 0.9f, 0.9f);
        } else if (displayEntity.entityType == EntityTypes.MOOSHROOM) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.SNIFFER) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        } else if (displayEntity.entityType == EntityTypes.SULFUR_CUBE) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        } else if (displayEntity.entityType == EntityTypes.TURTLE) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
            poseStack.translate(0, 0.5f, 0);
        }

        // Neutral Mobs
        else if (displayEntity.entityType == EntityTypes.DOLPHIN) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
        } else if (displayEntity.entityType == EntityTypes.FOX) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
        } else if (tags.contains(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR)) {
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.translate(0, 0.5f, 0);
        } else if (tags.contains(MojiraEntityTypeTags.BEARS)) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        }

        // Hostile Mobs
        else if (tags.contains(MojiraEntityTypeTags.GUARDIANS)) {
            poseStack.scale(0.325f, 0.325f, 0.325f);
            poseStack.translate(0, 1.5f, 0);
        } else if (tags.contains(MojiraEntityTypeTags.HOGLINS)) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (tags.contains(MojiraEntityTypeTags.SLIMES)) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.PHANTOM) {
            poseStack.scale(0.7f, 0.7f, 0.7f);
        } else if (displayEntity.entityType == EntityTypes.RAVAGER) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        } else if (displayEntity.entityType == EntityTypes.SILVERFISH) {
            poseStack.scale(0.9f, 0.9f, 0.9f);
            poseStack.translate(0, 0.5f, 0);
        } else if (displayEntity.entityType == EntityTypes.SHULKER) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        }
    }
}
