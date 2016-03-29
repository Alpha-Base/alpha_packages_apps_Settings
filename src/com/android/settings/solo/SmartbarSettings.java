/*
 * Copyright (C) 2016 The DirtyUnicorns Project
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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.utils.du.ActionConstants;
import com.android.internal.utils.du.Config;
import com.android.internal.utils.du.Config.ButtonConfig;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class SmartbarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private ListPreference mSmartBarContext;
    private ListPreference mImeActions;
    private ListPreference mButtonAnim;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DIALOG_RESET_CONFIRM = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.smartbar_settings);

        int contextVal = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                "smartbar_context_menu_mode", 0, UserHandle.USER_CURRENT);
        mSmartBarContext = (ListPreference) findPreference("smartbar_context_menu_position");
        mSmartBarContext.setValue(String.valueOf(contextVal));
        mSmartBarContext.setOnPreferenceChangeListener(this);

        int imeVal = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                "smartbar_ime_hint_mode", 1, UserHandle.USER_CURRENT);
        mImeActions = (ListPreference) findPreference("smartbar_ime_action");
        mImeActions.setValue(String.valueOf(imeVal));
        mImeActions.setOnPreferenceChangeListener(this);

        int buttonAnimVal = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                "smartbar_button_animation_style", 0, UserHandle.USER_CURRENT);
        mButtonAnim = (ListPreference) findPreference("smartbar_button_animation");
        mButtonAnim.setValue(String.valueOf(buttonAnimVal));
        mButtonAnim.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {
            case DIALOG_RESET_CONFIRM:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.smartbar_factory_reset_title);
                alertDialog.setMessage(R.string.smartbar_factory_reset_confirm);
                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetSmartbar();
                    }
                });
                alertDialog.setNegativeButton(R.string.write_settings_off, null);
                dialog = alertDialog.create();
                break;
        }
        return dialog;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == findPreference("smartbar_editor_mode")) {
            getActivity().sendBroadcastAsUser(new Intent("intent_navbar_edit"), UserHandle.CURRENT);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialog(DIALOG_RESET_CONFIRM);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetSmartbar() {
        ArrayList<ButtonConfig> buttonConfigs = Config.getDefaultConfig(
               mContext,
        ActionConstants.getDefaults(ActionConstants.SMARTBAR));
               Config.setConfig(mContext,
        ActionConstants.getDefaults(ActionConstants.SMARTBAR),
               buttonConfigs);
        Intent intent = new Intent("intent_navbar_edit");
        intent.putExtra("extra_navbar_edit_reset_layout", "resetMePlox");
        getActivity().sendBroadcastAsUser(intent, UserHandle.CURRENT);

        Settings.Secure.putInt(mContext.getContentResolver(),
                "smartbar_context_menu_mode", 0);
        mSmartBarContext.setValue(String.valueOf(0));
        mSmartBarContext.setOnPreferenceChangeListener(this);

        Settings.Secure.putInt(mContext.getContentResolver(),
                "smartbar_ime_hint_mode", 1);
        mImeActions.setValue(String.valueOf(1));
        mImeActions.setOnPreferenceChangeListener(this);

        Settings.Secure.putInt(mContext.getContentResolver(),
                "smartbar_button_animation_style", 0);
        mButtonAnim.setValue(String.valueOf(0));
        mButtonAnim.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mSmartBarContext)) {
            int position = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(), "smartbar_context_menu_mode",
                    position);
            return true;
        } else if (preference.equals(mButtonAnim)) {
            int val = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(), "smartbar_button_animation_style",
                    val);
            return true;
        } else if (preference.equals(mImeActions)) {
            int val = Integer.parseInt(((String) newValue).toString());
            Settings.Secure.putInt(getContentResolver(), "smartbar_ime_hint_mode",
                    val);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.TUNER;
    }
}

