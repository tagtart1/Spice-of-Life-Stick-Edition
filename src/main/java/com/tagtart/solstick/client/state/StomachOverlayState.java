package com.tagtart.solstick.client.state;

public final class StomachOverlayState {
    private static boolean visible;

    private StomachOverlayState() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean shouldBeVisible) {
        visible = shouldBeVisible;
    }
}
