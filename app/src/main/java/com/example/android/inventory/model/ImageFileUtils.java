package com.example.android.inventory.model;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A Utility class that helps in saving and retrieving bitmaps from files.
 */
@SuppressWarnings("WeakerAccess")
public final class ImageFileUtils {
    private ImageFileUtils() {
        // Empty Constructor
    }

    public static void saveBitmap(Context context, Bitmap bitmap, String name) {
        try {
            FileOutputStream outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveBytes(Context context, byte[] bytes, String name) {
        try {
            FileOutputStream outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] getBytes(Context context, String name) throws FileNotFoundException {
        File file = new File(context.getFilesDir(), name);
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        try {
            fis.read(bytesArray);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesArray;
    }
}
