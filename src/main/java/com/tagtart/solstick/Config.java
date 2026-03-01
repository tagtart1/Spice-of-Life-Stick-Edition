package com.tagtart.solstick;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final int STOMACH_QUEUE_SIZE_MIN = 3;
    public static final int STOMACH_QUEUE_SIZE_MAX = 24;
    public static final int STOMACH_QUEUE_SIZE_DEFAULT = 12;

    public static final int FOOD_DECAY_PERCENT_MIN = 1;
    public static final int FOOD_DECAY_PERCENT_MAX = 100;
    public static final int FOOD_DECAY_PERCENT_DEFAULT = 6;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue STOMACH_QUEUE_SIZE = BUILDER
            .comment(
                    "How many recent foods are kept in stomach history.",
                    "Range: 3-24.",
                    "When the queue is full, the oldest food is removed first (FIFO).")
            .translation("solstick.configuration.stomachQueueSize")
            .defineInRange(
                    "stomachQueueSize",
                    STOMACH_QUEUE_SIZE_DEFAULT,
                    STOMACH_QUEUE_SIZE_MIN,
                    STOMACH_QUEUE_SIZE_MAX);

    public static final ModConfigSpec.IntValue FOOD_DECAY_PERCENT = BUILDER
            .comment(
                    "How much food effectiveness is reduced each repeated eat of the same food.",
                    "Range: 1-100 (%).",
                    "This is rolling/multiplicative decay:",
                    "new effectiveness = current effectiveness * (1 - decay%).",
                    "Example with 6%: 100% -> 94% -> 88.36% -> 83.06%.")
            .translation("solstick.configuration.foodDecayPercent")
            .defineInRange(
                    "foodDecayPercent",
                    FOOD_DECAY_PERCENT_DEFAULT,
                    FOOD_DECAY_PERCENT_MIN,
                    FOOD_DECAY_PERCENT_MAX);

    static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
