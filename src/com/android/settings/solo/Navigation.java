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

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

public class Navigation extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";

    private SwitchPreference mCameraDoubleTapPowerGesture;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.TUNER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navigation_settings);

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
         }
        return false;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }
}


