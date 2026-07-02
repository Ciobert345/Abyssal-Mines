package com.tuonome.abyssalmines.event;

import com.tuonome.abyssalmines.AbyssalMines;
import com.tuonome.abyssalmines.ModDimensions;
import com.tuonome.abyssalmines.ModItems;
import com.tuonome.abyssalmines.item.AbyssalLanternItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

/**
 * Eventi server per Abyssal Mines.
 */
public class ModEvents {
    private static final Logger LOGGER = AbyssalMines.LOGGER;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().dimension().equals(ModDimensions.MINING_DIM_KEY)) {
            boolean hasActiveStrongLantern = hasActiveStrongLantern(player);
            boolean hasActiveNormalLantern = hasActiveNormalLantern(player);

            if (hasActiveStrongLantern) {
                // No blindness, no darkness - normal vision
                player.removeEffect(MobEffects.BLINDNESS);
                player.removeEffect(MobEffects.DARKNESS);
            } else if (hasActiveNormalLantern) {
                // No blindness, constant darkness (duration 400 ticks = 20 seconds, renew every tick to keep constant)
                player.removeEffect(MobEffects.BLINDNESS);
                // Add if not present or if about to expire (less than 100 ticks left)
                MobEffectInstance currentDarkness = player.getEffect(MobEffects.DARKNESS);
                if (currentDarkness == null || currentDarkness.getDuration() < 100) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DARKNESS,
                            400,
                            0,
                            true,
                            false,
                            false));
                }
            } else {
                // Constant blindness (renew often to keep it constant)
                player.removeEffect(MobEffects.DARKNESS);
                MobEffectInstance currentBlindness = player.getEffect(MobEffects.BLINDNESS);
                if (currentBlindness == null || currentBlindness.getDuration() < 100) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.BLINDNESS,
                            400,
                            0,
                            true,
                            false,
                            false));
                }
            }
        } else {
            // Remove all effects when leaving dimension
            player.removeEffect(MobEffects.BLINDNESS);
            player.removeEffect(MobEffects.DARKNESS);
        }
    }

    private static boolean hasActiveStrongLantern(ServerPlayer player) {
        // Check main hand/offhand first
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if ((main.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getStrongFuel(main) > 0) ||
                (off.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getStrongFuel(off) > 0)) {
            return true;
        }
        // Now check entire inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getStrongFuel(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveNormalLantern(ServerPlayer player) {
        // Check main hand/offhand first
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if ((main.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getFuel(main) > 0) ||
                (off.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getFuel(off) > 0)) {
            return true;
        }
        // Now check entire inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ABYSSAL_LANTERN.get()) && AbyssalLanternItem.getFuel(stack) > 0) {
                return true;
            }
        }
        return false;
    }
}
