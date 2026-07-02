// src/main/java/com/tuonome/abyssalmines/screen/AbyssalLanternScreen.java

package com.tuonome.abyssalmines.screen;

import com.tuonome.abyssalmines.AbyssalMines;
import com.tuonome.abyssalmines.menu.AbyssalLanternMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AbyssalLanternScreen extends AbstractContainerScreen<AbyssalLanternMenu> {
    // Texture personalizzata per la lanterna abissale
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AbyssalMines.MODID, "textures/gui/abyssal_lantern.png");

    public AbyssalLanternScreen(AbyssalLanternMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
