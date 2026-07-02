package com.tuonome.abyssalmines.block;

import com.tuonome.abyssalmines.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

public class VoidstoneOreBlock extends Block {
    public VoidstoneOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack tool = params.getParameter(LootContextParams.TOOL);
        Item toolItem = tool.getItem();
        
        if (toolItem == Items.DIAMOND_PICKAXE || toolItem == Items.NETHERITE_PICKAXE) {
            int count = 1 + params.getLevel().random.nextInt(3);
            drops.add(new ItemStack(ModItems.VOIDSTONE_CRYSTAL.get(), count));
        }
        
        return drops;
    }
}

