package com.example.android.inventory.other;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;

import com.example.android.inventory.R;

import java.lang.reflect.Field;

public class ItemsRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.android.inventory.other.ItemsRecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public ItemsRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    protected void setupSuggestions(String authority, int mode) {
        super.setupSuggestions(authority, mode);
        try {
            //noinspection JavaReflectionMemberAccess
            Field suggestionProjectionField = SearchRecentSuggestionsProvider.class
                    .getDeclaredField("mSuggestionProjection");
            suggestionProjectionField.setAccessible(true);
            String[] suggestionProjection = (String[]) suggestionProjectionField.get(this);
            suggestionProjection[1] = "'"
                    + R.drawable.ic_menu_recent_history + "' AS "
                    + SearchManager.SUGGEST_COLUMN_ICON_1;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}