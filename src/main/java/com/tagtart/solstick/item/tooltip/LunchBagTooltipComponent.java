package com.tagtart.solstick.item.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemContainerContents;

public record LunchBagTooltipComponent(ItemContainerContents contents, int selectedSlot) implements TooltipComponent {
}
