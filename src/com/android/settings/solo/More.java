/*
 * Copyright (C) 2016 Tuesday-Rom
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

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class More extends SettingsPreferenceFragment {
    private static final String KEY_LOCK_CLOCK =
            "lock_clock";

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.TUNER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.more);

        // Remove the lock clock preference if its not installed
        if (!isPackageInstalled("com.cyanogenmod.lockclock")) {
            removePreference(KEY_LOCK_CLOCK);
        }

    }

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
           pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
           installed = true;
        } catch (PackageManager.NameNotFoundException e) {
           installed = false;
        }
        return installed;
    }
}
