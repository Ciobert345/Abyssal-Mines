// src/main/java/com/tuonome/abyssalmines/item/VoidstoneCrystalItem.java

package com.tuonome.abyssalmines.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VoidstoneCrystalItem extends Item {
    private static final String LIGHT_POS_KEY = "abyssal_mines.voidstone_crystal_light_pos";
    private static final int LIGHT_LEVEL = 7;

    public VoidstoneCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide) {
            return false;
        }
        updateLightBlock(entity);
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        if (!itemEntity.level().isClientSide) {
            cleanupLightBlock(itemEntity);
        }
    }

    public static void cleanupLightBlock(ItemEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(LIGHT_POS_KEY)) {
            return;
        }
        BlockPos pos = BlockPos.of(data.getLong(LIGHT_POS_KEY));
        data.remove(LIGHT_POS_KEY);
        clearIfOurLight(entity.level(), pos);
    }

    private static void updateLightBlock(ItemEntity entity) {
        CompoundTag data = entity.getPersistentData();
        BlockPos newPos = entity.blockPosition();

        if (data.contains(LIGHT_POS_KEY)) {
            BlockPos oldPos = BlockPos.of(data.getLong(LIGHT_POS_KEY));
            if (!oldPos.equals(newPos)) {
                clearIfOurLight(entity.level(), oldPos);
            }
        }

        data.putLong(LIGHT_POS_KEY, newPos.asLong());

        BlockState state = entity.level().getBlockState(newPos);
        if (!state.isAir() && !state.is(Blocks.LIGHT)) {
            return;
        }

        BlockState lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL);
        if (!state.equals(lightState)) {
            entity.level().setBlock(newPos, lightState, 3);
        }
    }

    private static void clearIfOurLight(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}

