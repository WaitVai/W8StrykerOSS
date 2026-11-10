package com.zalexdev.stryker.utils;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.zalexdev.stryker.R;

import java.util.HashMap;
import java.util.Map;

public final class QrHelper {

    private QrHelper() {
    }

    public static String buildWifiString(String ssid, String password) {
        String type = (password == null || password.trim().isEmpty()) ? "nopass" : "WPA";
        String safeSsid = ssid != null ? escape(ssid) : "";
        String safePass = password != null ? escape(password) : "";
        return "WIFI:T:" + type + ";S:" + safeSsid + ";P:" + safePass + ";;";
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace(":", "\\:")
                .replace("\"", "\\\"");
    }

    public static Bitmap generateQrBitmap(String content, int size) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void showWifiQrDialog(Context context, String ssid, String password, String wpsPin) {
        showWifiDetailsDialog(context, ssid, null, password, wpsPin, null, null, null);
    }

    public static void showWifiDetailsDialog(Context context, String ssid, String bssid,
                                            String password, String wpsPin, String prevName,
                                            String date, Runnable onConnect) {
        if (context == null) return;
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_qr, null, false);
        dialog.setContentView(v);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView title = v.findViewById(R.id.qr_title);
        TextView macText = v.findViewById(R.id.qr_mac);
        TextView nameChangeText = v.findViewById(R.id.qr_name_change);
        TextView passText = v.findViewById(R.id.qr_password);
        TextView pinText = v.findViewById(R.id.qr_pin);
        TextView dateText = v.findViewById(R.id.qr_date);
        ImageView qrImage = v.findViewById(R.id.qr_image);
        MaterialButton connectBtn = v.findViewById(R.id.qr_connect);
        MaterialButton copyBtn = v.findViewById(R.id.qr_copy_pass);
        MaterialButton close = v.findViewById(R.id.qr_close);

        if (title != null) title.setText(ssid == null ? "" : ssid);
        if (macText != null) {
            if (bssid != null && !bssid.isEmpty()) {
                macText.setText("BSSID: " + bssid.toUpperCase(java.util.Locale.ROOT));
                macText.setVisibility(View.VISIBLE);
            } else {
                macText.setVisibility(View.GONE);
            }
        }
        if (nameChangeText != null) {
            if (prevName != null && !prevName.isEmpty() && !prevName.equalsIgnoreCase(ssid)) {
                nameChangeText.setText("⚠️ SSID Changed! Previous: " + prevName);
                nameChangeText.setVisibility(View.VISIBLE);
            } else {
                nameChangeText.setVisibility(View.GONE);
            }
        }
        if (passText != null) {
            if (password != null && !password.isEmpty()) {
                passText.setText("Password: " + password);
                passText.setTextColor(Color.parseColor("#FF1744")); // RED
                passText.setVisibility(View.VISIBLE);
            } else {
                passText.setVisibility(View.GONE);
            }
        }
        if (pinText != null) {
            if (wpsPin != null && !wpsPin.isEmpty()) {
                pinText.setText("WPS Pin: " + wpsPin);
                pinText.setTextColor(Color.parseColor("#00E676")); // GREEN
                pinText.setVisibility(View.VISIBLE);
            } else {
                pinText.setVisibility(View.GONE);
            }
        }
        if (dateText != null) {
            if (date != null && !date.isEmpty()) {
                dateText.setText("Saved: " + date);
                dateText.setVisibility(View.VISIBLE);
            } else {
                dateText.setVisibility(View.GONE);
            }
        }

        String qrStr = buildWifiString(ssid, password);
        Bitmap qrBmp = generateQrBitmap(qrStr, 512);
        if (qrBmp != null && qrImage != null) {
            qrImage.setImageBitmap(qrBmp);
        }

        if (connectBtn != null) {
            if (onConnect != null) {
                connectBtn.setVisibility(View.VISIBLE);
                connectBtn.setOnClickListener(b -> {
                    onConnect.run();
                    dialog.dismiss();
                });
            } else {
                connectBtn.setVisibility(View.GONE);
            }
        }

        if (copyBtn != null) {
            if (password != null && !password.isEmpty()) {
                copyBtn.setVisibility(View.VISIBLE);
                copyBtn.setOnClickListener(b -> {
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("WiFi Password", password));
                        Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                copyBtn.setVisibility(View.GONE);
            }
        }

        if (close != null) {
            close.setOnClickListener(view -> dialog.dismiss());
        }

        dialog.show();
    }
}
