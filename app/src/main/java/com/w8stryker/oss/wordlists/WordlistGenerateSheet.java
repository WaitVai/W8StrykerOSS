package com.w8stryker.oss.wordlists;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.w8stryker.oss.R;
import com.w8stryker.oss.utils.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WordlistGenerateSheet {

    private WordlistGenerateSheet() {}

    private enum Tab { PRESETS, BD_SIM, BD_NAME, NUMERIC, MUTATE }

    public static void show(Context context, Activity activity, Core core, Runnable onChanged) {
        build(context, activity, core, null, onChanged);
    }

    public static void showMutate(Context context, Activity activity, Core core,
                                  Wordlist source, Runnable onChanged) {
        build(context, activity, core, source, onChanged);
    }

    private static void build(Context context, Activity activity, Core core,
                              Wordlist mutateSource, Runnable onChanged) {

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_wordlist_generate);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        MaterialTextView title = dialog.findViewById(R.id.wlgen_title);
        MaterialTextView status = dialog.findViewById(R.id.wlgen_status);
        ChipGroup modes = dialog.findViewById(R.id.wlgen_modes);
        View modeScroll = dialog.findViewById(R.id.wlgen_mode_scroll);

        LinearLayout blockPresets = dialog.findViewById(R.id.wlgen_block_presets);
        LinearLayout blockBd = dialog.findViewById(R.id.wlgen_block_bd);
        LinearLayout blockBdName = dialog.findViewById(R.id.wlgen_block_bd_name);
        LinearLayout blockNumeric = dialog.findViewById(R.id.wlgen_block_numeric);
        LinearLayout blockMutate = dialog.findViewById(R.id.wlgen_block_mutate);
        LinearLayout blockOutput = dialog.findViewById(R.id.wlgen_block_output);

        ChipGroup bdOperatorChips = dialog.findViewById(R.id.wlgen_bd_operators);
        ChipGroup bdPrefixChips = dialog.findViewById(R.id.wlgen_bd_prefixes);
        MaterialTextView bdPreview = dialog.findViewById(R.id.wlgen_bd_preview);
        TextInputEditText bdSubPrefix = dialog.findViewById(R.id.wlgen_bd_subprefix);
        TextInputEditText bdFrom = dialog.findViewById(R.id.wlgen_bd_from);
        TextInputEditText bdTo = dialog.findViewById(R.id.wlgen_bd_to);

        ChipGroup bdNameSources = dialog.findViewById(R.id.wlgen_bd_name_sources);
        TextInputEditText bdNameCustom = dialog.findViewById(R.id.wlgen_bd_name_custom);
        ChipGroup bdNameCaps = dialog.findViewById(R.id.wlgen_bd_name_caps);
        ChipGroup bdNameNumModes = dialog.findViewById(R.id.wlgen_bd_name_num_modes);
        TextInputEditText bdNameDigits = dialog.findViewById(R.id.wlgen_bd_name_digits);
        View bdNameRandomWrap = dialog.findViewById(R.id.wlgen_bd_name_random_wrap);
        TextInputEditText bdNameRandomCount = dialog.findViewById(R.id.wlgen_bd_name_random_count);
        Chip bdNameMin8 = dialog.findViewById(R.id.wlgen_bd_name_min8);
        MaterialTextView bdNamePreview = dialog.findViewById(R.id.wlgen_bd_name_preview);

        TextInputEditText fromField = dialog.findViewById(R.id.wlgen_from);
        TextInputEditText toField = dialog.findViewById(R.id.wlgen_to);
        TextInputEditText padField = dialog.findViewById(R.id.wlgen_pad);
        ChipGroup mutationChips = dialog.findViewById(R.id.wlgen_mutations);
        TextInputEditText prefixField = dialog.findViewById(R.id.wlgen_prefix);
        TextInputEditText suffixField = dialog.findViewById(R.id.wlgen_suffix);

        TextInputEditText nameField = dialog.findViewById(R.id.wlgen_name);
        ChipGroup categoryChips = dialog.findViewById(R.id.wlgen_categories);
        Chip wpaChip = dialog.findViewById(R.id.wlgen_chip_wpa);
        MaterialTextView estimate = dialog.findViewById(R.id.wlgen_estimate);
        LinearProgressIndicator progress = dialog.findViewById(R.id.wlgen_progress);
        MaterialButton run = dialog.findViewById(R.id.wlgen_btn_run);
        MaterialButton cancel = dialog.findViewById(R.id.wlgen_btn_cancel);
        MaterialButton close = dialog.findViewById(R.id.wlgen_btn_close);

        final WordlistStore store = new WordlistStore(core);
        final Tab[] tab = { mutateSource != null ? Tab.MUTATE : Tab.PRESETS };
        final WordlistCategory[] category = { WordlistCategory.PASSWORD };
        final AtomicBoolean cancelled = new AtomicBoolean(false);
        final boolean[] running = { false };
        final String[] lastAutoName = { "" };

        final List<WordlistGenerator.BdOperator> bdOperators = WordlistGenerator.bdOperators();
        final List<Chip> opChipViews = new ArrayList<>();
        final List<Chip> pfxChipViews = new ArrayList<>();
        for (String pfx : WordlistGenerator.ALL_BD_PREFIXES) {
            Chip chip = toggle(context, bdPrefixChips, pfx, true);
            chip.setTag(pfx);
            pfxChipViews.add(chip);
        }

        final java.util.concurrent.Callable<List<String>> selectedBdPrefixes = () -> {
            List<String> list = new ArrayList<>();
            for (Chip chip : pfxChipViews) {
                if (chip.isChecked()) list.add(String.valueOf(chip.getTag()));
            }
            if (list.isEmpty()) list.addAll(Arrays.asList(WordlistGenerator.ALL_BD_PREFIXES));
            return list;
        };

        final java.util.concurrent.Callable<String> bdAutoFileName = () -> {
            List<String> selected = selectedBdPrefixes.call();
            String sub = WordlistGenerator.digitsOnly(text(bdSubPrefix));
            if (sub.length() > 8) sub = sub.substring(0, 8);
            String base = null;
            for (WordlistGenerator.BdOperator op : bdOperators) {
                if (Arrays.asList(op.prefixes).equals(selected)) {
                    base = op.defaultFileName.replace(".txt", "");
                    break;
                }
            }
            if (base == null) {
                if (selected.size() == WordlistGenerator.ALL_BD_PREFIXES.length) {
                    base = "bd-all-sim-013-019";
                } else {
                    StringBuilder sb = new StringBuilder("bd-sim");
                    for (String p : selected) sb.append('-').append(p);
                    base = sb.toString();
                }
            }
            if (!sub.isEmpty()) base = base + "-" + sub;
            return base + ".txt";
        };

        final boolean[] bdUseAllNames = { true };
        Chip srcAllChip = toggle(context, bdNameSources, "All BD Names (" + BuiltinLists.BD_NAMES.length + "+)", true);
        Chip srcCustomChip = toggle(context, bdNameSources, "Custom Name Only", false);

        final int[] capState = { 2 }; // 0 = Capital OFF (sojib), 1 = Capital ON (Sojib), 2 = Both (sojib + Sojib), 3 = ALL CAPS (SOJIB)
        Chip capOffChip = toggle(context, bdNameCaps, "Capital OFF (sojib12345)", false);
        Chip capOnChip = toggle(context, bdNameCaps, "Capital ON (Sojib12345)", false);
        Chip capBothChip = toggle(context, bdNameCaps, "Both (sojib + Sojib)", true);
        Chip capUpperChip = toggle(context, bdNameCaps, "ALL CAPS (SOJIB12345)", false);

        final WordlistGenerator.BdNameNumMode[] nameNumMode = { WordlistGenerator.BdNameNumMode.SMART_AND_RANDOM };
        Chip numSmartChip = toggle(context, bdNameNumModes, "Smart + Random (12345 + random)", true);
        Chip numRandomChip = toggle(context, bdNameNumModes, "Random Numbers Only", false);
        Chip numSeqChip = toggle(context, bdNameNumModes, "All Combinations (00000–99999)", false);

        final java.util.concurrent.Callable<String> bdNameAutoFileName = () -> {
            String custom = text(bdNameCustom).replaceAll("[^A-Za-z0-9_-]", "").toLowerCase(Locale.US);
            if (!bdUseAllNames[0] && !custom.isEmpty()) {
                return "bd-name-" + custom + ".txt";
            }
            return "bd-advance-names-pass.txt";
        };

        Chip mutCap = toggle(context, mutationChips, context.getString(R.string.wl_mut_capitalize), true);
        Chip mutUpper = toggle(context, mutationChips, context.getString(R.string.wl_mut_upper), false);
        Chip mutLeet = toggle(context, mutationChips, context.getString(R.string.wl_mut_leet), false);
        Chip mutRev = toggle(context, mutationChips, context.getString(R.string.wl_mut_reverse), false);
        Chip mutDigits = toggle(context, mutationChips, context.getString(R.string.wl_mut_digits), false);
        Chip mutYears = toggle(context, mutationChips, context.getString(R.string.wl_mut_years), false);

        for (WordlistCategory c : WordlistCategory.values()) {
            Chip chip = new Chip(context);
            chip.setText(c.title);
            chip.setCheckable(true);
            chip.setTextSize(11f);
            chip.setChipMinHeight(dp(context, 30));
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColor(ColorStateList.valueOf(c.color));
            chip.setChipBackgroundColorResource(R.color.light_contrast);
            chip.setTextColor(c.color);
            chip.setChecked(c == WordlistCategory.PASSWORD);
            chip.setOnClickListener(v -> category[0] = c);
            categoryChips.addView(chip);
        }

        final java.util.concurrent.Callable<WordlistGenerator.Spec> specOf = () -> {
            WordlistGenerator.Spec spec = new WordlistGenerator.Spec();
            spec.category = category[0];
            spec.wpaLengthOnly = wpaChip.isChecked();
            String enteredName = text(nameField);
            if (!enteredName.isEmpty()) {
                spec.name = enteredName;
            } else if (tab[0] == Tab.BD_SIM) {
                spec.name = bdAutoFileName.call();
            } else if (tab[0] == Tab.BD_NAME) {
                spec.name = bdNameAutoFileName.call();
            } else {
                spec.name = defaultName(tab[0]);
            }
            switch (tab[0]) {
                case BD_SIM: {
                    spec.mode = WordlistGenerator.Mode.BD_SIM;
                    spec.limit = Long.MAX_VALUE;
                    spec.bdPrefixes = selectedBdPrefixes.call();
                    String sub = WordlistGenerator.digitsOnly(text(bdSubPrefix));
                    if (sub.length() > 8) sub = sub.substring(0, 8);
                    spec.bdSubPrefix = sub;
                    int rem = 8 - sub.length();
                    long maxVal = rem > 0 ? (WordlistGenerator.pow10(rem) - 1) : 0L;
                    spec.from = Math.max(0L, Math.min(longOf(bdFrom, 0L), maxVal));
                    spec.to = Math.max(spec.from, Math.min(longOf(bdTo, maxVal), maxVal));
                    break;
                }
                case BD_NAME: {
                    spec.mode = WordlistGenerator.Mode.BD_NAME;
                    spec.limit = Long.MAX_VALUE;
                    spec.bdUseAllNames = bdUseAllNames[0];
                    spec.bdCustomNames = text(bdNameCustom);
                    spec.bdCapLower = (capState[0] == 0 || capState[0] == 2);
                    spec.bdCapFirst = (capState[0] == 1 || capState[0] == 2);
                    spec.bdCapUpper = (capState[0] == 3);
                    spec.bdNameNumMode = nameNumMode[0];
                    spec.bdNumDigits = Math.max(1, Math.min(8, intOf(bdNameDigits, 5)));
                    spec.bdRandomPerName = Math.max(1, intOf(bdNameRandomCount, 500));
                    spec.bdMin8Chars = bdNameMin8.isChecked();
                    break;
                }
                case NUMERIC:
                    spec.mode = WordlistGenerator.Mode.NUMERIC;
                    spec.from = longOf(fromField, 0);
                    spec.to = longOf(toField, 9999);
                    spec.pad = intOf(padField, 4);
                    break;
                case MUTATE:
                    spec.mode = WordlistGenerator.Mode.MUTATE;
                    spec.source = mutateSource;
                    spec.mutCapitalize = mutCap.isChecked();
                    spec.mutUpper = mutUpper.isChecked();
                    spec.mutLeet = mutLeet.isChecked();
                    spec.mutReverse = mutRev.isChecked();
                    spec.mutAppendDigits = mutDigits.isChecked();
                    spec.mutAppendYears = mutYears.isChecked();
                    spec.prefix = text(prefixField);
                    spec.suffix = text(suffixField);
                    break;
                default:
                    break;
            }
            return spec;
        };

        final Runnable refreshEstimate = () -> {
            if (tab[0] == Tab.PRESETS) return;
            try {
                WordlistGenerator.Spec s = specOf.call();
                long n = WordlistGenerator.estimate(s);
                estimate.setText(n < 0
                        ? context.getString(R.string.wl_gen_estimate_unknown)
                        : context.getString(R.string.wl_gen_estimate, String.format(Locale.US, "%,d", n)));
                boolean capped = s.mode != WordlistGenerator.Mode.BD_SIM
                        && s.mode != WordlistGenerator.Mode.BD_NAME
                        && n >= WordlistGenerator.DEFAULT_LIMIT;
                estimate.setTextColor(capped ? 0xFFEF6C00 : 0xFF757575);
                if (capped) {
                    estimate.setText(context.getString(R.string.wl_gen_estimate_capped,
                            String.format(Locale.US, "%,d", WordlistGenerator.DEFAULT_LIMIT)));
                }
            } catch (Exception e) {
                estimate.setText("");
            }
        };

        final Runnable updateBdPreview = () -> {
            try {
                List<String> selected = selectedBdPrefixes.call();
                String sub = WordlistGenerator.digitsOnly(text(bdSubPrefix));
                if (sub.length() > 8) sub = sub.substring(0, 8);
                int rem = 8 - sub.length();
                String firstPfx = selected.get(0);
                String sampleTail = "97457441".substring(sub.length());
                String sample = firstPfx + sub + sampleTail;
                StringBuilder pfxList = new StringBuilder();
                for (int i = 0; i < selected.size(); i++) {
                    if (i > 0) pfxList.append(", ");
                    pfxList.append(selected.get(i));
                }
                if (rem > 0) {
                    bdPreview.setText(String.format(Locale.US,
                            "11 Digits · [%s]%s + %d digits (%s–%s) · e.g. %s",
                            pfxList, sub.isEmpty() ? "" : "+" + sub, rem,
                            repeat('0', rem), repeat('9', rem), sample));
                } else {
                    bdPreview.setText(String.format(Locale.US,
                            "11 Digits · [%s] + %s · e.g. %s",
                            pfxList, sub, sample));
                }
                String currentName = text(nameField);
                String autoName = bdAutoFileName.call();
                if (currentName.isEmpty() || currentName.equals(lastAutoName[0])) {
                    nameField.setText(autoName);
                    lastAutoName[0] = autoName;
                }
            } catch (Exception ignored) {
            }
            refreshEstimate.run();
        };

        final Runnable updateBdNamePreview = () -> {
            try {
                String rawCustom = text(bdNameCustom).replaceAll("[^A-Za-z]", "").toLowerCase(Locale.US);
                String sampleBase = rawCustom.isEmpty() ? "sojib" : rawCustom;
                int digits = Math.max(1, Math.min(8, intOf(bdNameDigits, 5)));
                boolean min8 = bdNameMin8.isChecked() || wpaChip.isChecked();
                if (min8 && sampleBase.length() + digits < 8) {
                    digits = 8 - sampleBase.length();
                }
                String numSample = "12345678".substring(0, Math.min(8, digits));
                String rndSample = "48291736".substring(0, Math.min(8, digits));
                String capSample = Character.toUpperCase(sampleBase.charAt(0)) + sampleBase.substring(1);
                String ex;
                if (capState[0] == 0) {
                    ex = sampleBase + numSample + ", " + sampleBase + rndSample;
                } else if (capState[0] == 1) {
                    ex = capSample + numSample + ", " + capSample + rndSample;
                } else if (capState[0] == 3) {
                    String up = sampleBase.toUpperCase(Locale.US);
                    ex = up + numSample + ", " + up + rndSample;
                } else {
                    ex = sampleBase + numSample + ", " + capSample + numSample + ", " + sampleBase + rndSample;
                }
                bdNamePreview.setText(String.format(Locale.US,
                        "%s · e.g. %s",
                        min8 ? "Min 8 chars (Wi-Fi)" : "Any length",
                        ex));
                bdNameRandomWrap.setVisibility(
                        nameNumMode[0] == WordlistGenerator.BdNameNumMode.SEQUENTIAL_ALL
                                ? View.GONE : View.VISIBLE);
                String currentName = text(nameField);
                String autoName = bdNameAutoFileName.call();
                if (currentName.isEmpty() || currentName.equals(lastAutoName[0])) {
                    nameField.setText(autoName);
                    lastAutoName[0] = autoName;
                }
            } catch (Exception ignored) {
            }
            refreshEstimate.run();
        };

        srcAllChip.setOnClickListener(v -> {
            bdUseAllNames[0] = true;
            srcAllChip.setChecked(true);
            srcCustomChip.setChecked(false);
            updateBdNamePreview.run();
        });
        srcCustomChip.setOnClickListener(v -> {
            bdUseAllNames[0] = false;
            srcAllChip.setChecked(false);
            srcCustomChip.setChecked(true);
            updateBdNamePreview.run();
        });

        View.OnClickListener capClick = v -> {
            if (v == capOffChip) capState[0] = 0;
            else if (v == capOnChip) capState[0] = 1;
            else if (v == capUpperChip) capState[0] = 3;
            else capState[0] = 2;
            capOffChip.setChecked(capState[0] == 0);
            capOnChip.setChecked(capState[0] == 1);
            capBothChip.setChecked(capState[0] == 2);
            capUpperChip.setChecked(capState[0] == 3);
            updateBdNamePreview.run();
        };
        capOffChip.setOnClickListener(capClick);
        capOnChip.setOnClickListener(capClick);
        capBothChip.setOnClickListener(capClick);
        capUpperChip.setOnClickListener(capClick);

        View.OnClickListener numModeClick = v -> {
            if (v == numRandomChip) nameNumMode[0] = WordlistGenerator.BdNameNumMode.RANDOM_ONLY;
            else if (v == numSeqChip) nameNumMode[0] = WordlistGenerator.BdNameNumMode.SEQUENTIAL_ALL;
            else nameNumMode[0] = WordlistGenerator.BdNameNumMode.SMART_AND_RANDOM;
            numSmartChip.setChecked(nameNumMode[0] == WordlistGenerator.BdNameNumMode.SMART_AND_RANDOM);
            numRandomChip.setChecked(nameNumMode[0] == WordlistGenerator.BdNameNumMode.RANDOM_ONLY);
            numSeqChip.setChecked(nameNumMode[0] == WordlistGenerator.BdNameNumMode.SEQUENTIAL_ALL);
            updateBdNamePreview.run();
        };
        numSmartChip.setOnClickListener(numModeClick);
        numRandomChip.setOnClickListener(numModeClick);
        numSeqChip.setOnClickListener(numModeClick);
        bdNameMin8.setOnClickListener(v -> updateBdNamePreview.run());
        wpaChip.setOnClickListener(v -> {
            if (tab[0] == Tab.BD_NAME) updateBdNamePreview.run();
            else refreshEstimate.run();
        });

        final boolean[] syncingBd = { false };
        for (int i = 0; i < bdOperators.size(); i++) {
            final WordlistGenerator.BdOperator op = bdOperators.get(i);
            Chip chip = toggle(context, bdOperatorChips, op.title, i == 0);
            opChipViews.add(chip);
            chip.setOnClickListener(v -> {
                syncingBd[0] = true;
                for (Chip oc : opChipViews) oc.setChecked(oc == chip);
                List<String> opPfxs = Arrays.asList(op.prefixes);
                for (Chip pc : pfxChipViews) {
                    pc.setChecked(opPfxs.contains(String.valueOf(pc.getTag())));
                }
                syncingBd[0] = false;
                updateBdPreview.run();
            });
        }

        for (Chip pc : pfxChipViews) {
            pc.setOnClickListener(v -> {
                if (syncingBd[0]) return;
                boolean any = false;
                for (Chip c : pfxChipViews) if (c.isChecked()) { any = true; break; }
                if (!any) pc.setChecked(true);
                try {
                    List<String> sel = selectedBdPrefixes.call();
                    for (int i = 0; i < bdOperators.size(); i++) {
                        boolean match = Arrays.asList(bdOperators.get(i).prefixes).equals(sel);
                        opChipViews.get(i).setChecked(match);
                    }
                } catch (Exception ignored) {
                }
                updateBdPreview.run();
            });
        }

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refreshEstimate.run(); }
        };
        fromField.addTextChangedListener(watcher);
        toField.addTextChangedListener(watcher);
        padField.addTextChangedListener(watcher);
        bdFrom.addTextChangedListener(watcher);
        bdTo.addTextChangedListener(watcher);
        bdSubPrefix.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String sub = WordlistGenerator.digitsOnly(s == null ? "" : s.toString());
                if (sub.length() > 8) sub = sub.substring(0, 8);
                int rem = 8 - sub.length();
                bdFrom.setText(rem > 0 ? repeat('0', rem) : "0");
                bdTo.setText(rem > 0 ? repeat('9', rem) : "0");
                updateBdPreview.run();
            }
        });

        TextWatcher bdNameWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { updateBdNamePreview.run(); }
        };
        bdNameCustom.addTextChangedListener(bdNameWatcher);
        bdNameDigits.addTextChangedListener(bdNameWatcher);
        bdNameRandomCount.addTextChangedListener(bdNameWatcher);

        final Runnable showTab = () -> {
            blockPresets.setVisibility(tab[0] == Tab.PRESETS ? View.VISIBLE : View.GONE);
            blockBd.setVisibility(tab[0] == Tab.BD_SIM ? View.VISIBLE : View.GONE);
            blockBdName.setVisibility(tab[0] == Tab.BD_NAME ? View.VISIBLE : View.GONE);
            blockNumeric.setVisibility(tab[0] == Tab.NUMERIC ? View.VISIBLE : View.GONE);
            blockMutate.setVisibility(tab[0] == Tab.MUTATE ? View.VISIBLE : View.GONE);
            boolean rule = tab[0] != Tab.PRESETS;
            blockOutput.setVisibility(rule ? View.VISIBLE : View.GONE);
            run.setVisibility(rule ? View.VISIBLE : View.GONE);
            if (tab[0] == Tab.BD_SIM) {
                updateBdPreview.run();
            } else if (tab[0] == Tab.BD_NAME) {
                updateBdNamePreview.run();
            } else {
                refreshEstimate.run();
            }
        };

        final Chip[] bdSimTabChip = { null };
        final Chip[] bdNameTabChip = { null };
        if (mutateSource != null) {
            title.setText(R.string.wl_mut_title);
            status.setText(context.getString(R.string.wl_mut_subtitle, mutateSource.getName()));
            modeScroll.setVisibility(View.GONE);
            category[0] = mutateSource.category;
            nameField.setText(mutateSource.getDisplayName() + "-mutated.txt");
        } else {
            addTab(context, modes, context.getString(R.string.wl_gen_tab_presets), true, () -> {
                tab[0] = Tab.PRESETS;
                showTab.run();
            });
            bdSimTabChip[0] = addTab(context, modes, context.getString(R.string.wl_gen_tab_bd_sim), false, () -> {
                tab[0] = Tab.BD_SIM;
                showTab.run();
            });
            bdNameTabChip[0] = addTab(context, modes, context.getString(R.string.wl_gen_tab_bd_name), false, () -> {
                tab[0] = Tab.BD_NAME;
                showTab.run();
            });
            addTab(context, modes, context.getString(R.string.wl_gen_tab_numeric), false, () -> {
                tab[0] = Tab.NUMERIC;
                showTab.run();
            });
        }

        {
            View nameRow = LayoutInflater.from(context)
                    .inflate(R.layout.item_wordlist_offer, blockPresets, false);
            ImageView icon = nameRow.findViewById(R.id.wloffer_icon);
            MaterialTextView rowTitle = nameRow.findViewById(R.id.wloffer_title);
            MaterialTextView rowDesc = nameRow.findViewById(R.id.wloffer_desc);
            MaterialTextView rowSize = nameRow.findViewById(R.id.wloffer_size);
            ImageView action = nameRow.findViewById(R.id.wloffer_action);

            icon.setImageResource(R.drawable.name);
            icon.setColorFilter(0xFF1565C0);
            rowTitle.setText("BD · Advance Name + Number");
            rowDesc.setText("All Bangladeshi names + random/sequential numbers (e.g. sojib12345, Sojib12345) · Min 8 chars");
            rowSize.setText("8+ chars");
            action.setColorFilter(0xFF1565C0);

            nameRow.setOnClickListener(v -> {
                if (bdNameTabChip[0] != null) {
                    bdNameTabChip[0].setChecked(true);
                }
                tab[0] = Tab.BD_NAME;
                showTab.run();
            });
            blockPresets.addView(nameRow);
        }

        for (int i = 0; i < bdOperators.size(); i++) {
            final int opIndex = i;
            final WordlistGenerator.BdOperator op = bdOperators.get(i);
            View row = LayoutInflater.from(context)
                    .inflate(R.layout.item_wordlist_offer, blockPresets, false);
            ImageView icon = row.findViewById(R.id.wloffer_icon);
            MaterialTextView rowTitle = row.findViewById(R.id.wloffer_title);
            MaterialTextView rowDesc = row.findViewById(R.id.wloffer_desc);
            MaterialTextView rowSize = row.findViewById(R.id.wloffer_size);
            ImageView action = row.findViewById(R.id.wloffer_action);

            icon.setImageResource(R.drawable.ic_phone);
            icon.setColorFilter(0xFF00897B);
            rowTitle.setText("BD · " + op.title);
            rowDesc.setText(op.description);
            rowSize.setText("11 digits");
            action.setColorFilter(0xFF00897B);

            row.setOnClickListener(v -> {
                if (opIndex < opChipViews.size()) {
                    opChipViews.get(opIndex).performClick();
                }
                if (bdSimTabChip[0] != null) {
                    bdSimTabChip[0].setChecked(true);
                }
                tab[0] = Tab.BD_SIM;
                showTab.run();
            });
            blockPresets.addView(row);
        }

        for (WordlistGenerator.Preset preset : WordlistGenerator.presets()) {
            View row = LayoutInflater.from(context)
                    .inflate(R.layout.item_wordlist_offer, blockPresets, false);
            ImageView icon = row.findViewById(R.id.wloffer_icon);
            MaterialTextView rowTitle = row.findViewById(R.id.wloffer_title);
            MaterialTextView rowDesc = row.findViewById(R.id.wloffer_desc);
            MaterialTextView rowSize = row.findViewById(R.id.wloffer_size);
            ImageView action = row.findViewById(R.id.wloffer_action);

            icon.setImageResource(preset.category.iconRes);
            icon.setColorFilter(preset.category.color);
            rowTitle.setText(preset.title);
            rowDesc.setText(preset.description);
            rowSize.setText(String.format(Locale.US, "%,d", preset.approxLines));
            action.setColorFilter(preset.category.color);

            row.setOnClickListener(v -> {
                action.setEnabled(false);
                new Thread(() -> {
                    Wordlist made = WordlistGenerator.writePreset(store, preset);
                    activity.runOnUiThread(() -> {
                        action.setEnabled(true);
                        if (made == null) {
                            Toast.makeText(context, R.string.wl_gen_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        action.setImageResource(R.drawable.done);
                        action.setColorFilter(0xFF2E7D32);
                        Toast.makeText(context,
                                context.getString(R.string.wl_gen_written, made.getName()),
                                Toast.LENGTH_SHORT).show();
                        if (onChanged != null) onChanged.run();
                    });
                }, "wordlist-preset").start();
            });
            blockPresets.addView(row);
        }

        showTab.run();

        run.setOnClickListener(v -> {
            if (running[0]) return;
            WordlistGenerator.Spec spec;
            try {
                spec = specOf.call();
            } catch (Exception e) {
                return;
            }
            if (spec.mode == WordlistGenerator.Mode.MUTATE && spec.source == null) return;

            running[0] = true;
            cancelled.set(false);
            run.setEnabled(false);
            run.setAlpha(0.6f);
            cancel.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);

            new Thread(() -> {
                Wordlist made = WordlistGenerator.generate(store, spec, new WordlistGenerator.Progress() {
                    @Override
                    public void onProgress(long produced, long estimatedTotal) {
                        activity.runOnUiThread(() -> {
                            if (estimatedTotal > 0) {
                                progress.setIndeterminate(false);
                                progress.setMax(100);
                                progress.setProgress((int) Math.min(100,
                                        produced * 100 / Math.max(1, estimatedTotal)), true);
                            }
                            status.setText(context.getString(R.string.wl_gen_progress,
                                    String.format(Locale.US, "%,d", produced)));
                        });
                    }

                    @Override
                    public boolean isCancelled() {
                        return cancelled.get();
                    }
                });

                activity.runOnUiThread(() -> {
                    running[0] = false;
                    run.setEnabled(true);
                    run.setAlpha(1f);
                    cancel.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                    if (made == null) {
                        status.setText(R.string.wl_gen_cancelled);
                        return;
                    }
                    status.setText(context.getString(R.string.wl_gen_written, made.getName()));
                    Toast.makeText(context, context.getString(R.string.wl_gen_written, made.getName()),
                            Toast.LENGTH_SHORT).show();
                    if (onChanged != null) onChanged.run();
                });
            }, "wordlist-generate").start();
        });

        cancel.setOnClickListener(v -> cancelled.set(true));
        close.setOnClickListener(v -> {
            cancelled.set(true);
            dialog.dismiss();
        });

        dialog.show();
    }

    private static String defaultName(Tab tab) {
        switch (tab) {
            case BD_SIM: return "bd-sim-numbers.txt";
            case BD_NAME: return "bd-advance-names-pass.txt";
            case NUMERIC: return "numeric.txt";
            case MUTATE: return "mutated.txt";
            default: return "generated.txt";
        }
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(Math.max(0, count));
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    private static String dedupeChars(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (sb.indexOf(String.valueOf(c)) < 0) sb.append(c);
        }
        return sb.toString();
    }

    private static Chip toggle(Context context, ChipGroup group, String text, boolean checked) {
        Chip chip = new Chip(context);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setTextSize(11f);
        chip.setChipMinHeight(dp(context, 30));
        group.addView(chip);
        return chip;
    }

    private static Chip addTab(Context context, ChipGroup group, String text,
                               boolean checked, Runnable onSelected) {
        Chip chip = new Chip(context);
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setTextSize(11f);
        chip.setChipMinHeight(dp(context, 30));
        chip.setOnClickListener(v -> onSelected.run());
        group.addView(chip);
        return chip;
    }

    private static float dp(Context context, int value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    private static String text(TextInputEditText field) {
        CharSequence cs = field.getText();
        return cs == null ? "" : cs.toString().trim();
    }

    private static int intOf(TextInputEditText field, int fallback) {
        try {
            return Integer.parseInt(text(field));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long longOf(TextInputEditText field, long fallback) {
        try {
            return Long.parseLong(text(field));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
