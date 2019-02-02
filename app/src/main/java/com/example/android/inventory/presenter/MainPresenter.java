package com.example.android.inventory.presenter;

import android.database.Cursor;

import com.example.android.inventory.view.MainView;

import androidx.loader.app.LoaderManager;

/**
 * The interface for a presenter for the {@link com.example.android.inventory.view.MainActivity}.
 */
public interface MainPresenter extends LoaderManager.LoaderCallbacks<Cursor> {
    void bindView(MainView mainView);

    void unBindView();

    boolean isSignedIn();

    /**
     * Hides an item, but doesn't delete it
     *
     * @param position the position of the item to hide
     * @param id       the id of the item to hide
     */
    void hideItem(int position, long id);

    /**
     * Deletes an item
     *
     * @param id the id of the item to delete
     */
    void deleteItem(long id);

    /**
     * Re-shows an item after it was hidden with {@link MainPresenter#hideItem(int, long)}
     *
     * @param position the position of the item to show
     * @see MainPresenter#hideItem
     */
    void showItem(int position);

    void setOnItemClickListener(ItemsCursorAdapter.OnItemClickListener onItemClickListener);

    void clearSearchHistory();

    @SuppressWarnings("unused")
    void saveSearchQuery(String query);

    void setQuery(String query);

    int getItemCount();
}
