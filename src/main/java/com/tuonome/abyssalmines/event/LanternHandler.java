// src/main/java/com/tuonome/abyssalmines/event/LanternHandler.java

package com.tuonome.abyssalmines.event;

import org.slf4j.Logger;

import com.tuonome.abyssalmines.AbyssalMines;
import com.tuonome.abyssalmines.ModItems;
import com.tuonome.abyssalmines.item.AbyssalLanternItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class LanternHandler {
    private static final Logger LOGGER = AbyssalMines.LOGGER;
    private static final String LAST_LIGHT_POS_KEY = "abyssal_mines.lantern_last_light_pos";
    private static final int FUEL_CONSUME_EVERY_TICKS = 100;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        ItemStack lantern = findLantern(player);
        int strongFuel = lantern.isEmpty() ? 0 : AbyssalLanternItem.getStrongFuel(lantern);
        int normalFuel = lantern.isEmpty() ? 0 : AbyssalLanternItem.getFuel(lantern);

        if (player.isSpectator()) {
            tickInactiveLantern(player);
            return;
        }

        if (!lantern.isEmpty() && (strongFuel > 0 || normalFuel > 0)) {
            tickActiveLantern(player, lantern);
        } else {
            tickInactiveLantern(player);
        }
    }

    private static ItemStack findLantern(Player player) {
        // First check main hand and offhand for priority
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean mainIsLantern = main.is(ModItems.ABYSSAL_LANTERN.get());
        boolean offIsLantern = off.is(ModItems.ABYSSAL_LANTERN.get());

        // Check for strong fuel first
        if (mainIsLantern && AbyssalLanternItem.getStrongFuel(main) > 0) {
            return main;
        }
        if (offIsLantern && AbyssalLanternItem.getStrongFuel(off) > 0) {
            return off;
        }
        // Then check for normal fuel in hand
        if (mainIsLantern && AbyssalLanternItem.getFuel(main) > 0) {
            return main;
        }
        if (offIsLantern && AbyssalLanternItem.getFuel(off) > 0) {
            return off;
        }
        // Now check entire inventory for any active lantern
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ABYSSAL_LANTERN.get())) {
                if (AbyssalLanternItem.getStrongFuel(stack) > 0) {
                    return stack;
                }
            }
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ABYSSAL_LANTERN.get())) {
                if (AbyssalLanternItem.getFuel(stack) > 0) {
                    return stack;
                }
            }
        }
        // Check for any empty lantern in inventory
        if (mainIsLantern) {
            return main;
        }
        if (offIsLantern) {
            return off;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ABYSSAL_LANTERN.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void tickActiveLantern(Player player, ItemStack lantern) {
        int tick = player.tickCount;
        int strongFuel = AbyssalLanternItem.getStrongFuel(lantern);
        int normalFuel = AbyssalLanternItem.getFuel(lantern);

        // Auto-refuel: SOLO lato server!
        if (!player.level().isClientSide) {
            // Prima prova a ricaricare con cristalli forti
            ItemStack strongCrystal = AbyssalLanternItem.getStrongCrystalStack(lantern, player.registryAccess());
            while (!strongCrystal.isEmpty() && strongFuel < AbyssalLanternItem.MAX_FUEL) {
                strongCrystal.shrink(1);
                strongFuel += AbyssalLanternItem.FUEL_PER_STRONG_CRYSTAL;
                if (strongFuel > AbyssalLanternItem.MAX_FUEL) {
                    strongFuel = AbyssalLanternItem.MAX_FUEL;
                }
            }
            AbyssalLanternItem.setStrongCrystalStack(lantern, strongCrystal, player.registryAccess());

            // Poi prova a ricaricare con cristalli normali
            ItemStack normalCrystal = AbyssalLanternItem.getNormalCrystalStack(lantern, player.registryAccess());
            while (!normalCrystal.isEmpty() && normalFuel < AbyssalLanternItem.MAX_FUEL) {
                normalCrystal.shrink(1);
                normalFuel += AbyssalLanternItem.FUEL_PER_CRYSTAL;
                if (normalFuel > AbyssalLanternItem.MAX_FUEL) {
                    normalFuel = AbyssalLanternItem.MAX_FUEL;
                }
            }
            AbyssalLanternItem.setNormalCrystalStack(lantern, normalCrystal, player.registryAccess());

            // Aggiorna i livelli di carburante
            AbyssalLanternItem.setStrongFuel(lantern, strongFuel);
            AbyssalLanternItem.setFuel(lantern, normalFuel);
        }

        // Consumo carburante: SOLO lato server per evitare desync NBT
        if (!player.level().isClientSide && tick % FUEL_CONSUME_EVERY_TICKS == 0) {
            strongFuel = AbyssalLanternItem.getStrongFuel(lantern);
            normalFuel = AbyssalLanternItem.getFuel(lantern);
            if (strongFuel > 0) {
                AbyssalLanternItem.setStrongFuel(lantern, strongFuel - 1);
            } else if (normalFuel > 0) {
                AbyssalLanternItem.setFuel(lantern, normalFuel - 1);
            }
        }
    }

    private static void removeLight(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void tickInactiveLantern(Player player) {
        // Remove light
        if (!player.level().isClientSide && player.getPersistentData().contains(LAST_LIGHT_POS_KEY)) {
            BlockPos lastPos = BlockPos.of(player.getPersistentData().getLong(LAST_LIGHT_POS_KEY));
            removeLight(player.level(), lastPos);
            player.getPersistentData().remove(LAST_LIGHT_POS_KEY);
        }
    }
}
