package com.lei.recyclerview.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 用于转换界面尺寸的工具类
 */
public class DisplayUtils {
    
    
    
    /**
     * 获得屏幕宽度，单位为px
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获得屏幕高度,单位为px
     * @param activity
     * @return
     */
    public static int getScreenHigh(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 传入dp单位值 转换为px单位值
     * 
     * @param context
     * @param dip
     * @return
     */
    public static int convertDIP2PX(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }

    /**
     * 传入px单位值 转换为dp单位值
     * 
     * @param context
     * @param px
     * @return
     */
    public static int convertPX2DIP(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f * (px >= 0 ? 1 : -1));
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
