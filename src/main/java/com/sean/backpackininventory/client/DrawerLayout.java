package com.sean.backpackininventory.client;

/** Pure geometry shared by rendering, hit targets and regression tests. */
public record DrawerLayout(int columns, int capacity, int pages, int page, int first, int count) {
    public static DrawerLayout calculate(int slots, int gap, int requestedPage) {
        int columns = Math.max(1, Math.min(5, (gap - 35) / 18));
        int capacity = columns * 8;
        int pages = Math.max(1, (slots + capacity - 1) / capacity);
        int page = Math.max(0, Math.min(requestedPage, pages - 1));
        return new DrawerLayout(columns, capacity, pages, page, page * capacity,
                Math.max(0, Math.min(capacity, slots - page * capacity)));
    }
}
