package com.example.android.inventory.presenter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.example.android.inventory.view.EditorView;

import androidx.annotation.StringRes;
import androidx.loader.app.LoaderManager;

/**
 * The interface for a presenter for the {@link com.example.android.inventory.view.EditorActivity}.
 */
public interface EditorPresenter extends LoaderManager.LoaderCallbacks<Cursor> {
    void bindView(EditorView mainView);

    void unBindView();

    void setQuantity(int newQuantity);

    /**
     * Returns a service {@link Intent} that will tell the {@link InsertUpdateService} what to do exactly.
     *
     * @return an Intent.
     */
    Intent getServiceIntent(String name, long quantity, String supplierName,
                            String supplierNumber, double price, String description, Drawable picture);

    /**
     * Sets the original {@link Bitmap} that this item had
     *
     * @param bitmap the original bitmap
     */
    void setOriginalBitmap(Bitmap bitmap);

    /**
     * Sets the Uri that the image should be retrieved from.
     *
     * @param uri the uri of the image
     */
    void setImageUri(Uri uri);

    long getId();

    void setId(long id);

    @StringRes
    int getTitle(boolean isEditing);
}
