package com.tagtart.solstick.client.overlay;

import com.tagtart.solstick.item.custom.LunchBagItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

public final class LunchBagFoodPreviewRenderer {
    private static final ResourceLocation FOOD_EMPTY_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation FOOD_FULL_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation FOOD_EMPTY_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    private static final ResourceLocation FOOD_HALF_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    private static final ResourceLocation FOOD_FULL_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");
    private static final ResourceLocation APPLESKIN_HUNGER_OUTLINE_SPRITE = ResourceLocation.fromNamespaceAndPath(
            "appleskin",
            "tooltip_hunger_outline");
    private static final ResourceLocation APPLESKIN_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "appleskin",
            "textures/icons.png");
    private static final int HUNGER_ICON_SIZE = 9;
    private static final int SATURATION_ICON_SIZE = 7;
    private static final int HUNGER_ICON_SPACING = 9;
    private static final int SATURATION_ICON_SPACING = 7;
    private static final int ROW_GAP = 2;
    private static final int APPLESKIN_TEXTURE_SIZE = 256;
    private static final int SATURATION_ICON_V = 27;

    private LunchBagFoodPreviewRenderer() {
    }

    public static int getHeight(LunchBagItem.FoodPreview preview) {
        if (preview == null || !shouldRender()) {
            return 0;
        }
        return HUNGER_ICON_SIZE + ROW_GAP + SATURATION_ICON_SIZE;
    }

    public static void render(GuiGraphics guiGraphics, LunchBagItem.FoodPreview preview, int left, int top,
            int availableWidth) {
        if (preview == null || !shouldRender()) {
            return;
        }

        int hungerWidth = getRowWidth(
                preview.defaultFoodProperties().nutrition(),
                preview.modifiedFoodProperties().nutrition(),
                HUNGER_ICON_SPACING);
        int saturationWidth = getRowWidth(
                preview.defaultFoodProperties().saturation(),
                preview.modifiedFoodProperties().saturation(),
                SATURATION_ICON_SPACING);
        int contentWidth = Math.max(hungerWidth, saturationWidth);
        int contentLeft = left + (availableWidth - contentWidth) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderHungerRow(guiGraphics, preview, contentLeft + (contentWidth - hungerWidth) / 2, top);
        renderSaturationRow(guiGraphics, preview, contentLeft + (contentWidth - saturationWidth) / 2,
                top + HUNGER_ICON_SIZE + ROW_GAP);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void renderHungerRow(GuiGraphics guiGraphics, LunchBagItem.FoodPreview preview, int x, int y) {
        int defaultHunger = preview.defaultFoodProperties().nutrition();
        int modifiedHunger = preview.modifiedFoodProperties().nutrition();
        boolean rotten = preview.isRotten();
        renderLayeredRow(
                guiGraphics,
                x,
                y,
                getBarCount(defaultHunger, modifiedHunger),
                HUNGER_ICON_SPACING,
                (graphics, halfStep, pipX, pipY) -> {
                    FoodIcon defaultIcon = getHungerIcon(defaultHunger, halfStep);
                    FoodIcon modifiedIcon = getHungerIcon(modifiedHunger, halfStep);
                    renderHungerBackground(graphics, rotten, defaultIcon, modifiedIcon, pipX, pipY);
                    renderHungerLayer(graphics, rotten, defaultHunger, halfStep, pipX, pipY, 0.25F);
                    renderHungerLayer(graphics, rotten, modifiedHunger, halfStep, pipX, pipY, 1.0F);
                });
    }

    private static void renderHungerBackground(GuiGraphics guiGraphics, boolean rotten, FoodIcon defaultIcon,
            FoodIcon modifiedIcon, int x, int y) {
        guiGraphics.blitSprite(getFoodTexture(rotten, FoodIcon.EMPTY), x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);

        boolean missingPip = defaultIcon != FoodIcon.EMPTY && modifiedIcon == FoodIcon.EMPTY;
        if (!missingPip) {
            return;
        }

        guiGraphics.setColor(0.62F, 0.0F, 0.0F, 0.5F);
        guiGraphics.blitSprite(APPLESKIN_HUNGER_OUTLINE_SPRITE, x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderHungerLayer(GuiGraphics guiGraphics, boolean rotten, int hungerValue, int halfStep, int x, int y,
            float alpha) {
        FoodIcon icon = getHungerIcon(hungerValue, halfStep);
        if (icon == FoodIcon.EMPTY) {
            return;
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blitSprite(getFoodTexture(rotten, icon), x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderSaturationRow(GuiGraphics guiGraphics, LunchBagItem.FoodPreview preview, int x, int y) {
        float defaultSaturation = preview.defaultFoodProperties().saturation();
        float modifiedSaturation = preview.modifiedFoodProperties().saturation();
        renderLayeredRow(
                guiGraphics,
                x,
                y,
                getBarCount(defaultSaturation, modifiedSaturation),
                SATURATION_ICON_SPACING,
                (graphics, halfStep, pipX, pipY) -> {
                    renderSaturationLayer(graphics, defaultSaturation, halfStep, pipX, pipY, 0.25F);
                    renderSaturationLayer(graphics, modifiedSaturation, halfStep, pipX, pipY, 1.0F);
                });
    }

    private static void renderSaturationLayer(GuiGraphics guiGraphics, float saturationValue, int halfStep, int x,
            int y, float alpha) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(
                APPLESKIN_ICONS_TEXTURE,
                x,
                y,
                getSaturationU((saturationValue - halfStep) / 2.0F),
                SATURATION_ICON_V,
                SATURATION_ICON_SIZE,
                SATURATION_ICON_SIZE,
                APPLESKIN_TEXTURE_SIZE,
                APPLESKIN_TEXTURE_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static int getRowWidth(float defaultValue, float modifiedValue, int spacing) {
        return getBarCount(defaultValue, modifiedValue) * spacing;
    }

    private static int getBarCount(float defaultValue, float modifiedValue) {
        return Math.max(1, (int) Math.ceil(Math.max(defaultValue, modifiedValue) / 2.0F));
    }

    private static void renderLayeredRow(GuiGraphics guiGraphics, int x, int y, int bars, int spacing,
            PipRenderer pipRenderer) {
        int offsetX = x + (bars - 1) * spacing;
        for (int halfStep = 0; halfStep < bars * 2; halfStep += 2) {
            pipRenderer.render(guiGraphics, halfStep, offsetX, y);
            offsetX -= spacing;
        }
    }

    private static ResourceLocation getFoodTexture(boolean rotten, FoodIcon icon) {
        return switch (icon) {
            case EMPTY -> rotten ? FOOD_EMPTY_HUNGER_TEXTURE : FOOD_EMPTY_TEXTURE;
            case HALF -> rotten ? FOOD_HALF_HUNGER_TEXTURE : FOOD_HALF_TEXTURE;
            case FULL -> rotten ? FOOD_FULL_HUNGER_TEXTURE : FOOD_FULL_TEXTURE;
        };
    }

    private static FoodIcon getHungerIcon(int hungerValue, int halfStep) {
        if (hungerValue <= halfStep) {
            return FoodIcon.EMPTY;
        }
        if (hungerValue - 1 == halfStep) {
            return FoodIcon.HALF;
        }
        return FoodIcon.FULL;
    }

    private static int getSaturationU(float effectiveSaturation) {
        if (effectiveSaturation >= 1.0F) {
            return 21;
        }
        if (effectiveSaturation > 0.5F) {
            return 14;
        }
        if (effectiveSaturation > 0.25F) {
            return 7;
        }
        if (effectiveSaturation > 0.0F) {
            return 0;
        }
        return 28;
    }

    private static boolean shouldRender() {
        return ModList.get().isLoaded("appleskin");
    }

    @FunctionalInterface
    private interface PipRenderer {
        void render(GuiGraphics guiGraphics, int halfStep, int x, int y);
    }

    private enum FoodIcon {
        EMPTY,
        HALF,
        FULL
    }
}
