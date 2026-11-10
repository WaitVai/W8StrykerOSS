package com.zalexdev.stryker.ota;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class UpdateScheduler {

    private static final String UNIQUE_WORK = "stryker_update_poll";

    private UpdateScheduler() {
    }

    public static void schedule(Context context) {
        try {
            // Auto-update disabled
            WorkManager.getInstance(context.getApplicationContext())
                    .cancelUniqueWork(UNIQUE_WORK);
        } catch (Exception ignored) {
        }
    }
}
