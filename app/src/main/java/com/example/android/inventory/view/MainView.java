package com.example.android.inventory.view;

import android.database.Cursor;
import android.net.Uri;

import com.example.android.inventory.presenter.MainPresenter;

import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The interface of a view that the {@link MainPresenter} uses. This is mainly implemented by
 * the {@link com.example.android.inventory.view.MainActivity}
 */
public interface MainView {
    void setAccountDetails(Uri pictureUri, String name, String email);

    void setAdapter(RecyclerView.Adapter adapter);

    void setEmpty(boolean empty);

    void setLoading(boolean loading);

    Loader<Cursor> createLoader(Uri uri, String[] projection, String selection,
                                String[] selectionArgs, String sortOrder);

    void reloadLoader();
}
