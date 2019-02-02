package com.example.android.inventory.presenter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;

import com.example.android.inventory.model.InventoryContract.ItemsEntry;
import com.example.android.inventory.view.MainView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The main implementation of the {@link MainPresenter} interface
 */
public class MainPresenterImpl implements MainPresenter {
    private final ContentResolver mModel;
    private final ItemsCursorAdapter mCursorAdapter;
    private final SearchRecentSuggestions mSuggestions;
    private String mQuery = "";
    @Nullable
    private MainView mMainView;

    public MainPresenterImpl(ContentResolver model, SearchRecentSuggestions suggestions) {
        mModel = model;
        mSuggestions = suggestions;
        mCursorAdapter = new ItemsCursorAdapter(null, (id, currentQuantity) -> {
            if (currentQuantity > 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ItemsEntry.COLUMN_ITEM_QUANTITY, currentQuantity - 1);
                mModel.update(
                        ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, id), contentValues,
                        null, null);
            }
        }, () -> {
            if (mMainView != null) mMainView.reloadLoader();
        });

        mCursorAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            private void checkEmpty() {
                if (mMainView != null) mMainView.setEmpty(mCursorAdapter.getItemCount() == 0);
            }
        });

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                String email = user.getEmail();
                if (mMainView != null) {
                    mMainView.setAccountDetails(user.getPhotoUrl(),
                            user.getDisplayName(),
                            TextUtils.isEmpty(email) ? user.getPhoneNumber() : email);
                }
            } else {
                if (mMainView != null) {
                    mMainView.setAccountDetails(null,
                            "",
                            "");
                }
            }
        });
    }

    @Override
    public void bindView(@NonNull MainView mainView) {
        mMainView = mainView;
        mMainView.setAdapter(mCursorAdapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (mMainView != null) {
                mMainView.setAccountDetails(user.getPhotoUrl(),
                        user.getDisplayName(),
                        TextUtils.isEmpty(email) ? user.getPhoneNumber() : email);
            }
        }
    }

    @Override
    public void unBindView() {
        mMainView = null;
    }

    @Override
    public boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }


    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMainView != null) {
            String selection = TextUtils.isEmpty(mQuery) ? null :
                    ItemsEntry.COLUMN_ITEM_NAME + " LIKE ? OR " + ItemsEntry.COLUMN_ITEM_DESCRIPTION + " LIKE ?";
            String[] selectionArgs = TextUtils.isEmpty(mQuery) ? null :
                    new String[]{"%" + mQuery + "%", "%" + mQuery + "%"};
            mMainView.setLoading(true);
            return mMainView.createLoader(ItemsEntry.CONTENT_URI, ItemsEntry.PROJECTION_NO_SUPPLIER,
                    selection, selectionArgs,
                    ItemsEntry.COLUMN_ITEM_DATE_CREATED + " ASC");
        }
        // This should never happen, but to shut Android studio up I returned null.
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (mMainView != null) mMainView.setLoading(false);
        mCursorAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.changeCursor(null);
    }

    @Override
    public void hideItem(int position, long id) {
        mCursorAdapter.setItemInvisible(id);
        mCursorAdapter.notifyItemRemoved(position);
    }

    @Override
    public void showItem(int position) {
        mCursorAdapter.setItemInvisible(-1);
        mCursorAdapter.notifyItemInserted(position);
    }

    @Override
    public void setOnItemClickListener(ItemsCursorAdapter.OnItemClickListener onItemClickListener) {
        mCursorAdapter.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public void clearSearchHistory() {
        mSuggestions.clearHistory();
    }

    @Override
    public void saveSearchQuery(String query) {
        mSuggestions.saveRecentQuery(query, null);
    }

    @Override
    public void setQuery(String query) {
        mQuery = query;
        if (!TextUtils.isEmpty(query)) saveSearchQuery(query);
    }

    @Override
    public void deleteItem(long id) {
        mCursorAdapter.setItemInvisible(-1);
        mModel.delete(
                ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, id),
                null,
                null);
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getItemCount();
    }
}
