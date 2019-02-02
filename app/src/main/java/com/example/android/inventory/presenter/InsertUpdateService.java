package com.example.android.inventory.presenter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.example.android.inventory.model.ImageFileUtils;
import com.example.android.inventory.model.InventoryContract;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * A service that inserts or updates into the database, it is used because saving images can
 * take a lot of time and cause the app to be unresponsive.
 */
public class InsertUpdateService extends JobIntentService {
    public static final String EXTRA_INSERT_OR_UPDATE = "EXTRA_INSERT_OR_UPDATE";
    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";
    public static final String EXTRA_CONTENT_VALUES = "EXTRA_CONTENT_VALUES";
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    public static final String EXTRA_ITEM_URI = "EXTRA_ITEM_URI";

    public static final String BROADCAST_ADD_DONE =
            "com.example.android.inventory.BROADCAST_ADD_DONE";
    public static final String EXTENDED_DATA_INSERTED_ID = "INSERTED_ID";

    private boolean mDone = false;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // We are free to do whatever time-consuming work here, because this runs on a background
        // thread
        Bundle intentExtras = intent.getExtras();
        ContentValues values = Objects.requireNonNull(intentExtras).getParcelable(EXTRA_CONTENT_VALUES);
        Uri itemUri = intentExtras.getParcelable(EXTRA_ITEM_URI);
        if (intentExtras.containsKey(EXTRA_IMAGE_URI)) {
            try {
                ImageFileUtils.saveBitmap(this,
                        MediaStore.Images.Media.getBitmap(getContentResolver(),
                                intentExtras.getParcelable(EXTRA_IMAGE_URI)),
                        Objects.requireNonNull(values).getAsString(InventoryContract.ItemsEntry.COLUMN_ITEM_DATE_CREATED));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent localIntent = new Intent(BROADCAST_ADD_DONE);
        // remove the picture column to prevent exceptions.
        Objects.requireNonNull(values).putNull(InventoryContract.ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
        values.keySet().remove(InventoryContract.ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
        if (UPDATE.equals(intentExtras.getString(EXTRA_INSERT_OR_UPDATE))) {
            getContentResolver().update(Objects.requireNonNull(itemUri), values, null, null);
        } else {
            localIntent
                    .putExtra(EXTENDED_DATA_INSERTED_ID,
                            ContentUris.parseId(getContentResolver().insert(Objects.requireNonNull(itemUri), values)));
        }
        mDone = true;

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public boolean onStopCurrentWork() {
        return !mDone;
    }
}
