// src/main/java/com/tuonome/abyssalmines/AbyssalMinesConfig.java

package com.tuonome.abyssalmines;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AbyssalMinesConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue VOIDSTONE_CRYSTAL_DECAY_INTERVAL_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("items");
        VOIDSTONE_CRYSTAL_DECAY_INTERVAL_TICKS = builder
                .defineInRange("voidstoneCrystalDecayIntervalTicks", 6000, 20, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }

    private AbyssalMinesConfig() {}
}

