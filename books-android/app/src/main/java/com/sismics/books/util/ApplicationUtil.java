package com.sismics.books.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Utility class on general application data.
 * 
 * @author bgamard
 */
public class ApplicationUtil {

    /**
     * Returns version name.
     * 
     * @param context Context
     * @return Version name
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    /**
     * Returns version number.
     * 
     * @param context Context
     * @return Version code
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }
}
