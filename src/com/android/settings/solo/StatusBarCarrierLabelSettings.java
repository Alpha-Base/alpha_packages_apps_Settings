/*
 * Copyright (C) 2015 DarkKat
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

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarCarrierLabelSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_CARRIER_LABEL_SHOW =
            "carrier_label_show";
    private static final String PREF_CARRIER_LABEL_SHOW_ON_LOCK_SCREEN =
            "carrier_label_show_on_lock_screen";
    private static final String PREF_CARRIER_LABEL_USE_CUSTOM =
            "carrier_label_use_custom";
    private static final String PREF_CARRIER_LABEL_CUSTOM_LABEL =
            "carrier_label_custom_label";
    private static final String PREF_HIDE_LABEL =
            "carrier_label_hide_label";
    private static final String PREF_NUMBER_OF_NOTIFICATION_ICONS =
            "carrier_label_number_of_notification_icons";

    private SwitchPreference mShow;
    private SwitchPreference mShowOnLockScreen;
    private SwitchPreference mUseCustom;
    private EditTextPreference mCustomLabel;
    private SwitchPreference mHideLabel;
    private ListPreference mNumberOfNotificationIcons;

    private ContentResolver mResolver;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.TUNER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.status_bar_carrier_label_settings);
        mResolver = getActivity().getContentResolver();

        final boolean show = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_CARRIER_LABEL_SHOW, 1) == 1;
        final boolean showOnLockScreen = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_CARRIER_LABEL_SHOW_ON_LOCK_SCREEN, 1) == 1;
        final boolean useCustom = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_CARRIER_LABEL_USE_CUSTOM, 1) == 1;
        final boolean hideLabel = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_CARRIER_LABEL_HIDE_LABEL, 1) == 1;
        final boolean isHidden = !show && !showOnLockScreen;

        PreferenceCategory catNotificationIcons =
                (PreferenceCategory) findPreference("carrier_label_cat_notification_icons");

        mShow = (SwitchPreference) findPreference(PREF_CARRIER_LABEL_SHOW);
        mShow.setChecked(show);
        mShow.setOnPreferenceChangeListener(this);

        mShowOnLockScreen = (SwitchPreference) findPreference(PREF_CARRIER_LABEL_SHOW_ON_LOCK_SCREEN);
        mShowOnLockScreen.setChecked(showOnLockScreen);
        mShowOnLockScreen.setOnPreferenceChangeListener(this);

        if (!isHidden) {
            mUseCustom = (SwitchPreference) findPreference(PREF_CARRIER_LABEL_USE_CUSTOM);
            mUseCustom.setChecked(useCustom);
            mUseCustom.setOnPreferenceChangeListener(this);

            if (useCustom) {
                mCustomLabel = (EditTextPreference) findPreference(PREF_CARRIER_LABEL_CUSTOM_LABEL);
                mCustomLabel.getEditText().setHint(getResources().getString(
                        com.android.internal.R.string.default_custom_label));
                mCustomLabel.setOnPreferenceChangeListener(this);
                updateCustomLabelPreference();
            } else {
                removePreference(PREF_CARRIER_LABEL_CUSTOM_LABEL);
            }

            if (show) {
                mHideLabel =
                        (SwitchPreference) findPreference(PREF_HIDE_LABEL);
                mHideLabel.setChecked(hideLabel);
                mHideLabel.setOnPreferenceChangeListener(this);
                if (hideLabel) {
                    mNumberOfNotificationIcons =
                            (ListPreference) findPreference(PREF_NUMBER_OF_NOTIFICATION_ICONS);
                    int numberOfNotificationIcons = Settings.System.getInt(mResolver,
                           Settings.System.STATUS_BAR_CARRIER_LABEL_NUMBER_OF_NOTIFICATION_ICONS, 1);
                    mNumberOfNotificationIcons.setValue(String.valueOf(numberOfNotificationIcons));
                    mNumberOfNotificationIcons.setSummary(mNumberOfNotificationIcons.getEntry());
                    mNumberOfNotificationIcons.setOnPreferenceChangeListener(this);
                } else {
                    catNotificationIcons.removePreference(findPreference(PREF_NUMBER_OF_NOTIFICATION_ICONS));
                }
            } else {
                catNotificationIcons.removePreference(findPreference(PREF_HIDE_LABEL));
                catNotificationIcons.removePreference(findPreference(PREF_NUMBER_OF_NOTIFICATION_ICONS));
                removePreference("carrier_label_cat_notification_icons");
            }
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value;

        if (preference == mShow) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_SHOW,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowOnLockScreen) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_SHOW_ON_LOCK_SCREEN,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mUseCustom) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_USE_CUSTOM,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mCustomLabel) {
            String label = (String) newValue;
            Settings.System.putString(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_CUSTOM_LABEL, label);
            updateCustomLabelPreference();
        } else if (preference == mHideLabel) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_HIDE_LABEL,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mNumberOfNotificationIcons) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mNumberOfNotificationIcons.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_CARRIER_LABEL_NUMBER_OF_NOTIFICATION_ICONS,
                    intValue);
            preference.setSummary(mNumberOfNotificationIcons.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void updateCustomLabelPreference() {
        String customLabelText = Settings.System.getString(mResolver,
                Settings.System.STATUS_BAR_CARRIER_LABEL_CUSTOM_LABEL);
        String customLabelDefaultSummary = getResources().getString(
                    com.android.internal.R.string.default_custom_label);
        if (customLabelText == null) {
            customLabelText = "";
        }
        mCustomLabel.setText(customLabelText);
        mCustomLabel.setSummary(
                customLabelText.isEmpty() ? customLabelDefaultSummary : customLabelText);
    }
}

