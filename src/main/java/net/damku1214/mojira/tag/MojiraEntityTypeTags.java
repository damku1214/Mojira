package net.damku1214.mojira.tag;

import net.damku1214.mojira.Mojira;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface MojiraEntityTypeTags {
    TagKey<EntityType<?>> BLOCK_LIKES = create("block_likes");

    TagKey<EntityType<?>> CATS = create("cats");
    TagKey<EntityType<?>> GHASTS = create("ghasts");
    TagKey<EntityType<?>> SQUIDS = create("squids");

    TagKey<EntityType<?>> BEARS = create("bears");

    TagKey<EntityType<?>> GUARDIANS = create("guardians");
    TagKey<EntityType<?>> HOGLINS = create("hoglins");
    TagKey<EntityType<?>> SLIMES = create("slimes");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Mojira.MOD_ID, name));
    }
}
