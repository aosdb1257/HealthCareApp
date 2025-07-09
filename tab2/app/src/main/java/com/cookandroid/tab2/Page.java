package com.cookandroid.tab2;

public enum Page {
    HOME(0, R.id.nav_home),
    MAP(1, R.id.nav_map),
    SETTINGS(2, R.id.nav_settings);

    private final int position;
    private final int menuItemId;

    Page(int position, int menuItemId) {
        this.position = position;
        this.menuItemId = menuItemId;
    }

    public int getPosition() {
        return position;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public static Page fromPosition(int position) {
        for (Page page : values()) {
            if (page.getPosition() == position) {
                return page;
            }
        }
        return null;
    }

    public static Page fromMenuItemId(int menuItemId) {
        for (Page page : values()) {
            if (page.getMenuItemId() == menuItemId) {
                return page;
            }
        }
        return null;
    }
}