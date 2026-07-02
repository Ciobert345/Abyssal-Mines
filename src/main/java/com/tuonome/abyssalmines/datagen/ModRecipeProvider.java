package com.tuonome.abyssalmines.datagen;

import com.tuonome.abyssalmines.ModBlocks;
import com.tuonome.abyssalmines.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ABYSSAL_LANTERN.get())
                .pattern("CCC")
                .pattern("CIC")
                .pattern(" I ")
                .define('C', ModItems.VOIDSTONE_CRYSTAL.get())
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_crystal", has(ModItems.VOIDSTONE_CRYSTAL.get()))
                .save(pRecipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ABYSSAL_ANCHOR.get(), 4)
                .pattern(" D ")
                .pattern("DOD")
                .pattern(" D ")
                .define('D', Blocks.DEEPSLATE)
                .define('O', Blocks.OBSIDIAN)
                .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
                .save(pRecipeOutput);
    }
}
