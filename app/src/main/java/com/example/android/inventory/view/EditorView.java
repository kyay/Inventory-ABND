package com.example.android.inventory.view;

import android.database.Cursor;
import android.net.Uri;

import com.example.android.inventory.presenter.EditorPresenter;

import java.io.File;

import androidx.annotation.StringRes;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * The interface of a view that the {@link EditorPresenter} uses. This is mainly implemented by
 * the {@link com.example.android.inventory.view.EditorActivity}
 */
public interface EditorView {
    Loader<Cursor> createLoader(Uri uri, String[] projection);

    void updateUi(String name, int quantity, String supplierName,
                  String supplierNumber, double price, String description,
                  @StringRes int pictureButtonStringResource, File pictureFile);

    File getFilesDirectory();

    LocalBroadcastManager getLocalBroadcastManager();

    void showToast(@StringRes int msgResId, int duration);

    boolean isEditing();

    void setQuantity(int quantity);
}
