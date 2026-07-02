// src/main/java/com/tuonome/abyssalmines/menu/AbyssalLanternMenu.java

package com.tuonome.abyssalmines.menu;

import com.tuonome.abyssalmines.AbyssalMines;
import com.tuonome.abyssalmines.ModItems;
import com.tuonome.abyssalmines.item.AbyssalLanternItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

public class AbyssalLanternMenu extends AbstractContainerMenu {
    private static final Logger LOGGER = AbyssalMines.LOGGER;
    // Registro MenuType (MOD BUS)
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AbyssalMines.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AbyssalLanternMenu>> ABYSSAL_LANTERN_MENU =
            MENUS.register("abyssal_lantern", () -> IMenuTypeExtension.create(AbyssalLanternMenu::new));

    // Due slot separati per i due tipi di cristallo!
    private final Container crystalContainer = new SimpleContainer(2);
    private final Player player;
    private final boolean mainHand;

    public AbyssalLanternMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, data.readByte() == 0);
    }

    public AbyssalLanternMenu(int containerId, Inventory playerInventory, boolean mainHand) {
        super(ABYSSAL_LANTERN_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.mainHand = mainHand;

        // Carica i cristalli salvati nella lampada nel container!
        ItemStack lantern = getLanternStack();
        if (!lantern.isEmpty()) {
            crystalContainer.setItem(0, AbyssalLanternItem.getNormalCrystalStack(lantern, player.registryAccess()));
            crystalContainer.setItem(1, AbyssalLanternItem.getStrongCrystalStack(lantern, player.registryAccess()));
        }

        // Slot per cristallo normale a sinistra
        addSlot(new CrystalSlot(crystalContainer, 0, 62, 35, true));
        // Slot per cristallo forte a destra
        addSlot(new CrystalSlot(crystalContainer, 1, 98, 35, false));

        int startX = 8;
        int startY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, startX + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!player.level().isClientSide) {
            saveCrystalsToLantern();
            convertCrystalsIntoFuel();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        int crystalStart = 0;
        int crystalEndExclusive = 2;
        int playerInvStart = crystalEndExclusive;
        int playerInvEndExclusive = slots.size();

        if (index < crystalEndExclusive) {
            if (!moveItemStackTo(stack, playerInvStart, playerInvEndExclusive, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack.is(ModItems.VOIDSTONE_CRYSTAL.get())) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(ModItems.STRONG_VOIDSTONE_CRYSTAL.get())) {
                if (!moveItemStackTo(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (!player.level().isClientSide) {
            saveCrystalsToLantern();
            convertCrystalsIntoFuel();
        }

        return result;
    }

    @Override
    public void clicked(int slotId, int dragType, net.minecraft.world.inventory.ClickType clickType, Player player) {
        super.clicked(slotId, dragType, clickType, player);
        if (!player.level().isClientSide) {
            saveCrystalsToLantern();
            convertCrystalsIntoFuel();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveCrystalsToLantern();
        }
    }

    private void saveCrystalsToLantern() {
        ItemStack lantern = getLanternStack();
        if (!lantern.isEmpty()) {
            AbyssalLanternItem.setNormalCrystalStack(lantern, crystalContainer.getItem(0), player.registryAccess());
            AbyssalLanternItem.setStrongCrystalStack(lantern, crystalContainer.getItem(1), player.registryAccess());
        }
    }

    private void convertCrystalsIntoFuel() {
        ItemStack lantern = getLanternStack();
        if (lantern.isEmpty()) {
            return;
        }

        int strongFuel = AbyssalLanternItem.getStrongFuel(lantern);
        int normalFuel = AbyssalLanternItem.getFuel(lantern);

        // Prima converti il cristallo forte
        ItemStack strongCrystal = crystalContainer.getItem(1);
        while (!strongCrystal.isEmpty() && strongFuel < AbyssalLanternItem.MAX_FUEL) {
            strongCrystal.shrink(1);
            strongFuel += AbyssalLanternItem.FUEL_PER_STRONG_CRYSTAL;
            if (strongFuel > AbyssalLanternItem.MAX_FUEL) {
                strongFuel = AbyssalLanternItem.MAX_FUEL;
            }
        }
        crystalContainer.setItem(1, strongCrystal);

        // Poi converti il cristallo normale
        ItemStack normalCrystal = crystalContainer.getItem(0);
        while (!normalCrystal.isEmpty() && normalFuel < AbyssalLanternItem.MAX_FUEL) {
            normalCrystal.shrink(1);
            normalFuel += AbyssalLanternItem.FUEL_PER_CRYSTAL;
            if (normalFuel > AbyssalLanternItem.MAX_FUEL) {
                normalFuel = AbyssalLanternItem.MAX_FUEL;
            }
        }
        crystalContainer.setItem(0, normalCrystal);

        AbyssalLanternItem.setStrongFuel(lantern, strongFuel);
        AbyssalLanternItem.setFuel(lantern, normalFuel);
    }

    private ItemStack getLanternStack() {
        ItemStack stack = mainHand ? player.getMainHandItem() : player.getOffhandItem();
        if (!stack.is(ModItems.ABYSSAL_LANTERN.get())) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    private static final class CrystalSlot extends Slot {
        private final boolean isNormalCrystalSlot;

        public CrystalSlot(Container container, int slot, int x, int y, boolean isNormalCrystalSlot) {
            super(container, slot, x, y);
            this.isNormalCrystalSlot = isNormalCrystalSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (isNormalCrystalSlot) {
                return stack.is(ModItems.VOIDSTONE_CRYSTAL.get());
            } else {
                return stack.is(ModItems.STRONG_VOIDSTONE_CRYSTAL.get());
            }
        }
    }

    public static final class Provider implements net.minecraft.world.MenuProvider {
        private final InteractionHand hand;

        public Provider(InteractionHand hand) {
            this.hand = hand;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.abyssal_mines.abyssal_lantern");
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            boolean mainHand = hand == InteractionHand.MAIN_HAND;
            return new AbyssalLanternMenu(containerId, playerInventory, mainHand);
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
            buffer.writeByte(hand == InteractionHand.MAIN_HAND ? 0 : 1);
        }
    }
}
