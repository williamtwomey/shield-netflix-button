package com.netflix.ninja;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/** The app the Netflix button opens, stored in SharedPreferences. */
final class Target {
    static final String DEFAULT = "com.google.android.youtube.tv";
    private static final String PREFS = "target";
    private static final String KEY = "package";

    private Target() {}

    static String get(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, DEFAULT);
    }

    static void set(Context context, String pkg) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, pkg).apply();
    }

    private static Intent launchIntent(PackageManager pm, String pkg) {
        Intent launch = pm.getLeanbackLaunchIntentForPackage(pkg);
        return launch != null ? launch : pm.getLaunchIntentForPackage(pkg);
    }

    /** Opens the chosen app, or YouTube if the chosen one has since been uninstalled. */
    static void open(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent launch = launchIntent(pm, get(context));
        if (launch == null) {
            launch = launchIntent(pm, DEFAULT);
        }
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        }
    }
}
