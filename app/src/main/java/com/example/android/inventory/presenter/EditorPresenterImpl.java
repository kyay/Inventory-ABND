package com.example.android.inventory.presenter;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.android.inventory.R;
import com.example.android.inventory.model.InventoryContract.ItemsEntry;
import com.example.android.inventory.view.EditorView;
import com.google.firebase.database.core.utilities.PushIdGenerator;

import java.io.File;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

/**
 * The main implementation of the {@link EditorPresenter} interface
 */
public class EditorPresenterImpl implements EditorPresenter {
    private static final int ID_NOT_INITIALIZED = -1;
    private final ContentResolver mModel;
    @Nullable
    private EditorView mEditorView;
    private boolean mLoaded = false;
    private String mDateCreated;
    private long mId = ID_NOT_INITIALIZED;
    private Bitmap mOriginalBitmap;
    private Uri mImageUri;

    public EditorPresenterImpl(ContentResolver model) {
        mModel = model;
    }

    @Override
    public void bindView(@NonNull EditorView editorView) {
        mEditorView = editorView;
        mEditorView.getLocalBroadcastManager().registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extras = intent.getExtras();
                        int toastMsgId = R.string.msg_item_updated;
                        if (extras != null && extras.containsKey(InsertUpdateService.EXTENDED_DATA_INSERTED_ID)) {
                            mId = extras.getLong(InsertUpdateService.EXTENDED_DATA_INSERTED_ID, 0);
                            toastMsgId = R.string.msg_item_inserted;
                        }
                        if (mEditorView != null)
                            mEditorView.showToast(toastMsgId, Toast.LENGTH_LONG);
                    }
                },
                new IntentFilter(InsertUpdateService.BROADCAST_ADD_DONE));
    }

    @Override
    public void unBindView() {
        mEditorView = null;
    }


    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mEditorView.createLoader(
                ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, mId),
                ItemsEntry.PROJECTION_ALL);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (!mLoaded && mEditorView != null) {
            mLoaded = true;
            cursor.moveToFirst();
            String imagePath = cursor.getString(
                    cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED));
            mEditorView.updateUi(
                    cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_NAME)),
                    cursor.getInt(cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_QUANTITY)),
                    cursor.getString(
                            cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME)),
                    cursor.getString(
                            cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_SUPPLIER_NUMBER)),
                    cursor.getDouble(cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_PRICE)),
                    cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DESCRIPTION)),
                    TextUtils.isEmpty(imagePath) ?
                            R.string.item_picture_add :
                            R.string.item_picture_edit,
                    new File(mEditorView.getFilesDirectory(), imagePath));
            mDateCreated = cursor.getString(cursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void setQuantity(int newQuantity) {
        if (newQuantity >= 0) {
            if (mEditorView != null) {
                if (mId != ID_NOT_INITIALIZED && !mEditorView.isEditing()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ItemsEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                    mModel.update(
                            ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, mId), contentValues,
                            null, null);
                }
                mEditorView.setQuantity(newQuantity);
            }
        }
    }

    @Override
    public Intent getServiceIntent(String name, long quantity, String supplierName,
                                   String supplierNumber, double price,
                                   String description, Drawable picture) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemsEntry.COLUMN_ITEM_NAME, name);
        contentValues.put(ItemsEntry.COLUMN_ITEM_QUANTITY, quantity);
        contentValues.put(ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME, supplierName);
        contentValues.put(ItemsEntry.COLUMN_ITEM_SUPPLIER_NUMBER, supplierNumber);
        contentValues.put(ItemsEntry.COLUMN_ITEM_PRICE, price);
        contentValues.put(ItemsEntry.COLUMN_ITEM_DESCRIPTION, description);
        Intent serviceIntent = new Intent();
        if (picture instanceof BitmapDrawable) {
            Bitmap imageBitmap = ((BitmapDrawable) picture).getBitmap();
            if (!imageBitmap.sameAs(mOriginalBitmap)) {
                if (TextUtils.isEmpty(mDateCreated))
                    mDateCreated = PushIdGenerator.generatePushChildName(Calendar.getInstance().getTimeInMillis());
                contentValues.put(ItemsEntry.COLUMN_ITEM_DATE_CREATED, mDateCreated);
                contentValues.put(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE, true);
                serviceIntent.putExtra(InsertUpdateService.EXTRA_IMAGE_URI, mImageUri);
            }
        }
        serviceIntent.putExtra(InsertUpdateService.EXTRA_CONTENT_VALUES, contentValues);
        if (mId == ID_NOT_INITIALIZED) {
            serviceIntent.putExtra(InsertUpdateService.EXTRA_INSERT_OR_UPDATE, InsertUpdateService.INSERT);
            serviceIntent.putExtra(InsertUpdateService.EXTRA_ITEM_URI, ItemsEntry.CONTENT_URI);
        } else {
            serviceIntent.putExtra(InsertUpdateService.EXTRA_INSERT_OR_UPDATE, InsertUpdateService.UPDATE);
            serviceIntent.putExtra(InsertUpdateService.EXTRA_ITEM_URI, ContentUris.withAppendedId(ItemsEntry.CONTENT_URI, mId));
        }
        return serviceIntent;
    }

    @Override
    public void setOriginalBitmap(Bitmap bitmap) {
        mOriginalBitmap = bitmap;
    }

    @Override
    public void setImageUri(Uri uri) {
        mImageUri = uri;
    }

    @Override
    public int getTitle(boolean isEditing) {
        return mId == 0 ? R.string.label_editor_add :
                isEditing ? R.string.label_editor_edit : R.string.label_editor_details;
    }

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public void setId(long id) {
        mId = id;
        mLoaded = false;
    }
}
