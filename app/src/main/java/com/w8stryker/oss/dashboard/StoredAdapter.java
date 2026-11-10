package com.w8stryker.oss.dashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.w8stryker.oss.R;
import com.w8stryker.oss.utils.Core;
import com.w8stryker.oss.utils.QrHelper;

import java.util.ArrayList;
import java.util.Locale;

public class StoredAdapter extends RecyclerView.Adapter<StoredAdapter.ViewHolder> {

    private final Activity activity;
    private final Context context;
    private final Core core;
    private final ArrayList<ArrayList<String>> stored = new ArrayList<>();

    public StoredAdapter(Context context, Activity activity, ArrayList<ArrayList<String>> stored) {
        this.activity = activity;
        this.context = context;
        this.core = new Core(context);
        if (stored == null) return;
        for (ArrayList<String> network : stored) {
            if (network != null && network.size() >= 4) {
                this.stored.add(network);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.storedwifi_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, int position) {
        ArrayList<String> network = stored.get(position);
        String psk = field(network, 0);
        String pin = field(network, 1);
        String ssid = field(network, 2);
        String bssid = field(network, 3);
        String prevSsid = field(network, 4);
        if (prevSsid == null || prevSsid.isEmpty()) {
            prevSsid = core.checkBssidNameChange(bssid, ssid);
        }
        String date = field(network, 5);

        if (prevSsid != null && !prevSsid.isEmpty() && !prevSsid.equalsIgnoreCase(ssid)) {
            adapter.name.setText(ssid + " [⚠️ Was: " + prevSsid + "]\n(" + bssid.toUpperCase(Locale.ROOT) + ")");
        } else {
            adapter.name.setText(ssid + " (" + bssid.toUpperCase(Locale.ROOT) + ")");
        }

        adapter.additional.setText(getStyledAdditionalInfo(network));

        // Show details dialog with QR code and auto-connect
        final String finalPrev = prevSsid;
        View.OnClickListener showDetails = view -> {
            QrHelper.showWifiDetailsDialog(
                    context,
                    ssid,
                    bssid,
                    psk,
                    pin,
                    finalPrev,
                    date,
                    () -> {
                        core.connectWiFi2(ssid, psk);
                        core.toaster("Connecting to " + ssid + "…");
                    }
            );
        };

        adapter.cardView.setOnClickListener(showDetails);
        if (adapter.qr_code_btn != null) {
            adapter.qr_code_btn.setOnClickListener(showDetails);
        }

        adapter.copy_password.setOnClickListener(view -> {
            core.copyToClipBoard(psk);
            core.toaster("Password copied to clipboard!");
        });
        adapter.copy_password.setOnLongClickListener(view -> {
            core.toaster("Copy password to clipboard.");
            return false;
        });

        adapter.delete.setOnClickListener(view -> removeNetwork(network));
        adapter.cardView.setOnLongClickListener(view -> {
            removeNetwork(network);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return stored.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static String field(ArrayList<String> network, int index) {
        String value = index < network.size() ? network.get(index) : null;
        return value == null ? "" : value;
    }

    private CharSequence getStyledAdditionalInfo(ArrayList<String> network) {
        String password = field(network, 0);
        String wps_pin = field(network, 1);
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if (!password.isEmpty()) {
            int start = ssb.length();
            ssb.append("Password: ").append(password);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FF1744")), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (!wps_pin.isEmpty()) {
            if (ssb.length() > 0) ssb.append("\n");
            int start = ssb.length();
            ssb.append("WPS Pin: ").append(wps_pin);
            ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#00E676")), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), start, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    private void removeNetwork(ArrayList<String> network) {
        MaterialAlertDialogBuilder adb = new MaterialAlertDialogBuilder(context)
                .setTitle("Confirmation")
                .setMessage("Are you sure to delete this saved network?")
                .setPositiveButton(context.getResources().getString(R.string.yes), (di, i) -> {
                    int index = stored.indexOf(network);
                    if (index < 0) return;
                    core.removeSavedNetwork(field(network, 3));
                    stored.remove(index);
                    notifyItemRemoved(index);
                })
                .setNegativeButton(android.R.string.cancel, (di, i) -> {});
        adb.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView additional;
        public ImageView delete;
        public ImageView copy_password;
        public ImageView qr_code_btn;
        public MaterialCardView cardView;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            additional = v.findViewById(R.id.additional);
            delete = v.findViewById(R.id.delete);
            copy_password = v.findViewById(R.id.copy);
            qr_code_btn = v.findViewById(R.id.qr_code_btn);
            cardView = v.findViewById(R.id.item);
        }
    }
}
