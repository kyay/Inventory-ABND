package com.example.android.inventory.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract class for the Inventory Database.
 */
@SuppressWarnings("WeakerAccess")
public final class InventoryContract {
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ItemsEntry.TABLE_NAME + " (" +
            ItemsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ItemsEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
            ItemsEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            ItemsEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL DEFAULT -1, " +
            ItemsEntry.COLUMN_ITEM_DESCRIPTION + " TEXT, " +
            ItemsEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT, " +
            ItemsEntry.COLUMN_ITEM_SUPPLIER_NUMBER + " TEXT, " +
            ItemsEntry.COLUMN_ITEM_DATE_CREATED + " TEXT " + ")";
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "items";
    public static final String PATH_SEQUENCE = "SQLITE_SEQUENCE";

    private InventoryContract() {
    }

    /**
     * The table for the items in the inventory
     */
    public static abstract class ItemsEntry implements BaseColumns {
        public static final String TABLE_NAME = "items";
        public static final String _ID = BaseColumns._ID;
        /**
         * The name of the item. Stored as a String
         */
        public static final String COLUMN_ITEM_NAME = "name";
        /**
         * The quantity of the item. Stored as an Integer
         */
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        /**
         * The price of the item. Stored as a Double
         */
        public static final String COLUMN_ITEM_PRICE = "price";
        /**
         * The description of the item. Stored as a String
         */
        public static final String COLUMN_ITEM_DESCRIPTION = "description";
        /**
         * The name of the supplier of the item. Stored as a String
         */
        public static final String COLUMN_ITEM_SUPPLIER_NAME = "supplier_name";
        /**
         * The phone number of the supplier of the item. Stored as a String
         */
        public static final String COLUMN_ITEM_SUPPLIER_NUMBER = "supplier_number";
        /**
         * The date and time when the item was created. Stored as a String
         */
        public static final String COLUMN_ITEM_DATE_CREATED = "date_created";
        /**
         * This column is only used in firebase, don't use it when adding to the SQL database.
         */
        public static final String FIREBASE_COLUMN_ITEM_PICTURE = "picture";

        public static final String[] PROJECTION_ALL = new String[]{
                COLUMN_ITEM_NAME,
                COLUMN_ITEM_QUANTITY,
                COLUMN_ITEM_PRICE,
                COLUMN_ITEM_DESCRIPTION,
                COLUMN_ITEM_DATE_CREATED,
                COLUMN_ITEM_SUPPLIER_NAME,
                COLUMN_ITEM_SUPPLIER_NUMBER
        };
        public static final String[] PROJECTION_NO_SUPPLIER = new String[]{
                _ID,
                COLUMN_ITEM_NAME,
                COLUMN_ITEM_QUANTITY,
                COLUMN_ITEM_PRICE,
                COLUMN_ITEM_DESCRIPTION,
                COLUMN_ITEM_DATE_CREATED
        };

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

    }
}
