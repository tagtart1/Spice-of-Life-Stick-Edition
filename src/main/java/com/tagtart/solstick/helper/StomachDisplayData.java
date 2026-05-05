package com.tagtart.solstick.helper;

import com.tagtart.solstick.Config;
import com.tagtart.solstick.PlayerStomach;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class StomachDisplayData {
    private StomachDisplayData() {
    }

    public static List<QueueEntry> buildQueueEntries(PlayerStomach stomach) {
        List<ResourceLocation> queue = stomach.getFoodQueueAsList();
        List<QueueEntry> entries = new ArrayList<>(queue.size());
        boolean queueIsFull = queue.size() >= Config.STOMACH_QUEUE_SIZE.get();
        for (int queueIndex = queue.size() - 1, lineNumber = 1; queueIndex >= 0; queueIndex--, lineNumber++) {
            ResourceLocation foodId = queue.get(queueIndex);
            boolean isNextToExit = queueIsFull && queueIndex == 0;
            entries.add(new QueueEntry(
                    lineNumber,
                    foodId,
                    resolveFoodDisplayName(foodId),
                    resolveFoodStack(foodId),
                    isNextToExit));
        }
        return entries;
    }

    public static List<EffectivenessEntry> buildEffectivenessEntries(PlayerStomach stomach) {
        Map<ResourceLocation, Integer> foodMap = stomach.getFoodMap();
        List<ResourceLocation> sortedFoods = new ArrayList<>(foodMap.keySet());
        sortedFoods.sort(Comparator.comparing(ResourceLocation::toString));

        List<EffectivenessEntry> entries = new ArrayList<>(sortedFoods.size());
        for (ResourceLocation foodId : sortedFoods) {
            entries.add(new EffectivenessEntry(
                    foodId,
                    resolveFoodDisplayName(foodId),
                    Math.round(stomach.getFoodEffectiveness(foodId) * 100.0F)));
        }
        return entries;
    }

    public static Component resolveFoodDisplayName(ResourceLocation foodId) {
        Item item = BuiltInRegistries.ITEM.get(foodId);
        if (item == null || item == BuiltInRegistries.ITEM.byId(0)) {
            return Component.literal(foodId.toString());
        }
        return item.getDescription();
    }

    public static ItemStack resolveFoodStack(ResourceLocation foodId) {
        Item item = BuiltInRegistries.ITEM.get(foodId);
        if (item == null || item == BuiltInRegistries.ITEM.byId(0)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    public record QueueEntry(
            int lineNumber,
            ResourceLocation foodId,
            Component displayName,
            ItemStack stack,
            boolean nextToExit) {
    }

    public record EffectivenessEntry(
            ResourceLocation foodId,
            Component displayName,
            int percent) {
    }
}
