package com.troy.cameralib.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des:
 */

public class DisplayUtil {

    /**
     * 生成屏幕中间的矩形
     * @param w 目标矩形的宽度，单位px
     * @param h 目标矩形的高度，单位px
     * @return
     */
    public static Rect createCenterScreenRect(int viewWidth, int viewHeight, int w, int h) {
        int x1 = viewWidth / 2 - w / 2;
        int y1 = viewHeight / 2 - h / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    /**
     * 生成拍照后图片的中间矩形的宽度和高度
     *
     * @return
     */
    public static Point createCenterPictureRect(float ratio, float cameraRatio, int picSizeX, int picSizeY) {
        int wRectPicture;
        int hRectPicture;
        if (ratio > cameraRatio) {
            hRectPicture = picSizeY;
            wRectPicture = (int) (picSizeY / ratio);
        } else {
            wRectPicture = picSizeX;
            hRectPicture = (int) (picSizeX * ratio);
        }
        return new Point(wRectPicture, hRectPicture);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displaymetrics);
        return displaymetrics;
    }

    /**
     * @param context
     * @return
     */
    public static float getScreenHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    public static float getScreenWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     */
    public static float px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static float dp2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }


    /**
     * 读取照片旋转角度
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

}
