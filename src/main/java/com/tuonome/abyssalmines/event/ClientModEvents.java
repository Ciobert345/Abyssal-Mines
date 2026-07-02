// src/main/java/com/tuonome/abyssalmines/event/ClientModEvents.java

package com.tuonome.abyssalmines.event;

import com.tuonome.abyssalmines.menu.AbyssalLanternMenu;
import com.tuonome.abyssalmines.screen.AbyssalLanternScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Event handler lato CLIENT per il bus MOD della mod Abyssal Mines.
 * Gestisce la registrazione di schermate e altri contenuti client-side.
 */
@EventBusSubscriber(modid = "abyssal_mines", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // Registra la schermata per il menu della lanterna
        event.register(AbyssalLanternMenu.ABYSSAL_LANTERN_MENU.get(), AbyssalLanternScreen::new);
    }
}
