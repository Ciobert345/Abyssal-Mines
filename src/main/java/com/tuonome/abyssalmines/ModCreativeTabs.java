package com.tuonome.abyssalmines;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro della scheda della modalità creativa per Abyssal Mines.
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AbyssalMines.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ABYSSAL_MINES_TAB = CREATIVE_MODE_TABS.register(
            "abyssal_mines_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> ModItems.ABYSSAL_ANCHOR_ITEM.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.abyssal_mines.main"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ABYSSAL_ANCHOR_ITEM.get());
                        output.accept(ModItems.VOIDSTONE_ORE_ITEM.get());
                        output.accept(ModItems.STRONG_VOIDSTONE_ORE_ITEM.get());
                        output.accept(ModItems.VOIDSTONE_CRYSTAL.get());
                        output.accept(ModItems.STRONG_VOIDSTONE_CRYSTAL.get());
                        output.accept(ModItems.ABYSSAL_LANTERN.get());
                    })
                    .build()
    );
}
