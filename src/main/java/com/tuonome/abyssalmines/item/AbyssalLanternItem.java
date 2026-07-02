// src/main/java/com/tuonome/abyssalmines/item/AbyssalLanternItem.java

package com.tuonome.abyssalmines.item;

import com.tuonome.abyssalmines.menu.AbyssalLanternMenu;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class AbyssalLanternItem extends Item {
    public static final String FUEL_TAG = "fuel";
    public static final String STRONG_FUEL_TAG = "strong_fuel";
    public static final String NORMAL_CRYSTAL_SLOT_TAG = "normal_crystal";
    public static final String STRONG_CRYSTAL_SLOT_TAG = "strong_crystal";
    public static final int MAX_FUEL = 64;
    public static final int FUEL_PER_CRYSTAL = 16;
    public static final int FUEL_PER_STRONG_CRYSTAL = 8;

    public AbyssalLanternItem(Properties properties) {
        super(properties);
    }

    public static int getFuel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return 0;
        }
        CompoundTag tag = data.copyTag();
        return Math.max(0, tag.getInt(FUEL_TAG));
    }

    public static int getStrongFuel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return 0;
        }
        CompoundTag tag = data.copyTag();
        return Math.max(0, tag.getInt(STRONG_FUEL_TAG));
    }

    public static int getTotalFuel(ItemStack stack) {
        return getStrongFuel(stack) + getFuel(stack);
    }

    public static void setFuel(ItemStack stack, int fuel) {
        int clamped = Math.max(0, Math.min(MAX_FUEL, fuel));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(FUEL_TAG, clamped));
    }

    public static void setStrongFuel(ItemStack stack, int fuel) {
        int clamped = Math.max(0, Math.min(MAX_FUEL, fuel));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(STRONG_FUEL_TAG, clamped));
    }

    public static int addFuel(ItemStack stack, int fuelToAdd) {
        int fuel = getFuel(stack);
        int newFuel = Math.min(MAX_FUEL, fuel + Math.max(0, fuelToAdd));
        setFuel(stack, newFuel);
        return newFuel - fuel;
    }

    public static int addStrongFuel(ItemStack stack, int fuelToAdd) {
        int fuel = getStrongFuel(stack);
        int newFuel = Math.min(MAX_FUEL, fuel + Math.max(0, fuelToAdd));
        setStrongFuel(stack, newFuel);
        return newFuel - fuel;
    }

    public static ItemStack getNormalCrystalStack(ItemStack stack, HolderLookup.Provider provider) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.contains(NORMAL_CRYSTAL_SLOT_TAG)) {
            return ItemStack.parseOptional(provider, data.copyTag().getCompound(NORMAL_CRYSTAL_SLOT_TAG));
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getStrongCrystalStack(ItemStack stack, HolderLookup.Provider provider) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.contains(STRONG_CRYSTAL_SLOT_TAG)) {
            return ItemStack.parseOptional(provider, data.copyTag().getCompound(STRONG_CRYSTAL_SLOT_TAG));
        }
        return ItemStack.EMPTY;
    }

    public static void setNormalCrystalStack(ItemStack stack, ItemStack crystalStack, HolderLookup.Provider provider) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (crystalStack.isEmpty()) {
                tag.remove(NORMAL_CRYSTAL_SLOT_TAG);
            } else {
                tag.put(NORMAL_CRYSTAL_SLOT_TAG, crystalStack.save(provider));
            }
        });
    }

    public static void setStrongCrystalStack(ItemStack stack, ItemStack crystalStack, HolderLookup.Provider provider) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (crystalStack.isEmpty()) {
                tag.remove(STRONG_CRYSTAL_SLOT_TAG);
            } else {
                tag.put(STRONG_CRYSTAL_SLOT_TAG, crystalStack.save(provider));
            }
        });
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int totalFuel = getTotalFuel(stack);
        int maxTotalFuel = MAX_FUEL * 2; // MAX per each fuel type
        return Math.round(13.0F * totalFuel / maxTotalFuel);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int strongFuel = getStrongFuel(stack);
        if (strongFuel > 0) {
            return 0xFF55FF; // Viola per carburante forte
        }
        if (getFuel(stack) > 0) {
            return 0x55FFFF; // Ciano per carburante normale
        }
        return 0x555555; // Grigio per vuoto
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.openMenu(new AbyssalLanternMenu.Provider(hand));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int normalFuel = getFuel(stack);
        int strongFuel = getStrongFuel(stack);
        tooltipComponents.add(Component.translatable("tooltip.abyssal_mines.abyssal_lantern_fuel_normal", normalFuel, MAX_FUEL));
        tooltipComponents.add(Component.translatable("tooltip.abyssal_mines.abyssal_lantern_fuel_strong", strongFuel, MAX_FUEL));
    }
}
