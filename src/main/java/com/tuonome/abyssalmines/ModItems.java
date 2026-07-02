package com.tuonome.abyssalmines;

import com.tuonome.abyssalmines.item.AbyssalLanternItem;
import com.tuonome.abyssalmines.item.StrongVoidstoneCrystalItem;
import com.tuonome.abyssalmines.item.VoidstoneCrystalItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro centralizzato per tutti gli item della mod.
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AbyssalMines.MODID);

    public static final DeferredItem<BlockItem> ABYSSAL_ANCHOR_ITEM =
            ITEMS.registerSimpleBlockItem("abyssal_anchor", ModBlocks.ABYSSAL_ANCHOR);

    public static final DeferredItem<BlockItem> VOIDSTONE_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("voidstone_ore", ModBlocks.VOIDSTONE_ORE);

    public static final DeferredItem<BlockItem> STRONG_VOIDSTONE_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("strong_voidstone_ore", ModBlocks.STRONG_VOIDSTONE_ORE);

    public static final DeferredItem<VoidstoneCrystalItem> VOIDSTONE_CRYSTAL =
            ITEMS.register("voidstone_crystal", () -> new VoidstoneCrystalItem(
                    new Item.Properties()
                            .stacksTo(64)
            ));

    public static final DeferredItem<StrongVoidstoneCrystalItem> STRONG_VOIDSTONE_CRYSTAL =
            ITEMS.register("strong_voidstone_crystal", () -> new StrongVoidstoneCrystalItem(
                    new Item.Properties()
                            .stacksTo(64)
            ));

    public static final DeferredItem<AbyssalLanternItem> ABYSSAL_LANTERN =
            ITEMS.register("abyssal_lantern", () -> new AbyssalLanternItem(
                    new Item.Properties().stacksTo(1)
            ));
}
