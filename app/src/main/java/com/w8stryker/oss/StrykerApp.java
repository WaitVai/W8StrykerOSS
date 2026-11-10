package com.w8stryker.oss;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.w8stryker.oss.logger.LogEntry;
import com.w8stryker.oss.logger.LogStore;
import com.w8stryker.oss.ota.NotificationCenter;
import com.w8stryker.oss.ota.UpdateScheduler;
import com.w8stryker.oss.utils.Utils;

public class StrykerApp extends com.stryker.terminal.App {

    @Override
    public void onCreate() {
        super.onCreate();
        LogStore store = LogStore.init(this);
        store.add(LogEntry.INFO, "session", "==== Stryker " + BuildConfig.VERSION_NAME
                + " session start ====");
        String abi = Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown";
        store.add(LogEntry.INFO, "session", "Device: " + Build.MANUFACTURER + " " + Build.MODEL
                + " · Android " + Build.VERSION.RELEASE
                + " (" + abi + ")");
        com.stryker.terminal.bridge.StrykerLog.install((level, tag, message, error) -> {
            LogStore s = LogStore.peek();
            if (s == null) return;
            String text = message;
            if (error != null) {
                text = (text == null || text.isEmpty() ? "" : text + System.lineSeparator())
                        + android.util.Log.getStackTraceString(error);
            }
            s.add(level, tag, text);
        });

        NotificationCenter.ensureChannel(this);
        UpdateScheduler.schedule(this);
        applyEdgeToEdgeInsets();
        com.w8stryker.oss.engine.GuestShellFactory.install();
        com.stryker.terminal.backend.TerminalSession.setShellChooser(() -> {
            try {
                com.w8stryker.oss.utils.Core core = new com.w8stryker.oss.utils.Core(this);
                if (!com.w8stryker.oss.engine.EngineType.isChosen(core)) return null;
                return com.w8stryker.oss.engine.EngineType.isRootless(core) ? "ssh:guest" : null;
            } catch (Throwable t) {
                return null;
            }
        });
    }

    private void applyEdgeToEdgeInsets() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                Utils.applySystemBarInsets(activity);
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle state) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle b) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}
