package com.example.android.inventory.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A {@link SQLiteOpenHelper} that creates the Inventory SQL Database
 */
public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    private final Context mContext;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(InventoryContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Loop through all the respective SQL scripts that should be executed.
            // Copied from: https://riggaroo.co.za/android-sqlite-database-use-onupgrade-correctly/.
            for (int i = oldVersion; i < newVersion; ++i) {
                @SuppressLint("DefaultLocale")
                String migrationName = String.format("from_%d_to_%d.sql", i, (i + 1));
                readAndExecuteSQLScript(db, mContext, migrationName);
            }
        } catch (Exception exception) {
            Log.e(getClass().getSimpleName(), "Exception while running upgrade script:", exception);
        }

    }

    private void readAndExecuteSQLScript(SQLiteDatabase db, Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) return;

        AssetManager assetManager = context.getAssets();
        BufferedReader reader = null;

        try {
            InputStream stream = assetManager.open(fileName);
            InputStreamReader streamReader = new InputStreamReader(stream);
            reader = new BufferedReader(streamReader);
            executeSQLScript(db, reader);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "IOException while reading from file:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), "IOException while closing reader:", e);
                }
            }
        }

    }

    private void executeSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException {
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            statement.append(line);
            statement.append("\n");
            if (line.endsWith(";")) {
                db.execSQL(statement.toString());
                statement = new StringBuilder();
            }
        }
    }
}
