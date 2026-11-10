package com.flxholle.quicktiles;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.flxholle.quicktiles.accessibility_tiles.LockScreenTileService;
import com.flxholle.quicktiles.accessibility_tiles.PowerDialogTileService;
import com.flxholle.quicktiles.accessibility_tiles.ScreenshotTileService;
import com.flxholle.quicktiles.accessibility_tiles.ToggleSplitScreenTileService;
import com.flxholle.quicktiles.intent_tiles.BatteryTileService;
import com.flxholle.quicktiles.intent_tiles.MakeCallTileService;
import com.flxholle.quicktiles.intent_tiles.NewAlarmTileService;
import com.flxholle.quicktiles.intent_tiles.NewCalendarEventTileService;
import com.flxholle.quicktiles.intent_tiles.NewTimerTileService;
import com.flxholle.quicktiles.intent_tiles.NextSongTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAboutPhoneTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAppFiveTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAppFourTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAppOneTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAppThreeTileService;
import com.flxholle.quicktiles.intent_tiles.OpenAppTwoTileService;
import com.flxholle.quicktiles.intent_tiles.OpenCalculatorTileService;
import com.flxholle.quicktiles.intent_tiles.OpenCastTileService;
import com.flxholle.quicktiles.intent_tiles.OpenConnectedDevicesTileService;
import com.flxholle.quicktiles.intent_tiles.OpenDataUsageTileService;
import com.flxholle.quicktiles.intent_tiles.OpenDeveloperOptionsTileService;
import com.flxholle.quicktiles.intent_tiles.OpenDisplayTileService;
import com.flxholle.quicktiles.intent_tiles.OpenFilesTileService;
import com.flxholle.quicktiles.intent_tiles.OpenHomeTileService;
import com.flxholle.quicktiles.intent_tiles.OpenLocaleTileService;
import com.flxholle.quicktiles.intent_tiles.OpenNotificationLogTileService;
import com.flxholle.quicktiles.intent_tiles.OpenPrivacyTileService;
import com.flxholle.quicktiles.intent_tiles.OpenSettingsSearchTileService;
import com.flxholle.quicktiles.intent_tiles.OpenUserDictionaryTileService;
import com.flxholle.quicktiles.intent_tiles.OpenVolumePanelTileService;
import com.flxholle.quicktiles.intent_tiles.OpenVpnTileService;
import com.flxholle.quicktiles.intent_tiles.PlayPauseTileService;
import com.flxholle.quicktiles.intent_tiles.PreviousSongTileService;
import com.flxholle.quicktiles.intent_tiles.RecordVideoTileService;
import com.flxholle.quicktiles.intent_tiles.TakePhotoTileService;
import com.flxholle.quicktiles.tiles.AdaptiveBrightnessTileService;
import com.flxholle.quicktiles.tiles.AnimatorDurationTileService;
import com.flxholle.quicktiles.tiles.BrightnessTileService;
import com.flxholle.quicktiles.tiles.CounterTileService;
import com.flxholle.quicktiles.tiles.DemoModeTileService;
import com.flxholle.quicktiles.tiles.DoNotDisturbSwitchTileService;
import com.flxholle.quicktiles.tiles.FinishActivitiesTileService;
import com.flxholle.quicktiles.tiles.FixRotationTileService;
import com.flxholle.quicktiles.tiles.GrayscaleTileService;
import com.flxholle.quicktiles.tiles.HeadsUpTileService;
import com.flxholle.quicktiles.tiles.KeepScreenOnTileService;
import com.flxholle.quicktiles.tiles.MediaVolumeTileService;
import com.flxholle.quicktiles.tiles.RotationSwitchTileService;
import com.flxholle.quicktiles.tiles.ScreenTimeoutTileService;
import com.flxholle.quicktiles.tiles.ShowTapsTileService;
import com.flxholle.quicktiles.tiles.SilenceLoudSwitchTileService;
import com.flxholle.quicktiles.tiles.ToggleAnimationTileService;
import com.flxholle.quicktiles.tiles.UsbDebuggingTileService;
import com.flxholle.quicktiles.tiles.VibrateCallsTileService;
import com.flxholle.quicktiles.tiles.WirelessUsbDebuggingTileService;
import com.flxholle.quicktiles.utils.SharedPreferencesUtil;
import com.flxholle.quicktiles.utils.TileRegistry;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResult;
import com.bytehamster.lib.preferencesearch.SearchPreferenceResultListener;
import com.flxholle.quicktiles.R;

import java.util.ArrayList;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity implements SearchPreferenceResultListener {
    private com.flxholle.quicktiles.SettingsFragment prefsFragment;

    public static Map<String, Class<?>> getPreferenceService() {
        return TileRegistry.preferenceServices();
    }

    public static ArrayMap<Class<?>, String> getCustomAppKeys() {
        ArrayMap<Class<?>, String> servicePreferences = new ArrayMap<>();
        servicePreferences.put(OpenAppOneTileService.class, SharedPreferencesUtil.CUSTOM_PACKAGE_ONE);
        servicePreferences.put(OpenAppTwoTileService.class, SharedPreferencesUtil.CUSTOM_PACKAGE_TWO);
        servicePreferences.put(OpenAppThreeTileService.class, SharedPreferencesUtil.CUSTOM_PACKAGE_THREE);
        servicePreferences.put(OpenAppFourTileService.class, SharedPreferencesUtil.CUSTOM_PACKAGE_FOUR);
        servicePreferences.put(OpenAppFiveTileService.class, SharedPreferencesUtil.CUSTOM_PACKAGE_FIVE);
        return servicePreferences;
    }

    public static ArrayMap<Class<?>, Pair<Boolean, Integer>> getHigherThanMinAPIKeys() {
        //Pair: true = SDK_INT must be higher or equal; false = SDK_INT must be lower
        ArrayMap<Class<?>, Pair<Boolean, Integer>> services = new ArrayMap<>();
        services.put(GrayscaleTileService.class, new Pair<>(false, Build.VERSION_CODES.R));
        services.put(LockScreenTileService.class, new Pair<>(true, Build.VERSION_CODES.P));
        services.put(ScreenshotTileService.class, new Pair<>(true, Build.VERSION_CODES.P));
        services.put(ToggleSplitScreenTileService.class, new Pair<>(true, Build.VERSION_CODES.N));
        return services;
    }

    public static ArrayList<Class<?>> getSecureSettingsServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(GrayscaleTileService.class);
        services.add(UsbDebuggingTileService.class);
        services.add(FinishActivitiesTileService.class);
        services.add(AnimatorDurationTileService.class);
        services.add(ShowTapsTileService.class);
        services.add(ToggleAnimationTileService.class);
        services.add(RotationSwitchTileService.class);
        services.add(FixRotationTileService.class);
        services.add(HeadsUpTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getModifySystemSettingsServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(AdaptiveBrightnessTileService.class);
        services.add(BrightnessTileService.class);
        services.add(ScreenTimeoutTileService.class);
        services.add(VibrateCallsTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getNotificationPolicyServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(DoNotDisturbSwitchTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getSecureSettingsAndDumpServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(DemoModeTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getRootServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(WirelessUsbDebuggingTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getSecureSettingsModifySystemServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(KeepScreenOnTileService.class);
        return services;
    }

    public static ArrayList<Class<?>> getAccessibilityServiceServices() {
        ArrayList<Class<?>> services = new ArrayList<>();
        services.add(PowerDialogTileService.class);
        services.add(ScreenshotTileService.class);
        services.add(LockScreenTileService.class);
        services.add(ToggleSplitScreenTileService.class);
        return services;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
        }

        prefsFragment = new com.flxholle.quicktiles.SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, prefsFragment)
                .commit();
    }

    @Override
    public void onSearchResultClicked(SearchPreferenceResult result) {
        result.closeSearchPage(this);
        result.highlight(prefsFragment);
    }
}