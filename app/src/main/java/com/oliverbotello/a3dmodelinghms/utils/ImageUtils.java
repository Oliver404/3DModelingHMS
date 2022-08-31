package com.oliverbotello.a3dmodelinghms.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.FieldPosition;
import java.util.concurrent.ExecutionException;

public class ImageUtils {
    public static void cropImagesInDirectory(String path) throws Exception {
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] lstImages = directory.listFiles();
            File modelDirectory = new File(
                    Constants.MATERIALS_PATH + File.separator + directory.getName()
            );
            int newWidth = 0;

            if (!modelDirectory.exists()) {
                if (!modelDirectory.mkdirs())
                    throw new Exception("Fail: Can't create texture directory");
            }

            for (File image : lstImages) {
                if (!image.isDirectory()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                    newWidth = bitmap.getWidth();
                    bitmap = Bitmap.createBitmap(bitmap, 0, newWidth, newWidth, newWidth);

                    if (newWidth > Constants.TEXTURE_IMG_MAX_SIZE) {
                        while (newWidth > Constants.TEXTURE_IMG_MAX_SIZE) newWidth *= .9;

                        bitmap = changeSizeImage(bitmap, newWidth, newWidth);
                    }

                    bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            new FileOutputStream(modelDirectory.getAbsolutePath() +  File.separator + image.getName())
                    );
                    Log.e("3Dhms", "Processing image: " + image.getName());
                    Log.e("3Dhms", "width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
                }
            }
        }
    }

    public static void changeSizeImagesInDirectory(String path) throws Exception {
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] lstImages = directory.listFiles();
            File modelDirectory = new File(
                    Constants.MODELS_PATH + File.separator + directory.getName()
            );
            int newWidth = 0, newHeight = 0;

            if (!modelDirectory.exists()) {
                if (!modelDirectory.mkdirs())
                    throw new Exception("Fail: Can't create model directory");
            }

            for (File image : lstImages) {
                if (!image.isDirectory()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());

                    if (newWidth == 0 || newHeight == 0) {
                        newWidth = bitmap.getWidth();
                        newHeight = bitmap.getHeight();

                        while (
                                newHeight > Constants.MODEL_IMG_MAX_H
                                || newWidth > Constants.MODEL_IMG_MAX_W
                        ) {
                            newHeight *= .9;
                            newWidth *= .9;
                        }
                    }

                    if (bitmap.getHeight() != newHeight || bitmap.getWidth() != newWidth)
                        bitmap = changeSizeImage(bitmap, newWidth, newHeight);

                    bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            new FileOutputStream(modelDirectory.getAbsolutePath() +  File.separator + image.getName())
                    );
                    Log.e("3Dhms", "Processing image: " + image.getName());
                    Log.e("3Dhms", "width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
                }
            }
        }
    }

    public static Bitmap changeSizeImage(Bitmap bitmap, int width, int height) {
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        return bitmap;
    }
}
