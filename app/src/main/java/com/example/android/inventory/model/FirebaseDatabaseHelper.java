package com.example.android.inventory.model;

import android.content.ContentValues;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;

/**
 * An Interface that helps {@link android.content.ContentProvider}s easily use
 * {@link com.google.firebase.database.FirebaseDatabase} by adding methods that are very similar
 * to those in the content provider class. This also helps in testing when you don't want to really
 * use a Firebase Database.
 */
public interface FirebaseDatabaseHelper {
    void insert(String key, ContentValues values);

    void addListenerForSingleValueEvent(String key, ValueEventListener valueEventListener);

    void addChildEventListener(String key, ChildEventListener listener);

    void update(String key, ContentValues values);

    void delete(String key);

    void setEnabled(boolean enabled);

}
