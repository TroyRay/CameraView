package com.troy.cameralib.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des: FileUtil
 */

public class FileUtil {
    private static final  String TAG = FileUtil.class.getSimpleName();
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static   String storagePath = "";
    private static final String FOLDER_NAME = "CameraView";

    /**
     * 保存Bitmap到SD卡
     * @param b
     * @param savePath
     */
    public static void saveBitmap(Bitmap b,String savePath){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(savePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }
    }

    /**
     * 根据文件Uri获取文件路径
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context,final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String path = null;
        if ( scheme == null )
            path = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            path = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        path = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return path;
    }

    /**
     * 获取照片被旋转的角度
     * @param data
     * @return
     */
    public static int getRotateDegree(byte[] data){
        // Find out if the picture needs rotating by looking at its Exif data
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(new ByteArrayInputStream(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }

    public static Bitmap rotateBitmap(int angle,Bitmap bitmap) {
        if(bitmap != null){
            int myWidth = bitmap.getWidth();
            int myHeight = bitmap.getHeight();
            //旋转图片 动作
            Matrix mMatrix = new Matrix();
            mMatrix.postRotate(angle);
            // 创建新的图片
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    myWidth, myHeight, mMatrix, true);
            return resizedBitmap;
        }else{
            return bitmap;
        }
    }

}
