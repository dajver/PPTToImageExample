package project.dajver.com.ppttoimage.task.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * Created by gleb on 12/6/17.
 */

public class ImageFilePathUtils {
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(uri != null) {
                if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                    if (isExternalStorageDocument(uri)) {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            return Environment.getExternalStorageDirectory() + "/" + split[1];
                        }else {
                            //Below logic is how External Storage provider build URI for documents
                            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

                            try {
                                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                                Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                                Method getUuid = storageVolumeClazz.getMethod("getUuid");
                                Method getState = storageVolumeClazz.getMethod("getState");
                                Method getPath = storageVolumeClazz.getMethod("getPath");
                                Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                                Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                                Object result = getVolumeList.invoke(mStorageManager);

                                final int length = Array.getLength(result);
                                for (int i = 0; i < length; i++) {
                                    Object storageVolumeElement = Array.get(result, i);
                                    //String uuid = (String) getUuid.invoke(storageVolumeElement);

                                    final boolean mounted = Environment.MEDIA_MOUNTED.equals( getState.invoke(storageVolumeElement) )
                                            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                                    //if the media is not mounted, we need not get the volume details
                                    if (!mounted) continue;

                                    //Primary storage is already handled.
                                    if ((Boolean)isPrimary.invoke(storageVolumeElement) && (Boolean)isEmulated.invoke(storageVolumeElement)) continue;

                                    String uuid = (String) getUuid.invoke(storageVolumeElement);

                                    if (uuid != null && uuid.equals(type))
                                    {
                                        String res =getPath.invoke(storageVolumeElement) + "/" +split[1];
                                        return res;
                                    }
                                }
                            }
                            catch (Exception ex) {
                            }
                        }
                    } else if (isDownloadsDocument(uri)) {
                        final String id = DocumentsContract.getDocumentId(uri);
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } else if (isMediaDocument(uri)) {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];

                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }
                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[]{
                                split[1]
                        };
                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                    if (isGooglePhotosUri(uri))
                        return uri.getLastPathSegment();
                    return getDataColumn(context, uri, null, null);
                } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
