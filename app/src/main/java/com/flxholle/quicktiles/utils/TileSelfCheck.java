package com.flxholle.quicktiles.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

import com.flxholle.quicktiles.SettingsActivity;
import com.flxholle.quicktiles.abstract_tiles.AccessibilityTileService;
import com.flxholle.quicktiles.abstract_tiles.ModifySystemSettingsTileService;
import com.flxholle.quicktiles.abstract_tiles.NotificationPolicyTileService;
import com.flxholle.quicktiles.abstract_tiles.WriteSecureSettingsTileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.preference.PreferenceManager;

/**
 * Checks the conditions a tile needs on this device and writes a short report for the settings
 * screen: missing permissions, tile components that a ROM or the system disabled, battery
 * restrictions and how the previous processes ended.
 * <p>
 * The point is to answer "why does this tile not work?" without a logcat and without guessing, and
 * to make that answer copyable for a bug report.
 */
public final class TileSelfCheck {

    private static final String NOT_CHECKED = "not checked";

    private TileSelfCheck() {
    }

    public static String buildReport(Context context) {
        StringBuilder report = new StringBuilder();
        report.append("QuickTiles ").append(versionName(context))
                .append(" on Android ").append(Build.VERSION.RELEASE)
                .append(" (SDK ").append(Build.VERSION.SDK_INT).append(')').append('\n');
        report.append("Diagnostics recording: ")
                .append(TileDiagnostics.isRecordingEnabled(context) ? "on" : "off").append('\n');
        report.append("Battery: ").append(batterySummary(context)).append('\n');

        List<String> missingPermissions = new ArrayList<>();
        List<String> disabledComponents = new ArrayList<>();
        int enabledTiles = 0;

        Map<String, Class<?>> services = SettingsActivity.getPreferenceService();
        for (Map.Entry<String, Class<?>> entry : services.entrySet()) {
            if (!isTileEnabled(context, entry.getKey())) {
                continue;
            }
            enabledTiles++;
            Class<?> serviceClass = entry.getValue();
            if (isPermissionMissing(context, serviceClass)) {
                missingPermissions.add(entry.getKey() + " needs "
                        + describeRequirement(serviceClass));
            }
            if (!isComponentEnabled(context, serviceClass)) {
                disabledComponents.add(entry.getKey() + " is enabled in the app, but its tile "
                        + "service is disabled by the system");
            }
        }

        report.append("Tiles enabled in the app: ").append(enabledTiles).append('\n');

        report.append('\n').append("Missing permissions:").append('\n');
        appendList(report, missingPermissions);

        report.append('\n').append("Tile services that the system disabled:").append('\n');
        appendList(report, disabledComponents);

        report.append('\n').append("Previous process exits:").append('\n');
        List<String> exits = TileDiagnostics.readExitHistory(context);
        if (exits.isEmpty()) {
            report.append("- none recorded").append('\n');
        }
        for (String exit : exits) {
            report.append("- ").append(exit).append('\n');
        }

        String fatal = TileDiagnostics.lastFatal(context);
        report.append('\n').append("Last crash: ")
                .append(fatal.isEmpty() ? "none recorded" : fatal).append('\n');

        return report.toString();
    }

    private static void appendList(StringBuilder report, List<String> lines) {
        if (lines.isEmpty()) {
            report.append("- none").append('\n');
            return;
        }
        for (String line : lines) {
            report.append("- ").append(line).append('\n');
        }
    }

    private static boolean isTileEnabled(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, false);
    }

    private static String batterySummary(Context context) {
        try {
            PowerManager powerManager =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) {
                return NOT_CHECKED;
            }
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName())
                    ? "unrestricted"
                    : "optimised - the system may stop the app, see 'Why do my tiles stop working?'";
        } catch (Exception e) {
            return NOT_CHECKED;
        }
    }

    private static boolean isComponentEnabled(Context context, Class<?> serviceClass) {
        try {
            ComponentName component = new ComponentName(context, serviceClass);
            int state = context.getPackageManager().getComponentEnabledSetting(component);
            return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * @return true if the tile needs a permission that the app does not have on this device.
     */
    public static boolean isPermissionMissing(Context context, Class<?> serviceClass) {
        String requirement = describeRequirement(serviceClass);
        return requirement != null && !hasRequirement(context, serviceClass);
    }

    /**
     * @return the permission the tile asks for when it is tapped, or {@code null} if it needs none.
     */
    private static String describeRequirement(Class<?> serviceClass) {
        if (AccessibilityTileService.class.isAssignableFrom(serviceClass)) {
            return "the QuickTiles accessibility service";
        }
        if (NotificationPolicyTileService.class.isAssignableFrom(serviceClass)) {
            return "Do Not Disturb access";
        }
        if (WriteSecureSettingsTileService.class.isAssignableFrom(serviceClass)) {
            return "WRITE_SECURE_SETTINGS (adb)";
        }
        if (ModifySystemSettingsTileService.class.isAssignableFrom(serviceClass)) {
            return "\"Modify system settings\"";
        }
        return null;
    }

    private static boolean hasRequirement(Context context, Class<?> serviceClass) {
        if (AccessibilityTileService.class.isAssignableFrom(serviceClass)) {
            return GrantPermissionDialogs.hasAccessibilityServicePermission(context);
        }
        if (NotificationPolicyTileService.class.isAssignableFrom(serviceClass)) {
            return GrantPermissionDialogs.hasNotificationPolicyPermission(context);
        }
        if (WriteSecureSettingsTileService.class.isAssignableFrom(serviceClass)) {
            return GrantPermissionDialogs.hasWriteSecureSettingsPermission(context);
        }
        if (ModifySystemSettingsTileService.class.isAssignableFrom(serviceClass)) {
            return GrantPermissionDialogs.hasModifySystemSettingsPermission(context);
        }
        return true;
    }

    private static String versionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName + " (" + info.versionCode + ")";
        } catch (Exception e) {
            return "?";
        }
    }
}
