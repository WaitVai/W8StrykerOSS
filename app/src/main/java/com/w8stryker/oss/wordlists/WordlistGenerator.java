package com.w8stryker.oss.wordlists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class WordlistGenerator {

    public interface Progress {
        void onProgress(long produced, long estimatedTotal);
        boolean isCancelled();
    }

    public static final long DEFAULT_LIMIT = 5_000_000L;

    public static class Preset {
        public final String id;
        public final String title;
        public final String description;
        public final WordlistCategory category;
        public final String fileName;
        public final long approxLines;

        Preset(String id, String title, String description, WordlistCategory category,
               String fileName, long approxLines) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.fileName = fileName;
            this.approxLines = approxLines;
        }
    }

    public static List<Preset> presets() {
        return Arrays.asList(
                new Preset("top10", "Top 10 passwords",
                        "The ten most common passwords in every published dump. Seconds to run.",
                        WordlistCategory.PASSWORD, "top-10-passwords.txt", 10),
                new Preset("top250", "Top 250 passwords",
                        "Ranked most-common first. The sensible first attempt before anything larger.",
                        WordlistCategory.PASSWORD, "top-250-passwords.txt", 250),
                new Preset("pins20", "Top 20 PINs",
                        "The ranked head of the PIN distribution — 1234, 1111, 0000 and the rest.",
                        WordlistCategory.PIN, "top-20-pins.txt", 20),
                new Preset("pins250", "Top 250 PINs",
                        "Ranked head plus dates, runs and repeated pairs.",
                        WordlistCategory.PIN, "top-250-pins.txt", 250),
                new Preset("pins4all", "All 4-digit PINs",
                        "All 10 000, ordered by how often each actually turns up.",
                        WordlistCategory.PIN, "all-4-digit-pins.txt", 10000),
                new Preset("wifi", "Wi-Fi defaults",
                        "Router and hotspot defaults that satisfy the 8-character WPA minimum.",
                        WordlistCategory.WIFI, "wifi-defaults.txt", 35),
                new Preset("users", "Common usernames",
                        "Service and login names, ranked.",
                        WordlistCategory.USERNAME, "common-usernames.txt", 35),
                new Preset("walks", "Keyboard walks",
                        "Patterns a charset rule never produces and everyone types anyway.",
                        WordlistCategory.PASSWORD, "keyboard-walks.txt", 30)
        );
    }

    public static Preset presetById(String id) {
        for (Preset p : presets()) if (p.id.equals(id)) return p;
        return null;
    }

    public static Wordlist writePreset(WordlistStore store, Preset preset) {
        List<String> lines;
        switch (preset.id) {
            case "top10":   lines = Arrays.asList(BuiltinLists.TOP_10_PASSWORDS); break;
            case "top250":  lines = Arrays.asList(BuiltinLists.TOP_250_PASSWORDS); break;
            case "pins20":  lines = Arrays.asList(BuiltinLists.TOP_20_PINS); break;
            case "pins250": lines = BuiltinLists.top250Pins(); break;
            case "pins4all":lines = BuiltinLists.allFourDigitPins(); break;
            case "wifi":    lines = Arrays.asList(BuiltinLists.WIFI_DEFAULTS); break;
            case "users":   lines = Arrays.asList(BuiltinLists.COMMON_USERNAMES); break;
            case "walks":   lines = BuiltinLists.keyboardWalks(); break;
            default: return null;
        }
        Wordlist wl = store.create(preset.fileName, preset.category, Wordlist.Origin.GENERATED);
        wl.note = preset.title;
        long n = 0;
        try (BufferedWriter w = store.writer(wl, false)) {
            for (String line : lines) {
                w.write(line);
                w.write('\n');
                n++;
            }
        } catch (IOException e) {
            wl.file.delete();
            return null;
        }
        store.finish(wl, n);
        return wl;
    }

    public enum Mode { CHARSET, MASK, NUMERIC, MUTATE, BD_SIM, BD_NAME }

    public enum BdNameNumMode { SMART_AND_RANDOM, RANDOM_ONLY, SEQUENTIAL_ALL }

    public static final String[] ALL_BD_PREFIXES = {
            "013", "014", "015", "016", "017", "018", "019"
    };

    public static class BdOperator {
        public final String id;
        public final String title;
        public final String description;
        public final String defaultFileName;
        public final String[] prefixes;

        public BdOperator(String id, String title, String description,
                          String defaultFileName, String... prefixes) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.defaultFileName = defaultFileName;
            this.prefixes = prefixes;
        }
    }

    public static List<BdOperator> bdOperators() {
        return Arrays.asList(
                new BdOperator("all", "All SIM (013–019)",
                        "All Bangladeshi SIM combinations (013, 014, 015, 016, 017, 018, 019) · 11 digits",
                        "bd-all-sim-013-019.txt", ALL_BD_PREFIXES),
                new BdOperator("bl019", "Banglalink (019)",
                        "Banglalink 019 + 8 digits (e.g. 01997457441) · 11 digits",
                        "bd-banglalink-019.txt", "019"),
                new BdOperator("gp017", "Grameenphone (017)",
                        "Grameenphone 017 + 8 digits (e.g. 01797457441) · 11 digits",
                        "bd-grameenphone-017.txt", "017"),
                new BdOperator("robi018", "Robi (018)",
                        "Robi 018 + 8 digits (e.g. 01897457441) · 11 digits",
                        "bd-robi-018.txt", "018"),
                new BdOperator("airtel016", "Airtel (016)",
                        "Airtel 016 + 8 digits (e.g. 01697457441) · 11 digits",
                        "bd-airtel-016.txt", "016"),
                new BdOperator("teletalk015", "Teletalk (015)",
                        "Teletalk 015 + 8 digits (e.g. 01597457441) · 11 digits",
                        "bd-teletalk-015.txt", "015"),
                new BdOperator("gp013", "Grameenphone (013)",
                        "Grameenphone 013 + 8 digits (e.g. 01397457441) · 11 digits",
                        "bd-grameenphone-013.txt", "013"),
                new BdOperator("bl014", "Banglalink (014)",
                        "Banglalink 014 + 8 digits (e.g. 01497457441) · 11 digits",
                        "bd-banglalink-014.txt", "014"),
                new BdOperator("bl_all", "Banglalink All (019, 014)",
                        "All Banglalink SIM prefixes (019 & 014) + 8 digits · 11 digits",
                        "bd-banglalink-019-014.txt", "019", "014"),
                new BdOperator("gp_all", "Grameenphone All (017, 013)",
                        "All Grameenphone SIM prefixes (017 & 013) + 8 digits · 11 digits",
                        "bd-grameenphone-017-013.txt", "017", "013")
        );
    }

    public static final String DIGITS = "0123456789";
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String SPECIAL = "!@#$%^&*()-_=+.,?";

    public static class Spec {
        public Mode mode = Mode.CHARSET;
        public String name = "generated.txt";
        public WordlistCategory category = WordlistCategory.PASSWORD;
        public long limit = DEFAULT_LIMIT;

        public String charset = DIGITS;
        public int minLength = 4;
        public int maxLength = 4;

        public String mask = "?d?d?d?d";

        public long from = 0;
        public long to = 9999;
        public int pad = 4;

        public List<String> bdPrefixes = new ArrayList<>(Arrays.asList(ALL_BD_PREFIXES));
        public String bdSubPrefix = "";

        public boolean bdUseAllNames = true;
        public String bdCustomNames = "";
        public boolean bdCapLower = true;
        public boolean bdCapFirst = true;
        public boolean bdCapUpper = false;
        public BdNameNumMode bdNameNumMode = BdNameNumMode.SMART_AND_RANDOM;
        public int bdNumDigits = 5;
        public int bdRandomPerName = 500;
        public boolean bdMin8Chars = true;

        public Wordlist source;
        public boolean mutCapitalize = true;
        public boolean mutUpper = false;
        public boolean mutLeet = false;
        public boolean mutReverse = false;
        public boolean mutAppendDigits = false;
        public boolean mutAppendYears = false;
        public String prefix = "";
        public String suffix = "";

        public boolean wpaLengthOnly = true;
    }

    public static long estimate(Spec spec) {
        switch (spec.mode) {
            case CHARSET: {
                if (spec.charset == null || spec.charset.isEmpty()) return 0;
                BigInteger base = BigInteger.valueOf(spec.charset.length());
                BigInteger total = BigInteger.ZERO;
                for (int len = spec.minLength; len <= spec.maxLength; len++) {
                    total = total.add(base.pow(len));
                    if (total.bitLength() > 62) return spec.limit;
                }
                long t = total.longValue();
                return Math.min(t, spec.limit);
            }
            case MASK: {
                List<String> slots = parseMask(spec.mask);
                BigInteger total = BigInteger.ONE;
                for (String slot : slots) {
                    total = total.multiply(BigInteger.valueOf(slot.length()));
                    if (total.bitLength() > 62) return spec.limit;
                }
                return Math.min(total.longValue(), spec.limit);
            }
            case NUMERIC:
                return Math.min(Math.max(0, spec.to - spec.from + 1), spec.limit);
            case BD_SIM: {
                List<String> prefixes = (spec.bdPrefixes == null || spec.bdPrefixes.isEmpty())
                        ? Arrays.asList(ALL_BD_PREFIXES)
                        : spec.bdPrefixes;
                int validPrefixes = 0;
                for (String pfx : prefixes) {
                    if (digitsOnly(pfx).length() == 3) validPrefixes++;
                }
                if (validPrefixes == 0) return 0;
                String sub = digitsOnly(spec.bdSubPrefix);
                if (sub.length() > 8) sub = sub.substring(0, 8);
                int remDigits = 8 - sub.length();
                if (remDigits == 0) return Math.min(validPrefixes, spec.limit);
                long maxVal = pow10(remDigits) - 1;
                long start = Math.max(0L, Math.min(spec.from, maxVal));
                long end = Math.max(start, Math.min(spec.to, maxVal));
                long perPrefix = end - start + 1L;
                BigInteger total = BigInteger.valueOf(validPrefixes)
                        .multiply(BigInteger.valueOf(perPrefix));
                if (total.bitLength() > 62) return spec.limit;
                return Math.min(total.longValue(), spec.limit);
            }
            case BD_NAME: {
                List<String> names = resolveBdNames(spec);
                int caseCount = (spec.bdCapLower ? 1 : 0)
                        + (spec.bdCapFirst ? 1 : 0)
                        + (spec.bdCapUpper ? 1 : 0);
                if (caseCount == 0) caseCount = 1;
                long totalVariants = (long) names.size() * caseCount;
                int digits = Math.max(1, Math.min(8, spec.bdNumDigits));
                long perName;
                switch (spec.bdNameNumMode) {
                    case RANDOM_ONLY:
                        perName = Math.max(1, spec.bdRandomPerName) + 1L;
                        break;
                    case SEQUENTIAL_ALL:
                        perName = pow10(digits);
                        break;
                    case SMART_AND_RANDOM:
                    default:
                        perName = BuiltinLists.BD_NAME_COMMON_NUMBERS.length
                                + 57L + Math.max(0, spec.bdRandomPerName);
                        break;
                }
                BigInteger total = BigInteger.valueOf(totalVariants)
                        .multiply(BigInteger.valueOf(perName));
                if (total.bitLength() > 62) return spec.limit;
                return Math.min(total.longValue(), spec.limit);
            }
            default:
                return -1;
        }
    }

    public static Wordlist generate(WordlistStore store, Spec spec, Progress progress) {
        Wordlist wl = store.create(spec.name, spec.category, Wordlist.Origin.GENERATED);
        long estimated = estimate(spec);
        long written = 0;
        try (BufferedWriter w = store.writer(wl, false)) {
            switch (spec.mode) {
                case CHARSET: written = charset(w, spec, progress, estimated); break;
                case MASK:    written = mask(w, spec, progress, estimated); break;
                case NUMERIC: written = numeric(w, spec, progress, estimated); break;
                case MUTATE:  written = mutate(w, spec, progress); break;
                case BD_SIM:  written = bdSim(w, spec, progress, estimated); break;
                case BD_NAME: written = bdName(w, spec, progress, estimated); break;
            }
        } catch (IOException e) {
            wl.file.delete();
            return null;
        }
        if (progress != null && progress.isCancelled() && written == 0) {
            wl.file.delete();
            return null;
        }
        store.finish(wl, written);
        return wl;
    }

    public static List<String> resolveBdNames(Spec spec) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (spec.bdCustomNames != null && !spec.bdCustomNames.trim().isEmpty()) {
            String[] parts = spec.bdCustomNames.split("[,\\s;]+");
            for (String part : parts) {
                String cleaned = part.trim().replaceAll("[^A-Za-z]", "").toLowerCase(Locale.US);
                if (!cleaned.isEmpty()) set.add(cleaned);
            }
        }
        if (spec.bdUseAllNames || set.isEmpty()) {
            for (String n : BuiltinLists.BD_NAMES) {
                set.add(n.toLowerCase(Locale.US));
            }
        }
        return new ArrayList<>(set);
    }

    public static List<String> caseVariantsOf(String lowerName, Spec spec) {
        LinkedHashSet<String> vars = new LinkedHashSet<>();
        if (lowerName == null || lowerName.isEmpty()) return new ArrayList<>();
        String low = lowerName.toLowerCase(Locale.US);
        String cap = Character.toUpperCase(low.charAt(0)) + low.substring(1);
        String up = low.toUpperCase(Locale.US);
        if (spec.bdCapLower) vars.add(low);
        if (spec.bdCapFirst) vars.add(cap);
        if (spec.bdCapUpper) vars.add(up);
        if (vars.isEmpty()) vars.add(low);
        return new ArrayList<>(vars);
    }

    private static String ensureMinDigits(String num, int minDigits) {
        if (num.length() >= minDigits) return num;
        StringBuilder sb = new StringBuilder(minDigits);
        sb.append(num);
        char next = '1';
        while (sb.length() < minDigits) {
            sb.append(next);
            next = (next == '9') ? '0' : (char) (next + 1);
        }
        return sb.toString();
    }

    private static long bdName(BufferedWriter w, Spec spec, Progress p, long estimated)
            throws IOException {
        List<String> baseNames = resolveBdNames(spec);
        boolean min8 = spec.bdMin8Chars || spec.wpaLengthOnly;
        int requestedDigits = Math.max(1, Math.min(8, spec.bdNumDigits));
        int randomCount = Math.max(0, spec.bdRandomPerName);
        java.util.Random rng = new java.util.Random();
        long written = 0;
        long lastReportNs = System.nanoTime();

        for (String baseName : baseNames) {
            for (String name : caseVariantsOf(baseName, spec)) {
                int minDigits = min8 ? Math.max(1, 8 - name.length()) : 1;
                int digits = Math.max(minDigits, requestedDigits);
                if (digits > 8) digits = 8;

                if (spec.bdNameNumMode == BdNameNumMode.SEQUENTIAL_ALL) {
                    long maxVal = pow10(digits) - 1;
                    int nameLen = name.length();
                    int totalLen = nameLen + digits;
                    char[] buf = new char[totalLen + 1];
                    for (int i = 0; i < nameLen; i++) buf[i] = name.charAt(i);
                    for (int i = nameLen; i < totalLen; i++) buf[i] = '0';
                    buf[totalLen] = '\n';

                    for (long v = 0; v <= maxVal; v++) {
                        if ((written & 0x3FFF) == 0 && cancelled(p)) return written;
                        if (written >= spec.limit) return written;
                        w.write(buf, 0, totalLen + 1);
                        written++;
                        if (p != null && (written & 0xFFFF) == 0) {
                            long now = System.nanoTime();
                            if (now - lastReportNs >= 80_000_000L) {
                                lastReportNs = now;
                                p.onProgress(written, estimated);
                            }
                        }
                        for (int pos = totalLen - 1; pos >= nameLen; pos--) {
                            if (buf[pos] < '9') {
                                buf[pos]++;
                                break;
                            }
                            buf[pos] = '0';
                        }
                    }
                    continue;
                }

                LinkedHashSet<String> suffixes = new LinkedHashSet<>();
                String classic12345 = ensureMinDigits("12345".substring(0, Math.min(5, digits)), digits);
                suffixes.add(classic12345);

                if (spec.bdNameNumMode == BdNameNumMode.SMART_AND_RANDOM) {
                    for (String common : BuiltinLists.BD_NAME_COMMON_NUMBERS) {
                        suffixes.add(ensureMinDigits(common, minDigits));
                    }
                    for (int y = 2026; y >= 1970; y--) {
                        suffixes.add(ensureMinDigits(String.valueOf(y), minDigits));
                    }
                }

                int maxUnique = (int) Math.min(1_000_000L, pow10(digits));
                int targetRandom = Math.min(randomCount, maxUnique);
                String fmt = "%0" + digits + "d";
                int bound = (int) pow10(digits);
                int attempts = 0;
                int baseSize = suffixes.size();
                while (suffixes.size() - baseSize < targetRandom && attempts < targetRandom * 3) {
                    attempts++;
                    int r = rng.nextInt(bound);
                    suffixes.add(String.format(Locale.US, fmt, r));
                }

                for (String num : suffixes) {
                    if ((written & 0x3FF) == 0 && cancelled(p)) return written;
                    if (written >= spec.limit) return written;
                    String candidate = name + num;
                    if (min8 && candidate.length() < 8) continue;
                    if (accept(candidate, spec)) {
                        w.write(candidate);
                        w.write('\n');
                        written++;
                        if (p != null && (written & 0xFFF) == 0) {
                            long now = System.nanoTime();
                            if (now - lastReportNs >= 80_000_000L) {
                                lastReportNs = now;
                                p.onProgress(written, estimated);
                            }
                        }
                    }
                }
            }
        }
        if (p != null) p.onProgress(written, estimated);
        return written;
    }

    private static long bdSim(BufferedWriter w, Spec spec, Progress p, long estimated)
            throws IOException {
        List<String> prefixes = (spec.bdPrefixes == null || spec.bdPrefixes.isEmpty())
                ? Arrays.asList(ALL_BD_PREFIXES)
                : spec.bdPrefixes;
        String sub = digitsOnly(spec.bdSubPrefix);
        if (sub.length() > 8) sub = sub.substring(0, 8);
        int subLen = sub.length();
        int remDigits = 8 - subLen;
        long maxVal = pow10(remDigits) - 1;
        long start = Math.max(0L, Math.min(spec.from, maxVal));
        long end = Math.max(start, Math.min(spec.to, maxVal));

        char[] buf = new char[12];
        buf[11] = '\n';
        long written = 0;
        long lastReportNs = System.nanoTime();

        for (String rawPrefix : prefixes) {
            String pfx = digitsOnly(rawPrefix);
            if (pfx.length() != 3) continue;
            buf[0] = pfx.charAt(0);
            buf[1] = pfx.charAt(1);
            buf[2] = pfx.charAt(2);
            for (int i = 0; i < subLen; i++) {
                buf[3 + i] = sub.charAt(i);
            }

            if (remDigits == 0) {
                if (cancelled(p) || written >= spec.limit) return written;
                w.write(buf, 0, 12);
                written++;
                if (p != null) p.onProgress(written, estimated);
                continue;
            }

            long temp = start;
            for (int pos = 10; pos >= 3 + subLen; pos--) {
                buf[pos] = (char) ('0' + (temp % 10));
                temp /= 10;
            }

            for (long v = start; v <= end; v++) {
                if ((written & 0x3FFF) == 0 && cancelled(p)) return written;
                if (written >= spec.limit) return written;
                w.write(buf, 0, 12);
                written++;
                if (p != null && (written & 0xFFFF) == 0) {
                    long now = System.nanoTime();
                    if (now - lastReportNs >= 80_000_000L) {
                        lastReportNs = now;
                        p.onProgress(written, estimated);
                    }
                }
                for (int pos = 10; pos >= 3 + subLen; pos--) {
                    if (buf[pos] < '9') {
                        buf[pos]++;
                        break;
                    }
                    buf[pos] = '0';
                }
            }
        }
        if (p != null) p.onProgress(written, estimated);
        return written;
    }

    public static String digitsOnly(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
        }
        return sb.toString();
    }

    public static long pow10(int exp) {
        long r = 1L;
        for (int i = 0; i < exp; i++) r *= 10L;
        return r;
    }

    private static long charset(BufferedWriter w, Spec spec, Progress p, long estimated)
            throws IOException {
        char[] alphabet = spec.charset.toCharArray();
        long written = 0;
        for (int len = spec.minLength; len <= spec.maxLength; len++) {
            int[] idx = new int[len];
            char[] buf = new char[len];
            while (true) {
                if (cancelled(p) || written >= spec.limit) return written;
                for (int i = 0; i < len; i++) buf[i] = alphabet[idx[i]];
                String candidate = new String(buf);
                if (accept(candidate, spec)) {
                    w.write(candidate);
                    w.write('\n');
                    written++;
                    report(p, written, estimated);
                }
                int pos = len - 1;
                while (pos >= 0 && ++idx[pos] == alphabet.length) {
                    idx[pos] = 0;
                    pos--;
                }
                if (pos < 0) break;
            }
        }
        return written;
    }

    private static long mask(BufferedWriter w, Spec spec, Progress p, long estimated)
            throws IOException {
        List<String> slots = parseMask(spec.mask);
        if (slots.isEmpty()) return 0;
        int len = slots.size();
        int[] idx = new int[len];
        char[] buf = new char[len];
        long written = 0;
        while (true) {
            if (cancelled(p) || written >= spec.limit) return written;
            for (int i = 0; i < len; i++) buf[i] = slots.get(i).charAt(idx[i]);
            String candidate = new String(buf);
            if (accept(candidate, spec)) {
                w.write(candidate);
                w.write('\n');
                written++;
                report(p, written, estimated);
            }
            int pos = len - 1;
            while (pos >= 0 && ++idx[pos] == slots.get(pos).length()) {
                idx[pos] = 0;
                pos--;
            }
            if (pos < 0) return written;
        }
    }

    private static long numeric(BufferedWriter w, Spec spec, Progress p, long estimated)
            throws IOException {
        long written = 0;
        String format = spec.pad > 0 ? "%0" + spec.pad + "d" : "%d";
        for (long v = spec.from; v <= spec.to; v++) {
            if (cancelled(p) || written >= spec.limit) return written;
            String candidate = String.format(Locale.US, format, v);
            if (accept(candidate, spec)) {
                w.write(candidate);
                w.write('\n');
                written++;
                report(p, written, estimated);
            }
        }
        return written;
    }

    private static long mutate(BufferedWriter w, Spec spec, Progress p) throws IOException {
        if (spec.source == null) return 0;
        long written = 0;
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(new FileInputStream(spec.source.file), "UTF-8"), 1 << 16)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (cancelled(p) || written >= spec.limit) return written;
                String base = line.trim();
                if (base.isEmpty()) continue;
                for (String variant : variantsOf(base, spec)) {
                    if (written >= spec.limit) return written;
                    String candidate = spec.prefix + variant + spec.suffix;
                    if (accept(candidate, spec)) {
                        w.write(candidate);
                        w.write('\n');
                        written++;
                        report(p, written, -1);
                    }
                }
            }
        }
        return written;
    }

    private static List<String> variantsOf(String base, Spec spec) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        out.add(base);
        if (spec.mutCapitalize && base.length() > 0) {
            out.add(Character.toUpperCase(base.charAt(0)) + base.substring(1));
        }
        if (spec.mutUpper) out.add(base.toUpperCase(Locale.US));
        if (spec.mutLeet) out.add(leet(base));
        if (spec.mutReverse) out.add(new StringBuilder(base).reverse().toString());
        if (spec.mutAppendDigits) {
            for (int i = 0; i <= 9; i++) out.add(base + i);
            out.add(base + "123");
            out.add(base + "1234");
        }
        if (spec.mutAppendYears) {
            for (int y = 2026; y >= 1990; y--) {
                out.add(base + y);
                out.add(base + String.format(Locale.US, "%02d", y % 100));
            }
        }
        return new ArrayList<>(out);
    }

    private static String leet(String s) {
        return s.replace('a', '4').replace('A', '4')
                .replace('e', '3').replace('E', '3')
                .replace('i', '1').replace('I', '1')
                .replace('o', '0').replace('O', '0')
                .replace('s', '5').replace('S', '5')
                .replace('t', '7').replace('T', '7');
    }

    static List<String> parseMask(String mask) {
        List<String> slots = new ArrayList<>();
        if (mask == null) return slots;
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '?' && i + 1 < mask.length()) {
                char kind = mask.charAt(++i);
                switch (kind) {
                    case 'd': slots.add(DIGITS); break;
                    case 'l': slots.add(LOWER); break;
                    case 'u': slots.add(UPPER); break;
                    case 's': slots.add(SPECIAL); break;
                    case 'a': slots.add(LOWER + UPPER + DIGITS + SPECIAL); break;
                    case '?': slots.add("?"); break;
                    default:  slots.add(String.valueOf(kind)); break;
                }
            } else {
                slots.add(String.valueOf(c));
            }
        }
        return slots;
    }

    private static boolean accept(String candidate, Spec spec) {
        if (!spec.wpaLengthOnly) return true;
        int len = candidate.length();
        return len >= 8 && len <= 63;
    }

    private static boolean cancelled(Progress p) {
        return p != null && p.isCancelled();
    }

    private static void report(Progress p, long written, long estimated) {
        if (p != null && (written & 0x3FF) == 0) p.onProgress(written, estimated);
    }
}
