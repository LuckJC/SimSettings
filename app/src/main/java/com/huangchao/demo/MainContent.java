package com.huangchao.demo;

import java.util.ArrayList;
import java.util.List;

public class MainContent {
    public static final List<MainItem> ITEMS = new ArrayList<>();

    public static void addMainItem(MainItem item) {
        ITEMS.add(item);
    }

    public static MainItem createMainItem(String content) {
        return createMainItem(content, "");
    }

    public static MainItem createMainItem(String content, String details) {
        return new MainItem(content, details);
    }

    public static class MainItem {
        public String content;
        public String details;

        public MainItem(String content, String details) {
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
