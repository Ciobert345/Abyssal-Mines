// src/main/java/com/tuonome/abyssalmines/ModDimensions.java

package com.tuonome.abyssalmines;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * Chiavi delle risorse per la dimensione Abyssal Mines.
 * Le chiavi corrispondono ai file JSON in data/abyssal_mines/dimension/.
 */
public class ModDimensions {

    /** Chiave della dimensione (usata per Level.dimension()) */
    public static final ResourceKey<Level> MINING_DIM_KEY =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(
                    AbyssalMines.MODID, "mining_dim"));

    /** Chiave del LevelStem (corrisponde a data/abyssal_mines/dimension/mining_dim.json) */
    public static final ResourceKey<LevelStem> MINING_DIM_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, ResourceLocation.fromNamespaceAndPath(
                    AbyssalMines.MODID, "mining_dim"));
}
