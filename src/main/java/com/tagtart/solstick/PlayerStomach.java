package com.tagtart.solstick;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayDeque;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.ArrayList;
import io.netty.buffer.ByteBuf;

public class PlayerStomach {
    private final Map<ResourceLocation, Integer> foodMap = new HashMap<>();
    private final ArrayDeque<ResourceLocation> foodQueue = new ArrayDeque<>();
    private static final int MAX_CODEC_ENTRIES = Config.STOMACH_QUEUE_SIZE_MAX;

    public PlayerStomach() {
    }

    public PlayerStomach(Map<ResourceLocation, Integer> foodMap, List<ResourceLocation> foodQueue) {
        this.foodMap.putAll(foodMap);
        this.foodQueue.addAll(foodQueue);

        while (this.foodQueue.size() > Config.STOMACH_QUEUE_SIZE.get()) {
            removeOldestFood();
        }
    }

    public Map<ResourceLocation, Integer> getFoodMap() {
        return new HashMap<>(foodMap);
    }

    public List<ResourceLocation> getFoodQueueAsList() {
        return new ArrayList<>(foodQueue);
    }

    public void addFood(ResourceLocation food) {
        foodQueue.addLast(food);
        foodMap.put(food, foodMap.getOrDefault(food, 0) + 1);

        // Remove oldest food if the stomach is full
        while (foodQueue.size() > Config.STOMACH_QUEUE_SIZE.get()) {
            removeOldestFood();
        }
    }

    private void removeOldestFood() {
        ResourceLocation removedFood = foodQueue.removeFirst();
        foodMap.put(removedFood, foodMap.getOrDefault(removedFood, 0) - 1);
        if (foodMap.get(removedFood) == 0) {
            foodMap.remove(removedFood);
        }

    }

    public float getFoodEffectiveness(ResourceLocation food) {
        float effectiveness = 1.0f;
        if (!foodMap.containsKey(food)) {
            return effectiveness;
        }

        int amountEaten = foodMap.get(food);
        for (int i = 0; i < amountEaten; i++) {
            float decay = Config.FOOD_DECAY_PERCENT.get() / 100.0f;
            effectiveness *= (1.0f - decay);
        }
        return effectiveness;
    }

    public static final Codec<PlayerStomach> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).fieldOf("foodMap")
                    .forGetter(PlayerStomach::getFoodMap),
            ResourceLocation.CODEC.listOf().fieldOf("foodQueue").forGetter(PlayerStomach::getFoodQueueAsList))
            .apply(instance, PlayerStomach::new));

    private static final StreamCodec<ByteBuf, Map<ResourceLocation, Integer>> FOOD_MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.INT, MAX_CODEC_ENTRIES);

    private static final StreamCodec<ByteBuf, List<ResourceLocation>> FOOD_QUEUE_STREAM_CODEC = ByteBufCodecs
            .collection(ArrayList::new, ResourceLocation.STREAM_CODEC, MAX_CODEC_ENTRIES);

    public static final StreamCodec<ByteBuf, PlayerStomach> STREAM_CODEC = StreamCodec.composite(
            FOOD_MAP_STREAM_CODEC, PlayerStomach::getFoodMap,
            FOOD_QUEUE_STREAM_CODEC, PlayerStomach::getFoodQueueAsList,
            PlayerStomach::new);

}
