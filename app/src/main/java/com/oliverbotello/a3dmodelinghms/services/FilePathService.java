package com.oliverbotello.a3dmodelinghms.services;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;

public class FilePathService {
    private static final String TAG = "FilePathProvider";
    private static final String EXTERNAL_CONTENT_URI = "com.android.externalstorage.documents";
    private static final String DOWNLOADS_CONTENT_URI = "com.android.providers.downloads.documents";
    private static final String MEDIA_CONTENT_URI = "com.android.providers.media.documents";

    public String getPath(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);

            if (isDownloadsUri(uri)) {
                if (docId.contains(":"))
                    docId = docId.split("[:]")[1];

                Uri contentUri = ContentUris.withAppendedId(
                         Uri.parse("content://downloads/public_downloads/browser"),
                        Long.parseLong(docId)
                );

                return getDataColumn(context, contentUri, null, null);
            } else {
                String[] split = docId.split(":");
                String type = split[0];

                if (isExternalStorageUri(uri)) {
                    if ("primary".equalsIgnoreCase(type))
                        return Environment.getExternalStorageDirectory().toString()
                                + "/" + split[1];
                } else if ( isMediaUri(uri)) {
                    Uri contentUri = null;
                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};

                    if ("image".equals(type))
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    else if ("video".equals(type))
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    else if ("audio".equals(type))
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    else
                        contentUri = MediaStore.Files.getContentUri("external");

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn(
            Context context,
            Uri uri,
            String selection,
            String[] selectionArgs
    ) {
        Cursor cursor = null;
        final String column = "_data";
        String[] projection = new String[]{column};

        try {
            cursor = context.getContentResolver()
                    .query(uri, projection, selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst())
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private boolean isExternalStorageUri(Uri uri) {
        return EXTERNAL_CONTENT_URI.equals(uri.getAuthority());
    }

    private boolean isDownloadsUri(Uri uri) {
        return DOWNLOADS_CONTENT_URI.equals(uri.getAuthority());
    }

    private boolean isMediaUri(Uri uri) {
        return MEDIA_CONTENT_URI.equals(uri.getAuthority());
    }

    public String getImageFromAlbum(String folder, Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folder);

        Uri from  = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        String path = getPath(context, from);

        if (path != null)
            path = new File(path).getParent();

        return path;
    }

    public Uri getImageUri(Context context, String folder, Uri uriImage) {
        File imgFile = new File(getPath(context, uriImage));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folder + "/" + imgFile.getName());

        Uri from  = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return from;
    }

    public Uri saveBitmapAsImage(Bitmap bitmap, String folder, Context context)  {
        Uri uriFile = null;

        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folder);
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                if (saveImageToStream(bitmap, context, uri))
                    uriFile = uri;

                values.put(MediaStore.Images.Media.IS_PENDING, false);
                context.getContentResolver().update(uri, values, null, null);
            }
        }
        else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + File.separator + folder);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + ".png";
            File file = new File(directory, fileName);
            Uri uri = Uri.fromFile(file);

            if(saveImageToStream(bitmap, context, uri)) {
                uriFile = uri;
            }

            if (file.getAbsolutePath() != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        }

        return uriFile;
    }

    private boolean saveImageToStream(Bitmap bitmap, Context context, Uri uri) {
        boolean imageSaved = false;
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            }

            imageSaved = true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return imageSaved;
    }
}