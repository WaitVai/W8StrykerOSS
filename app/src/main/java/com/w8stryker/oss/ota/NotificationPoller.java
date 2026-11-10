package com.w8stryker.oss.ota;

import android.content.Context;
import android.content.SharedPreferences;

import com.w8stryker.oss.BuildConfig;
import com.w8stryker.oss.R;

import java.util.HashSet;
import java.util.Set;

public final class NotificationPoller {

    private static final String KEY_SEEN = "seen_notifications";
    private static final String KEY_OTA_NOTIFIED = "ota_notified_version";

    private NotificationPoller() {
    }

    public static void poll(Context context) {
        RemoteManifest manifest = ManifestService.fetch(context);
        if (manifest == null) {
            return;
        }
        SharedPreferences prefs = ManifestService.prefs(context);

        Set<String> seen = new HashSet<>(prefs.getStringSet(KEY_SEEN, new HashSet<>()));
        boolean changed = false;
        for (RemoteManifest.NotificationItem item : manifest.notifications) {
            if (item.id == 0 || item.title == null || item.title.isEmpty()) {
                continue;
            }
            String key = String.valueOf(item.id);
            if (seen.contains(key)) {
                continue;
            }
            NotificationCenter.postNews(context, item.id, item.title, item.body, item.url);
            seen.add(key);
            changed = true;
        }
        if (changed) {
            prefs.edit().putStringSet(KEY_SEEN, seen).apply();
        }
        // Auto-update notification disabled
    }
}
