package com.flxholle.quicktiles.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

/**
 * The single source of truth that maps a preference key of the settings screen to the tile service
 * behind it.
 * <p>
 * It lives outside of the activity on purpose: the settings screen, the boot receiver and the unit
 * tests all read this table, and a plain Java class can be loaded in a JVM test without an Android
 * runtime.
 */
public final class TileRegistry {

    private TileRegistry() {
    }

    /**
     * @return every tile that can be enabled in the settings screen, keyed by its preference key.
     * The order is the order of the settings screen.
     */
    public static Map<String, Class<?>> preferenceServices() {
        Map<String, Class<?>> map = new LinkedHashMap<>();
        map.put("play_pause", PlayPauseTileService.class);
        map.put("next_song", NextSongTileService.class);
        map.put("previous_song", PreviousSongTileService.class);
        map.put("media_volume", MediaVolumeTileService.class);
        map.put("grayscale", GrayscaleTileService.class);
        map.put("adaptive_brightness", AdaptiveBrightnessTileService.class);
        map.put("usb_debugging", UsbDebuggingTileService.class);
        map.put("usb_wifi_debugging", WirelessUsbDebuggingTileService.class);
        map.put("finish_activities", FinishActivitiesTileService.class);
        map.put("keep_screen_on", KeepScreenOnTileService.class);
        map.put("demo_mode", DemoModeTileService.class);
        map.put("animator_duration", AnimatorDurationTileService.class);
        map.put("show_taps", ShowTapsTileService.class);
        map.put("disable_all_animations", ToggleAnimationTileService.class);
        map.put("new_alarm", NewAlarmTileService.class);
        map.put("new_timer", NewTimerTileService.class);
        map.put("take_photo", TakePhotoTileService.class);
        map.put("record_video", RecordVideoTileService.class);
        map.put("open_calculator", OpenCalculatorTileService.class);
        map.put("open_volume_panel", OpenVolumePanelTileService.class);
        map.put("adjust_brightness", BrightnessTileService.class);
        map.put("open_files", OpenFilesTileService.class);
        map.put("battery", BatteryTileService.class);
        map.put("vpn", OpenVpnTileService.class);
        map.put("data_usage", OpenDataUsageTileService.class);
        map.put("about_phone", OpenAboutPhoneTileService.class);
        map.put("connected_devices", OpenConnectedDevicesTileService.class);
        map.put("notification_history", OpenNotificationLogTileService.class);
        map.put("search_settings", OpenSettingsSearchTileService.class);
        map.put("new_event", NewCalendarEventTileService.class);
        map.put("make_call", MakeCallTileService.class);
        map.put("counter", CounterTileService.class);
        map.put("screen_timeout", ScreenTimeoutTileService.class);
        map.put("vibrate_calls", VibrateCallsTileService.class);
        map.put("switch_states", DoNotDisturbSwitchTileService.class);
        map.put("force_rotation", RotationSwitchTileService.class);
        map.put("screen_cast", OpenCastTileService.class);
        map.put("developer_options", OpenDeveloperOptionsTileService.class);
        map.put("open_language", OpenLocaleTileService.class);
        map.put("custom_app_one", OpenAppOneTileService.class);
        map.put("custom_app_two", OpenAppTwoTileService.class);
        map.put("custom_app_three", OpenAppThreeTileService.class);
        map.put("custom_app_four", OpenAppFourTileService.class);
        map.put("custom_app_five", OpenAppFiveTileService.class);
        map.put("switch_silence_loud", SilenceLoudSwitchTileService.class);
        map.put("open_display", OpenDisplayTileService.class);
        map.put("open_home", OpenHomeTileService.class);
        map.put("open_privacy", OpenPrivacyTileService.class);
        map.put("open_user_dictionary", OpenUserDictionaryTileService.class);
        map.put("power_dialog", PowerDialogTileService.class);
        map.put("take_screenshot", ScreenshotTileService.class);
        map.put("lock_screen", LockScreenTileService.class);
        map.put("toggle_split_screen", ToggleSplitScreenTileService.class);
        map.put("fix_rotation", FixRotationTileService.class);
        map.put("heads_up", HeadsUpTileService.class);
        return Collections.unmodifiableMap(map);
    }
}
