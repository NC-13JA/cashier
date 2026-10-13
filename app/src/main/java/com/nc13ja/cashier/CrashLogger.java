package com.nc13ja.cashier;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Offline diagnostic logger. Writes to this app's private storage. */
public final class CrashLogger {
    private static final String TAG = "CashierDiagnostics";
    private static final long MAX_BYTES = 1024L * 1024L;
    private static Context appContext;
    private CrashLogger() {}

    public static synchronized void initialize(Context context) {
        appContext = context.getApplicationContext();
        log("APP", "Diagnostic logging initialized");
    }

    public static synchronized void log(String event, String detail) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
        String line = time + " [" + Thread.currentThread().getName() + "] " + event
                + (detail == null || detail.isEmpty() ? "" : " | " + detail);
        Log.i(TAG, line);
        if (appContext == null) return;
        File dir = new File(appContext.getFilesDir(), "diagnostics");
        File file = new File(dir, "cashier.log");
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Cannot create diagnostics directory");
                return;
            }
            if (file.exists() && file.length() > MAX_BYTES) {
                File old = new File(dir, "cashier.log.1");
                if (old.exists()) old.delete();
                if (!file.renameTo(old)) file.delete();
            }
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(line);
                writer.write("\n");
                writer.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not persist diagnostic log", e);
        }
    }

    public static synchronized void crash(String event, Throwable error) {
        StringWriter sw = new StringWriter();
        if (error != null) error.printStackTrace(new PrintWriter(sw));
        log("CRASH: " + event, sw.toString());
    }

    public static synchronized String readLog() {
        if (appContext == null) return "Diagnostic logger is not initialized.";
        File file = new File(new File(appContext.getFilesDir(), "diagnostics"), "cashier.log");
        if (!file.exists()) return "No diagnostic log has been saved yet.";
        StringBuilder out = new StringBuilder();
        File previous = new File(file.getParentFile(), "cashier.log.1");
        if (previous.exists()) appendFile(previous, out);
        appendFile(file, out);
        return out.toString();
    }

    private static void appendFile(File file, StringBuilder out) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append('\n');
        } catch (Exception e) {
            out.append("Could not read log: ").append(e).append('\n');
        }
    }
}
