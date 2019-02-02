package com.example.android.inventory.model;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Base64;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;

/**
 * The Base implementation of the {@link FirebaseDatabaseHelper}
 */
@SuppressWarnings("WeakerAccess")
public class BaseFirebaseDatabaseHelper implements FirebaseDatabaseHelper {
    private DatabaseReference mChildReference;

    private boolean mEnabled = true;

    BaseFirebaseDatabaseHelper(String pathToChild) {
        setChildReference(FirebaseDatabase.getInstance().getReference(pathToChild));
    }

    BaseFirebaseDatabaseHelper() {
        setChildReference(FirebaseDatabase.getInstance().getReference());
    }

    @Override
    public void insert(final String childKey, final ContentValues values) {
        if (mEnabled) {
            update(childKey, values);
        }
    }

    public void addValueEventListener(String childKey, ValueEventListener valueEventListener) {
        (TextUtils.isEmpty(childKey) ? mChildReference : mChildReference.child(childKey)).addValueEventListener(valueEventListener);
    }

    @Override
    public void addListenerForSingleValueEvent(String childKey, ValueEventListener valueEventListener) {
        (TextUtils.isEmpty(childKey) ? mChildReference : mChildReference.child(childKey)).addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void addChildEventListener(String childKey, ChildEventListener listener) {
        (TextUtils.isEmpty(childKey) ? mChildReference : mChildReference.child(childKey)).addChildEventListener(listener);
    }

    @Override
    public void update(String childKey, ContentValues values) {
        if (mEnabled) {
            try {
                if (values.containsKey(InventoryContract.ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE)) {
                    values.put(InventoryContract.ItemsEntry.FIREBASE_COLUMN_ITEM_PICTURE,
                            Base64.encodeToString(ImageFileUtils.getBytes(
                                    getDatabase().getApp().getApplicationContext(),
                                    TextUtils.isEmpty(childKey) ?
                                            mChildReference.getKey() : childKey), Base64.DEFAULT));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for (String key : values.keySet()) {
                (TextUtils.isEmpty(childKey) ?
                        mChildReference :
                        mChildReference.child(childKey))
                        .child(key)
                        .setValue(values.get(key));
            }
        }
    }

    @Override
    public void delete(String childKey) {
        if (mEnabled) {
            (TextUtils.isEmpty(childKey) ? mChildReference : mChildReference.child(childKey)).removeValue();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    private FirebaseDatabase getDatabase() {
        return mChildReference.getDatabase();
    }

    public void setChildReference(DatabaseReference reference) {
        mChildReference = reference;
    }

}
