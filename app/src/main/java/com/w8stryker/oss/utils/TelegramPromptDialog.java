package com.w8stryker.oss.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.w8stryker.oss.R;

public final class TelegramPromptDialog {

    public static final String TELEGRAM_URL = Links.TELEGRAM;
    private static final String PREFS_NAME = "w8_community";
    private static final String KEY_TELEGRAM_JOINED = "telegram_joined";
    private static boolean sessionDismissed = false;

    private TelegramPromptDialog() {
    }

    public static void showOnAppOpen(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean alreadyJoined = prefs.getBoolean(KEY_TELEGRAM_JOINED, false);
        if (alreadyJoined || sessionDismissed) {
            return;
        }

        // Show fast on app open (small delay so Activity window layout is fully attached)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            show(activity, prefs);
        }, 500);
    }

    @SuppressLint("InflateParams")
    public static void show(Activity activity, SharedPreferences prefs) {
        try {
            Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_telegram_join, null);
            dialog.setContentView(view);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                window.getAttributes().windowAnimations = R.style.DialogAnimation_TelegramPopup;
            }

            dialog.setCancelable(true);

            View btnJoin = view.findViewById(R.id.btn_telegram_join);
            View btnAlready = view.findViewById(R.id.btn_telegram_already);
            View btnLater = view.findViewById(R.id.btn_telegram_later);
            View btnClose = view.findViewById(R.id.btn_telegram_close);
            ImageView tgIcon = view.findViewById(R.id.img_telegram_logo);

            if (tgIcon != null) {
                tgIcon.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.telegram_icon_pulse));
            }

            btnJoin.setOnClickListener(v -> {
                prefs.edit().putBoolean(KEY_TELEGRAM_JOINED, true).apply();
                sessionDismissed = true;
                dialog.dismiss();
                openTelegramChannel(activity);
            });

            if (btnAlready != null) {
                btnAlready.setOnClickListener(v -> {
                    prefs.edit().putBoolean(KEY_TELEGRAM_JOINED, true).apply();
                    sessionDismissed = true;
                    dialog.dismiss();
                });
            }

            if (btnLater != null) {
                btnLater.setOnClickListener(v -> {
                    sessionDismissed = true;
                    dialog.dismiss();
                });
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(v -> {
                    sessionDismissed = true;
                    dialog.dismiss();
                });
            }

            dialog.show();
        } catch (Exception ignored) {
        }
    }

    public static void openTelegramChannel(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/W8TeamNew"));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
            } catch (Exception ignored) {
            }
        }
    }
}
