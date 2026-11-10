package com.flxholle.quicktiles.utils;

/**
 * Wraps around a list index, e.g. the index of the next value of a selection tile.
 * <p>
 * Kept as a plain function so it can be unit tested without an Android runtime.
 */
public final class ValueCycle {

    private ValueCycle() {
    }

    /**
     * @param currentIndex index of the current value, -1 if the current value is not in the list
     * @param size         number of values
     * @return the index of the next value, 0 when the current one was the last (or unknown)
     */
    public static int next(int currentIndex, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("A tile needs at least one value, got " + size);
        }
        return Math.floorMod(currentIndex + 1, size);
    }
}
