package com.tagtart.solstick.client.state;

public final class LunchBagOverlayState {
    private static boolean visible;
    private static int lockedHotbarSlot = -1;

    private LunchBagOverlayState() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean shouldBeVisible) {
        if (shouldBeVisible) {
            show();
        } else {
            hide();
        }
    }

    public static void show() {
        visible = true;
    }

    public static void hide() {
        visible = false;
        clearLockedHotbarSlot();
    }

    public static int getLockedHotbarSlot() {
        return lockedHotbarSlot;
    }

    public static void setLockedHotbarSlot(int slot) {
        lockedHotbarSlot = slot;
    }

    public static void clearLockedHotbarSlot() {
        lockedHotbarSlot = -1;
    }
}
