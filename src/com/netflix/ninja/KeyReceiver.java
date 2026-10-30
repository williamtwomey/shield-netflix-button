package com.netflix.ninja;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * NVIDIA shieldtech (NvNetflix.handleKeyEvent) answers the remote Netflix button with a
 * com.netflix.ninja.intent.action.NETFLIX_KEY broadcast to this package, not a launch.
 */
public class KeyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Target.open(context);
    }
}
