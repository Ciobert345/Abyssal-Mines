package com.tuonome.abyssalmines;

import com.tuonome.abyssalmines.block.AbyssalAnchorBlock;
import com.tuonome.abyssalmines.block.StrongVoidstoneOreBlock;
import com.tuonome.abyssalmines.block.VoidstoneOreBlock;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro centralizzato per tutti i blocchi della mod.
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(AbyssalMines.MODID);

    public static final DeferredBlock<AbyssalAnchorBlock> ABYSSAL_ANCHOR =
            BLOCKS.register("abyssal_anchor", () -> new AbyssalAnchorBlock(
                    BlockBehaviour.Properties.of()
                            .strength(50f, 1200f)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
            ));

    public static final DeferredBlock<VoidstoneOreBlock> VOIDSTONE_ORE =
            BLOCKS.register("voidstone_ore", () -> new VoidstoneOreBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.0f, 9.0f)
                            .sound(SoundType.DEEPSLATE)
            ));

    public static final DeferredBlock<StrongVoidstoneOreBlock> STRONG_VOIDSTONE_ORE =
            BLOCKS.register("strong_voidstone_ore", () -> new StrongVoidstoneOreBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f, 10.0f)
                            .sound(SoundType.DEEPSLATE)
            ));
}
