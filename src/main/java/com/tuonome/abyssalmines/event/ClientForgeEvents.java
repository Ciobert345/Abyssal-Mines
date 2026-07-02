// src/main/java/com/tuonome/abyssalmines/event/ClientForgeEvents.java

package com.tuonome.abyssalmines.event;

import com.tuonome.abyssalmines.ModDimensions;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Event handler lato CLIENT per il bus FORGE della mod Abyssal Mines.
 */
@EventBusSubscriber(modid = "abyssal_mines", value = Dist.CLIENT)
public class ClientForgeEvents {

    /**
     * Forza la nebbia nera nella mining_dim.
     */
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        if (!mc.level.dimension().equals(ModDimensions.MINING_DIM_KEY)) {
            return;
        }

        event.setRed(0.0f);
        event.setGreen(0.0f);
        event.setBlue(0.0f);
    }
}
