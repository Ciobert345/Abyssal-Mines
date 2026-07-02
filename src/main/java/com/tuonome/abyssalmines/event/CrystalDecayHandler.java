// src/main/java/com/tuonome/abyssalmines/event/CrystalDecayHandler.java

package com.tuonome.abyssalmines.event;

import com.tuonome.abyssalmines.AbyssalMinesConfig;
import com.tuonome.abyssalmines.ModDimensions;
import com.tuonome.abyssalmines.ModItems;
import com.tuonome.abyssalmines.item.VoidstoneCrystalItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class CrystalDecayHandler {
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (player.level().dimension().equals(ModDimensions.MINING_DIM_KEY)) {
            return;
        }
        int interval = AbyssalMinesConfig.VOIDSTONE_CRYSTAL_DECAY_INTERVAL_TICKS.get();
        if (player.tickCount % interval != 0) {
            return;
        }

        // Logica complessa: iteriamo tutti gli slot del player e facciamo decadere gli stack di cristalli fuori dimensione.
        decayInventorySection(player.getInventory().items);
        decayInventorySection(player.getInventory().armor);
        decayInventorySection(player.getInventory().offhand);
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer().level().isClientSide) {
            return;
        }

        ItemEntity itemEntity = event.getItemEntity();
        if (itemEntity.getItem().is(ModItems.VOIDSTONE_CRYSTAL.get())) {
            VoidstoneCrystalItem.cleanupLightBlock(itemEntity);
        }
    }

    private static void decayInventorySection(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(ModItems.VOIDSTONE_CRYSTAL.get())) {
                continue;
            }
            stack.shrink(1);
        }
    }
}
