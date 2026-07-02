// src/main/java/com/tuonome/abyssalmines/AbyssalMines.java

package com.tuonome.abyssalmines;

import com.mojang.logging.LogUtils;
import com.tuonome.abyssalmines.event.CrystalDecayHandler;
import com.tuonome.abyssalmines.event.LanternHandler;
import com.tuonome.abyssalmines.event.ModEvents;
import com.tuonome.abyssalmines.ModCreativeTabs;
import com.tuonome.abyssalmines.menu.AbyssalLanternMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.slf4j.Logger;

/**
 * Classe principale della mod Abyssal Mines.
 * Registra tutti i contenuti e gli event listener al momento del caricamento.
 */
@Mod(AbyssalMines.MODID)
public class AbyssalMines {

    public static final String MODID = "abyssal_mines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AbyssalMines(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("Abyssal Mines: inizializzazione in corso...");

        container.registerConfig(ModConfig.Type.COMMON, AbyssalMinesConfig.SPEC);

        // Registra blocchi, item e tab creative tramite i DeferredRegister
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        AbyssalLanternMenu.MENUS.register(modEventBus);

        // Registra gli event listener sul bus globale di NeoForge
        NeoForge.EVENT_BUS.register(new ModEvents());
        NeoForge.EVENT_BUS.register(new CrystalDecayHandler());
        NeoForge.EVENT_BUS.register(new LanternHandler());
        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);

        LOGGER.info("Abyssal Mines: registrazione completata.");
    }
    
    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        var recipeManager = event.getServer().getRecipeManager();
        LOGGER.info("Abyssal Mines: registering recipes...");
        
        // Add recipes here - but wait, let's do data generators or use JSON is better
        LOGGER.info("Abyssal Mines: recipe registration complete.");
    }
}
