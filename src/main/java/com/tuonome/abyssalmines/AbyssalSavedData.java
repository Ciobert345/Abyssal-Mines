// src/main/java/com/tuonome/abyssalmines/AbyssalSavedData.java

package com.tuonome.abyssalmines;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * SavedData persistente per la mod Abyssal Mines.
 *
 * Tiene traccia di:
 * - spawnStructureGenerated: se la struttura spawn.nbt è già stata generata
 * - spawnX, spawnY, spawnZ: le coordinate esatte in cui è stata posizionata
 */
public class AbyssalSavedData extends SavedData {

    public boolean spawnStructureGenerated = false;
    public int spawnX = 0;
    public int spawnY = 60;
    public int spawnZ = 0;

    public AbyssalSavedData() {}

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("SpawnStructureGenerated", spawnStructureGenerated);
        tag.putInt("SpawnX", spawnX);
        tag.putInt("SpawnY", spawnY);
        tag.putInt("SpawnZ", spawnZ);
        return tag;
    }

    public static AbyssalSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        AbyssalSavedData data = new AbyssalSavedData();
        data.spawnStructureGenerated = tag.getBoolean("SpawnStructureGenerated");
        data.spawnX = tag.getInt("SpawnX");
        data.spawnY = tag.getInt("SpawnY");
        data.spawnZ = tag.getInt("SpawnZ");
        return data;
    }
}
