package com.flxholle.quicktiles.utils;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Keeps a small ring buffer of the last tile callbacks, so that a user can tell (and share) what
 * actually happened when a tile was tapped. This is what makes it possible to distinguish between
 * "the system never even bound our service" and "we were called but something failed", which look
 * exactly the same from the outside: the tile does nothing.
 * <p>
 * Recording is off by default because every tile callback would rewrite the whole buffer. It is
 * switched on from the settings screen, or automatically as soon as something fails - an error or a
 * crash must never be lost, they are the interesting cases. The writes happen on a single background
 * thread, so a tile callback never waits for the disk.
 */
public final class TileDiagnostics {

    public static final String TAG = "QuickTiles";

    /**
     * Preference (default shared preferences) that switches the recording on and off.
     */
    public static final String PREF_RECORDING = "tile_diagnostics_enabled";

    private static final String PREFS = "quicktiles_diagnostics";
    private static final String KEY_LOG = "log";
    private static final int MAX_LINES = 120;

    private static final Object LOCK = new Object();

    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tile-diagnostics");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    /**
     * Guards the "--- app process started ---" marker: it is written once per process, with the
     * first event that is recorded.
     */
    private static boolean processStartPending = true;

    private TileDiagnostics() {
    }

    public static boolean isRecordingEnabled(Context context) {
        try {
            return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                    .getBoolean(PREF_RECORDING, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static void setRecordingEnabled(Context context, boolean enabled) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                    .edit()
                    .putBoolean(PREF_RECORDING, enabled)
                    .apply();
        } catch (Exception e) {
            Log.w(TAG, "Could not change the diagnostics setting", e);
        }
    }

    /**
     * Records an event. Newest entries are at the top. Never throws and never blocks the caller.
     */
    public static void record(Context context, String event) {
        Log.i(TAG, event);
        if (!isRecordingEnabled(context)) {
            return;
        }
        writeLine(context, event);
    }

    /**
     * Records an event together with the exception that caused it and switches the recording on, so
     * a failure can always be looked at afterwards.
     */
    public static void recordError(Context context, String event, Throwable error) {
        setRecordingEnabled(context, true);
        String message = error == null ? ""
                : " (" + error.getClass().getSimpleName()
                + (error.getMessage() == null ? "" : ": " + error.getMessage()) + ")";
        record(context, event + message);
    }

    /**
     * Records a fatal, uncaught exception with its stack trace.
     * <p>
     * A crash is the only way a tile can stay bound to nothing: the process dies while SystemUI is
     * waiting for it, SystemUI gives up on that tile and every later tap on it is silently dropped.
     * Since the crash happens while the app is in the background nobody sees it, so it is written
     * here, where it survives the process death. Never throws.
     */
    public static void recordFatal(Context context, Thread thread, Throwable error) {
        try {
            setRecordingEnabled(context, true);
            record(context, formatFatal(thread, error));
        } catch (Throwable ignored) {
            // A crash handler must never crash
        }
    }

    /**
     * @return the log line that describes an uncaught exception, including the first stack frames.
     */
    public static String formatFatal(Thread thread, Throwable error) {
        String trace = "";
        if (error != null) {
            StackTraceElement[] frames = error.getStackTrace();
            StringBuilder builder = new StringBuilder();
            int count = Math.min(frames.length, 8);
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    builder.append(" | ");
                }
                builder.append(frames[i].toString());
            }
            trace = builder.toString();
        }
        return "FATAL in thread " + (thread == null ? "?" : thread.getName())
                + ": " + (error == null ? "?" : error.getClass().getName()
                + (error.getMessage() == null ? "" : ": " + error.getMessage()))
                + (trace.isEmpty() ? "" : " at " + trace);
    }

    /**
     * Records how and when the previous app process ended.
     * <p>
     * This is the key piece of information for the "tiles only work while the app is open" problem:
     * if the system killed the app when it was swiped out of the recent apps, the next start reports
     * {@code REASON_USER_REQUESTED} (or, on some OEM builds, {@code REASON_OTHER}). A killed but not
     * stopped app is still startable in the background, so its tiles keep working; an app that was
     * force stopped cannot start any component at all until it is launched again. Never throws.
     */
    public static void recordProcessExitHistory(Context context) {
        for (String line : readExitHistory(context)) {
            record(context, line);
        }
    }

    /**
     * @return one description per remembered process exit, newest first (may be empty).
     */
    public static List<String> readExitHistory(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        try {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return lines;
            }
            List<ApplicationExitInfo> exits = activityManager.getHistoricalProcessExitReasons(
                    context.getPackageName(), 0, 3);
            if (exits == null || exits.isEmpty()) {
                lines.add("no previous process exit recorded");
                return lines;
            }
            for (ApplicationExitInfo exit : exits) {
                lines.add("previous process ended " + stamp(exit.getTimestamp())
                        + " reason=" + reasonName(exit.getReason())
                        + " importance=" + exit.getImportance());
            }
        } catch (Throwable error) {
            Log.w(TAG, "Could not read process exit history", error);
        }
        return lines;
    }

    private static String reasonName(int reason) {
        switch (reason) {
            case ApplicationExitInfo.REASON_EXIT_SELF:
                return "EXIT_SELF";
            case ApplicationExitInfo.REASON_SIGNALED:
                return "SIGNALED (killed, e.g. by the task killer or the low memory killer)";
            case ApplicationExitInfo.REASON_LOW_MEMORY:
                return "LOW_MEMORY";
            case ApplicationExitInfo.REASON_CRASH:
                return "CRASH (see AndroidRuntime in logcat)";
            case ApplicationExitInfo.REASON_CRASH_NATIVE:
                return "CRASH_NATIVE";
            case ApplicationExitInfo.REASON_ANR:
                return "ANR";
            case ApplicationExitInfo.REASON_INITIALIZATION_FAILURE:
                return "INITIALIZATION_FAILURE";
            case ApplicationExitInfo.REASON_PERMISSION_CHANGE:
                return "PERMISSION_CHANGE";
            case ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE:
                return "EXCESSIVE_RESOURCE_USAGE";
            case ApplicationExitInfo.REASON_USER_REQUESTED:
                return "USER_REQUESTED (the phone stopped the app, e.g. when it was swiped out of the recent apps)";
            case ApplicationExitInfo.REASON_OTHER:
                return "OTHER (usually a task killer or the OEM battery manager)";
            default:
                return "REASON_" + reason;
        }
    }

    /**
     * @return the recorded events, newest first (may be empty). Pending writes are flushed first.
     */
    public static String read(Context context) {
        try {
            // The writer thread is a single thread queue: waiting for it to drain guarantees that
            // everything that was recorded before this call is on disk.
            WRITER.submit(() -> {
            }).get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // A missing entry is better than a frozen settings screen
        }
        try {
            return prefs(context).getString(KEY_LOG, "");
        } catch (Exception e) {
            Log.w(TAG, "Could not read diagnostics", e);
            return "";
        }
    }

    /**
     * @return the most recent fatal error line, or an empty string.
     */
    public static String lastFatal(Context context) {
        for (String line : read(context).split("\n")) {
            if (line.contains("FATAL in thread ")) {
                return line.trim();
            }
        }
        return "";
    }

    /**
     * Keeps the newest {@code maxLines} lines of the given log.
     */
    public static String trimToLines(String log, int maxLines) {
        if (log == null || log.isEmpty() || maxLines <= 0) {
            return "";
        }
        String[] lines = log.split("\n");
        StringBuilder trimmed = new StringBuilder();
        int count = Math.min(lines.length, maxLines);
        for (int i = 0; i < count; i++) {
            trimmed.append(lines[i]).append('\n');
        }
        return trimmed.toString();
    }

    private static void writeLine(Context context, String event) {
        final Context appContext = context.getApplicationContext() == null
                ? context
                : context.getApplicationContext();
        try {
            WRITER.execute(() -> appendLine(appContext, event));
        } catch (Exception e) {
            Log.w(TAG, "Could not queue diagnostics entry", e);
        }
    }

    private static void appendLine(Context context, String event) {
        synchronized (LOCK) {
            try {
                SharedPreferences prefs = prefs(context);
                StringBuilder builder = new StringBuilder();
                if (processStartPending) {
                    processStartPending = false;
                    builder.append(stamp())
                            .append("  --- app process started (pid ")
                            .append(android.os.Process.myPid())
                            .append(") ---")
                            .append('\n');
                }
                builder.append(stamp()).append("  ").append(event).append('\n');
                builder.append(prefs.getString(KEY_LOG, ""));
                prefs.edit().putString(KEY_LOG, trimToLines(builder.toString(), MAX_LINES)).apply();
            } catch (Exception e) {
                Log.w(TAG, "Could not store diagnostics", e);
            }
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String stamp() {
        return stamp(System.currentTimeMillis());
    }

    private static String stamp(long time) {
        return new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(time));
    }
}
