package com.example.android.inventory.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.example.android.inventory.R;
import com.example.android.inventory.model.InventoryContract.ItemsEntry;
import com.example.android.inventory.other.App;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.utilities.PushIdGenerator;

import java.util.Calendar;
import java.util.Iterator;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A {@link ContentProvider} that serves as an abstraction layer between the program that wants
 * the Inventory data and the Inventory database and the Firebase Realtime database
 */
@SuppressWarnings({"ConstantConditions", "TryFinallyCanBeTryWithResources"})
public class InventoryProvider extends ContentProvider {
    private static final int ITEMS = 100;
    private static final int ITEM_ID = 101;
    private static final int SEQUENCE = 200;
    private static final String KEY_USER_ID = "KEY_USER_ID";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_SEQUENCE, SEQUENCE);
    }

    private SQLiteOpenHelper mDbhelper;
    private FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private final ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            mFirebaseDatabaseHelper.setEnabled(false);
            Cursor itemCursor = null;
            try {
                // Check if the item is saved locally
                itemCursor = query(ItemsEntry.CONTENT_URI, new String[]{ItemsEntry._ID},
                        ItemsEntry.COLUMN_ITEM_DATE_CREATED + "=?",
                        new String[]{dataSnapshot.getKey()}, null);
                if (itemCursor.getCount() <= 0) {
                    // It is not, so insert it
                    ContentValues values = getContentValuesFromDataSnapshot(dataSnapshot);
                    values.put(ItemsEntry.COLUMN_ITEM_DATE_CREATED, dataSnapshot.getKey());
                    if (values.containsKey(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE)) {
                        ImageFileUtils.saveBytes(getContext(),
                                Base64.decode(values.getAsString(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE),
                                        Base64.DEFAULT), dataSnapshot.getKey());
                        // Remove the picture column to not cause exceptions being thrown
                        values.putNull(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
                        values.keySet().remove(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
                    }
                    insert(ItemsEntry.CONTENT_URI, values);
                }
            } finally {
                mFirebaseDatabaseHelper.setEnabled(true);
                if (itemCursor != null) itemCursor.close();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            mFirebaseDatabaseHelper.setEnabled(false);
            try {
                ContentValues values = getContentValuesFromDataSnapshot(dataSnapshot);
                if (values.containsKey(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE)) {
                    ImageFileUtils.saveBytes(getContext(),
                            Base64.decode(values.getAsString(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE),
                                    Base64.DEFAULT), dataSnapshot.getKey());
                    // Remove the picture column to not cause exceptions being thrown
                    values.putNull(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
                    values.keySet().remove(ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE);
                }
                update(ItemsEntry.CONTENT_URI, values,
                        ItemsEntry.COLUMN_ITEM_DATE_CREATED + "=?",
                        new String[]{dataSnapshot.getKey()});
            } finally {
                mFirebaseDatabaseHelper.setEnabled(true);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            mFirebaseDatabaseHelper.setEnabled(false);
            Cursor itemCursor = null;
            try {
                itemCursor = query(ItemsEntry.CONTENT_URI, new String[]{ItemsEntry._ID},
                        ItemsEntry.COLUMN_ITEM_DATE_CREATED + "=?",
                        new String[]{dataSnapshot.getKey()}, null);
                // Check if the item is there
                if (itemCursor.getCount() > 0) {
                    // It is, so delete it
                    delete(ItemsEntry.CONTENT_URI,
                            ItemsEntry.COLUMN_ITEM_DATE_CREATED + "=?",
                            new String[]{dataSnapshot.getKey()});
                }
            } finally {
                mFirebaseDatabaseHelper.setEnabled(true);
                if (itemCursor != null) itemCursor.close();
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // Do nothing
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Do nothing
        }

        /**
         * Converts a {@link DataSnapshot} to a {@link ContentValues} Object.
         * @param snapshot the data snapshot to convert
         * @return the content values from the data snapshot
         */
        private ContentValues getContentValuesFromDataSnapshot(DataSnapshot snapshot) {
            Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
            ContentValues values = new ContentValues();
            while (iterator.hasNext()) {
                DataSnapshot currentNode = iterator.next();
                Object value = currentNode.getValue();
                if (value instanceof Integer) {
                    values.put(currentNode.getKey(), (int) currentNode.getValue());
                } else if (value instanceof Float) {
                    values.put(currentNode.getKey(), (float) currentNode.getValue());
                } else if (value instanceof Double) {
                    values.put(currentNode.getKey(), (double) currentNode.getValue());
                } else if (value instanceof Short) {
                    values.put(currentNode.getKey(), (short) currentNode.getValue());
                } else if (value instanceof Long) {
                    values.put(currentNode.getKey(), (long) currentNode.getValue());
                } else if (value instanceof String) {
                    values.put(currentNode.getKey(), (String) currentNode.getValue());
                } else if (value instanceof Boolean) {
                    values.put(currentNode.getKey(), (boolean) currentNode.getValue());
                } else if (value instanceof Byte) {
                    values.put(currentNode.getKey(), (byte) currentNode.getValue());
                } else if (value instanceof byte[]) {
                    values.put(currentNode.getKey(), (byte[]) currentNode.getValue());
                }
            }
            return values;
        }
    };

    @Inject
    public void setSQLDbhelper(SQLiteOpenHelper SQLDbHelper) {
        mDbhelper = SQLDbHelper;
    }

    @Inject
    public void setFirebaseDatabaseHelper(FirebaseDatabaseHelper firebaseDatabaseHelper) {
        mFirebaseDatabaseHelper = firebaseDatabaseHelper;
    }

    @Override
    public boolean onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        ((App) getContext()).getAppComponent().inject(this);
        Log.e("test", " " + (mFirebaseDatabaseHelper == null));
        mFirebaseDatabaseHelper.addChildEventListener("", mChildEventListener);
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getContext());
            String userId = firebaseAuth.getUid();
            // Check if the user id changed
            if (!TextUtils.isEmpty(userId) &&
                    !sharedPrefs.getString(KEY_USER_ID, "").equals(userId)) {
                // It did, so we need to delete all items in the local database and then
                // add all the items from the new user
                sharedPrefs.edit().putString(KEY_USER_ID, userId).apply();
                mFirebaseDatabaseHelper.setEnabled(false);
                delete(ItemsEntry.CONTENT_URI, "1", null);
                mFirebaseDatabaseHelper.setEnabled(true);
                mFirebaseDatabaseHelper.addListenerForSingleValueEvent("", new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            mChildEventListener.onChildAdded(snapshot, null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbhelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case ITEMS:
                Cursor cursor = db.query(ItemsEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case SEQUENCE:
                return db.query(InventoryContract.PATH_SEQUENCE, projection,
                        selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_query) + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemsEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.unknown_type) +
                        uri +
                        getContext().getString(R.string.with_match) +
                        match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbhelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ITEMS:
                getContext().getContentResolver().notifyChange(uri, null);
                // If a date was passed in, use it, else, create a new one using the PushIdGenerator
                // of the Firebase Database
                String dateCreated = values.containsKey(ItemsEntry.COLUMN_ITEM_DATE_CREATED) ?
                        values.getAsString(ItemsEntry.COLUMN_ITEM_DATE_CREATED) :
                        PushIdGenerator.generatePushChildName(Calendar.getInstance().getTimeInMillis());
                values.putNull(ItemsEntry.COLUMN_ITEM_DATE_CREATED);
                mFirebaseDatabaseHelper.insert(
                        dateCreated,
                        new ContentValues(values));
                values.put(ItemsEntry.COLUMN_ITEM_DATE_CREATED, dateCreated);
                return ContentUris.withAppendedId(
                        InventoryContract.BASE_CONTENT_URI,
                        db.insert(ItemsEntry.TABLE_NAME, null, values));
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_insert) + uri);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbhelper.getWritableDatabase();
        String id;
        String fileName = selectionArgs == null ? "" : selectionArgs[0];
        String itemKey = "";
        switch (sUriMatcher.match(uri)) {
            case ITEM_ID:
                id = String.valueOf(ContentUris.parseId(uri));
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{id};
                String path = uri.toString();
                if (path.substring(path.length() - 1).equals("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                path = path.substring(0, path.lastIndexOf("/"));
                uri = Uri.parse(path);
                Cursor itemCursor = query(
                        ContentUris.withAppendedId(ItemsEntry.CONTENT_URI,
                                Long.parseLong(selectionArgs[0])),
                        new String[]{
                                ItemsEntry.COLUMN_ITEM_DATE_CREATED},
                        null, null, null);
                itemCursor.moveToFirst();
                fileName = itemCursor.getString(itemCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED));
                itemKey = itemCursor.getString(
                        itemCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED));
                itemCursor.close();
                itemCursor.moveToFirst();
            case ITEMS:
                if (TextUtils.isEmpty(selection)) {
                    selection = "1";
                }
                if (selection.equals("1")) {
                    getContext().getFilesDir().delete();
                } else {
                    if (!TextUtils.isEmpty(fileName)) getContext().deleteFile(fileName);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                int result = db.delete(ItemsEntry.TABLE_NAME, selection, selectionArgs);
                mFirebaseDatabaseHelper.delete(itemKey);
                return result;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_delete) + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        assert values != null;
        if (values.size() == 0) {
            return 0;
        }
        String dateCreated = selectionArgs == null ? "" : selectionArgs[0];
        SQLiteDatabase db = mDbhelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case ITEM_ID:
                selection = ItemsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                String path = uri.toString();
                if (path.substring(path.length() - 1).equals("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                path = path.substring(0, path.lastIndexOf("/"));
                uri = Uri.parse(path);
                Cursor itemCursor =
                        query(
                                ContentUris.withAppendedId(ItemsEntry.CONTENT_URI,
                                        Long.parseLong(selectionArgs[0])),
                                new String[]{ItemsEntry.COLUMN_ITEM_DATE_CREATED},
                                null, null, null);
                itemCursor.moveToFirst();
                dateCreated = itemCursor.getString(itemCursor.getColumnIndex(ItemsEntry.COLUMN_ITEM_DATE_CREATED));
                itemCursor.close();
            case ITEMS:
                getContext().getContentResolver().notifyChange(uri, null);
                mFirebaseDatabaseHelper.update(dateCreated, values);
                return db.update(ItemsEntry.TABLE_NAME, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_update) + uri);
        }
    }
}
