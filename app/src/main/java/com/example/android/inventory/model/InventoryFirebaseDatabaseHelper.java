package com.example.android.inventory.model;

import android.content.ContentValues;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A subclass of the {@link BaseFirebaseDatabaseHelper} but with the addition of only allowing
 * insertion if the user is signed in.
 */
public class InventoryFirebaseDatabaseHelper extends BaseFirebaseDatabaseHelper {
    private final HashMap<String, ValueEventListener> mValueEventListeners = new HashMap<>();
    private final HashMap<String, ValueEventListener> mSingleValueEventListeners = new HashMap<>();
    private final HashMap<String, ChildEventListener> mChildEventListeners = new HashMap<>();
    private String mUserId;

    public InventoryFirebaseDatabaseHelper() {
        super(InventoryContract.ItemsEntry.TABLE_NAME + "/" +
                FirebaseAuth.getInstance().getUid());
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            mUserId = firebaseAuth.getUid();
            if (!TextUtils.isEmpty(mUserId)) {
                setChildReference(
                        FirebaseDatabase.getInstance().getReference().child(
                                InventoryContract.ItemsEntry.TABLE_NAME + "/" +
                                        mUserId));
                for (Map.Entry<String, ValueEventListener> currentEntry : mValueEventListeners.entrySet()) {
                    addValueEventListener(currentEntry.getKey(), currentEntry.getValue());
                }
                mValueEventListeners.clear();
                for (Map.Entry<String, ValueEventListener> currentEntry : mSingleValueEventListeners.entrySet()) {
                    addListenerForSingleValueEvent(currentEntry.getKey(), currentEntry.getValue());
                }
                mSingleValueEventListeners.clear();
                for (Map.Entry<String, ChildEventListener> currentEntry : mChildEventListeners.entrySet()) {
                    addChildEventListener(currentEntry.getKey(), currentEntry.getValue());
                }
                mChildEventListeners.clear();
            }
        });
    }

    @Override
    public void addChildEventListener(String childKey, ChildEventListener listener) {
        if (!TextUtils.isEmpty(mUserId)) super.addChildEventListener(childKey, listener);
        else mChildEventListeners.put(childKey, listener);
    }

    @Override
    public void insert(String childKey, ContentValues values) {
        if (!TextUtils.isEmpty(mUserId)) super.insert(childKey, values);
    }

    public void addValueEventListener(String childKey, ValueEventListener valueEventListener) {
        if (!TextUtils.isEmpty(mUserId)) super.addValueEventListener(childKey, valueEventListener);
        else mValueEventListeners.put(childKey, valueEventListener);
    }

    @Override
    public void addListenerForSingleValueEvent(String childKey, ValueEventListener valueEventListener) {
        if (!TextUtils.isEmpty(mUserId))
            super.addListenerForSingleValueEvent(childKey, valueEventListener);
        else mSingleValueEventListeners.put(childKey, valueEventListener);
    }

    @Override
    public void update(String childKey, ContentValues values) {
        if (!TextUtils.isEmpty(mUserId)) super.update(childKey, values);
    }

    @Override
    public void delete(String childKey) {
        if (!TextUtils.isEmpty(mUserId)) super.delete(childKey);
    }

    public void setChildReference(DatabaseReference reference) {
        if (!TextUtils.isEmpty(mUserId)) super.setChildReference(reference);
    }

}
