/*
 * Copyright (C) 2015 Solo Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.solo;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.settings.preference.SecureSettingSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.utils.du.DUActionUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.Utils;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

public class Navigation extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String VOLUME_ROCKER_WAKE = "volume_rocker_wake";
    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String KEY_NAVBAR_MODE = "navbar_mode";
    private static final String KEY_AOSP_NAVBAR_SETTINGS = "aosp_navbar_settings";
    private static final String KEY_FLING_NAVBAR_SETTINGS = "fling_settings";
    private static final String KEY_CATEGORY_NAVIGATION_INTERFACE = "category_navbar_interface";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_BAR_SIZE = "navigation_bar_size";

    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mVolumeRockerWake;
    private SwitchPreference mCameraDoubleTapPowerGesture;
    private SwitchPreference mNavbarVisibility;
    private ListPreference mNavbarMode;
    private PreferenceScreen mNavbarSettings;
    private PreferenceScreen mFlingSettings;
    private PreferenceCategory mNavInterface;
    private PreferenceCategory mNavGeneral;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.TUNER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();
        addPreferencesFromResource(R.xml.navigation_settings);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mCameraDoubleTapPowerGesture
                    = (SwitchPreference) findPreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);

        if (mCameraDoubleTapPowerGesture != null &&
                    isCameraDoubleTapPowerGestureAvailable(getResources())) {
                // Update double tap power to launch camera if available.
                mCameraDoubleTapPowerGesture.setOnPreferenceChangeListener(this);
                int cameraDoubleTapPowerDisabled = Settings.Secure.getInt(
                        getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
                mCameraDoubleTapPowerGesture.setChecked(cameraDoubleTapPowerDisabled == 0);
        }

        int cursorControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);

        mVolumeRockerWake = (SwitchPreference) findPreference(VOLUME_ROCKER_WAKE);
        mVolumeRockerWake.setOnPreferenceChangeListener(this);
        int volumeRockerWake = Settings.System.getInt(getContentResolver(),
                VOLUME_ROCKER_WAKE, 0);
        mVolumeRockerWake.setChecked(volumeRockerWake != 0);

        mNavInterface = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_INTERFACE);
        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);
        mNavbarMode = (ListPreference) findPreference(KEY_NAVBAR_MODE);
        mNavbarSettings = (PreferenceScreen) findPreference(KEY_AOSP_NAVBAR_SETTINGS);
        mFlingSettings = (PreferenceScreen) findPreference(KEY_FLING_NAVBAR_SETTINGS);

        boolean showing = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_VISIBLE,
                DUActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);
        mNavbarVisibility.setOnPreferenceChangeListener(this);

        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);
        updateBarModeSettings(mode);
        mNavbarMode.setOnPreferenceChangeListener(this);

        // Navigation bar left-in-landscape
        // remove if not a phone
        if (!DUActionUtils.isNormalScreen()) {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_BAR_LEFT));
        }
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);

        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void updateBarModeSettings(int mode) {
        mNavbarMode.setValue(String.valueOf(mode));
        mNavbarSettings.setEnabled(mode == 0);
        mNavbarSettings.setSelectable(mode == 0);
        mFlingSettings.setEnabled(mode == 1);
        mFlingSettings.setSelectable(mode == 1);
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
        mNavInterface.setEnabled(mNavbarVisibility.isChecked());
        mNavGeneral.setEnabled(mNavbarVisibility.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCameraDoubleTapPowerGesture) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                    value ? 0 : 1 /* Backwards because setting is for disabling */);
            return true;
        } else if (preference == mVolumeKeyCursorControl) {
            handleActionListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mVolumeRockerWake) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), VOLUME_ROCKER_WAKE,
                    value ? 1 : 0);
            return true;
        } else if (preference.equals(mNavbarMode)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_MODE, mode);
            updateBarModeSettings(mode);
            return true;
        } else if (preference.equals(mNavbarVisibility)) {
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 1 : 0);
            updateBarVisibleAndUpdatePrefs(showing);
            return true;
        }
        return false;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }
}


