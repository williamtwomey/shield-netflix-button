package com.netflix.ninja;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Launcher tile: pick the app the Netflix button opens. Launching it also takes the
 * package out of the stopped state, so the NETFLIX_KEY broadcast reaches KeyReceiver.
 * From adb: am start -n com.netflix.ninja/.Picker --es target <package>
 */
public class Picker extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String target = getIntent().getStringExtra("target");
        if (target != null) {
            Target.set(this, target);
            finish();
            return;
        }

        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        // label -> package, sorted case-insensitively for the list
        TreeMap<String, String> apps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ResolveInfo ri : pm.queryIntentActivities(query, 0)) {
            String pkg = ri.activityInfo.packageName;
            if (!pkg.equals(getPackageName())) {
                apps.put(ri.loadLabel(pm).toString(), pkg);
            }
        }
        final String[] labels = apps.keySet().toArray(new String[0]);
        final List<String> pkgs = new ArrayList<>(apps.values());
        int checked = pkgs.indexOf(Target.get(this));

        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Netflix button opens")
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    Target.set(this, pkgs.get(which));
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> finish())
                .show();
    }
}
