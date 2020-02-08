/*
 * Copyright (C) 2017-2019 The PixelDust Project
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
package com.derpquest.settings.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.derpquest.settings.preferences.CustomSeekBarPreference;
import com.derpquest.settings.preferences.SecureSettingSeekBarPreference;
import com.derpquest.settings.preferences.SecureSettingSwitchPreference;
import com.derpquest.settings.preferences.SecureSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingListPreference;
import com.derpquest.settings.preferences.SystemSettingMasterSwitchPreference;
import com.derpquest.settings.preferences.SystemSettingSeekBarPreference;
import com.derpquest.settings.preferences.SystemSettingSwitchPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FINGERPRINT_VIB = "fingerprint_success_vib";
    private static final String LOCKSCREEN_VISUALIZER_ENABLED = "lockscreen_visualizer_enabled";
    private static final String KEY_AMBIENT_VIS = "ambient_visualizer";
    private static final String LOCKSCREEN_ALBUM_ART_FILTER = "lockscreen_album_art_filter";
    private static final String LOCKSCREEN_CLOCK = "lockscreen_clock";
    private static final String LOCKSCREEN_INFO = "lockscreen_info";
    private static final String LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String KEY_LAVALAMP = "lockscreen_lavalamp_enabled";
    private static final String KEY_LAVALAMP_SPEED = "lockscreen_lavalamp_speed";
    private static final String KEY_AUTOCOLOR = "lockscreen_visualizer_autocolor";
    private static final String KEY_SOLID_UNITS = "lockscreen_solid_units_count";
    private static final String KEY_FUDGE_FACTOR = "lockscreen_solid_fudge_factor";
    private static final String KEY_OPACITY = "lockscreen_solid_units_opacity";
    private static final String KEY_COLOR = "lockscreen_visualizer_color";
    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";
    private static final String KEY_BATT_BAR_COLOR = "sysui_keyguard_battery_bar_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private CustomSeekBarPreference mPulseBrightness;
    private CustomSeekBarPreference mDozeBrightness;
    private SecureSettingSwitchPreference mVisualizerEnabled;
    private SystemSettingListPreference mArtFilter;
    private SystemSettingMasterSwitchPreference mClockEnabled;
    private SystemSettingMasterSwitchPreference mInfoEnabled;
    private SystemSettingSeekBarPreference mBlurSeekbar;
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SystemSettingSwitchPreference mAmbientVisualizer;
    private SecureSettingSwitchPreference mAutoColor;
    private SecureSettingSwitchPreference mLavaLamp;
    private SecureSettingSeekBarPreference mSolidUnits;
    private SecureSettingSeekBarPreference mFudgeFactor;
    private SecureSettingSeekBarPreference mOpacity;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mColor;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.derpquest_settings_lockscreen);
        final PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();

        mClockEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_CLOCK);
        mClockEnabled.setOnPreferenceChangeListener(this);
        int clockEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_CLOCK, 1);
        mClockEnabled.setChecked(clockEnabled != 0);

        mInfoEnabled = (SystemSettingMasterSwitchPreference) findPreference(LOCKSCREEN_INFO);
        mInfoEnabled.setOnPreferenceChangeListener(this);
        int infoEnabled = Settings.System.getInt(resolver,
                LOCKSCREEN_INFO, 1);
        mInfoEnabled.setChecked(infoEnabled != 0);
        mInfoEnabled.setEnabled(clockEnabled != 0);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintVib = (SwitchPreference) findPreference(FINGERPRINT_VIB);
        if (mFingerprintManager == null) {
            prefScreen.removePreference(mFingerprintVib);
        } else {
            mFingerprintVib.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.FINGERPRINT_SUCCESS_VIB, 1) == 1));
            mFingerprintVib.setOnPreferenceChangeListener(this);
        }

        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;
        mAutoColor = (SecureSettingSwitchPreference) findPreference(KEY_AUTOCOLOR);
        mAutoColor.setOnPreferenceChangeListener(this);
        mAutoColor.setChecked(mAutoColorEnabled);

        if (mLavaLampEnabled) {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_lavalamp));
        } else {
            mAutoColor.setSummary(getActivity().getString(
                    R.string.lockscreen_autocolor_summary));
        }

        mLavaLamp = (SecureSettingSwitchPreference) findPreference(KEY_LAVALAMP);
        mLavaLamp.setOnPreferenceChangeListener(this);

        mSolidUnits = (SecureSettingSeekBarPreference) findPreference(KEY_SOLID_UNITS);
        mSolidUnits.setOnPreferenceChangeListener(this);
        mSolidUnits.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, 32));

        mFudgeFactor = (SecureSettingSeekBarPreference) findPreference(KEY_FUDGE_FACTOR);
        mFudgeFactor.setOnPreferenceChangeListener(this);
        mFudgeFactor.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, 16));

        mOpacity = (SecureSettingSeekBarPreference) findPreference(KEY_OPACITY);
        mOpacity.setOnPreferenceChangeListener(this);
        mOpacity.setValue(Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, 140));

        mColor = (ColorPickerPreference) findPreference(KEY_COLOR);
        mColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, DEFAULT_COLOR);
        String hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mColor.setSummary(hexColor);
        mColor.setNewPreviewColor(intColor);

        mVisualizerEnabled = (SecureSettingSwitchPreference) findPreference(LOCKSCREEN_VISUALIZER_ENABLED);
        mVisualizerEnabled.setOnPreferenceChangeListener(this);
        int visualizerEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0);
        mVisualizerEnabled.setChecked(visualizerEnabled != 0);

        UpdateEnablement(resolver);

        mArtFilter = (SystemSettingListPreference) findPreference(LOCKSCREEN_ALBUM_ART_FILTER);
        mArtFilter.setOnPreferenceChangeListener(this);
        int artFilter = Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_ALBUM_ART_FILTER, 0);
        mBlurSeekbar = (SystemSettingSeekBarPreference) findPreference(LOCKSCREEN_MEDIA_BLUR);
        mBlurSeekbar.setEnabled(artFilter > 2);

        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (CustomSeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (CustomSeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);

        mBatteryBarColor = (ColorPickerPreference) findPreference(KEY_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSUI_KEYGUARD_BATTERY_BAR_COLOR, DEFAULT_COLOR);
        hexColor = String.format("#%08x", (DEFAULT_COLOR & intColor));
        mBatteryBarColor.setSummary(hexColor);
        mBatteryBarColor.setNewPreviewColor(intColor);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mClockEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_CLOCK, value ? 1 : 0);
            mInfoEnabled.setEnabled(value);
            return true;
        } else if (preference == mInfoEnabled) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
		            LOCKSCREEN_INFO, value ? 1 : 0);
            return true;
        } else if (preference == mFingerprintVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FINGERPRINT_SUCCESS_VIB, value ? 1 : 0);
            return true;
        } else if (preference == mVisualizerEnabled) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, value ? 1 : 0);
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mArtFilter) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_ALBUM_ART_FILTER, value);
            mBlurSeekbar.setEnabled(value > 2);
            return true;
        } else if (preference == mLavaLamp) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, value ? 1 : 0);
            if (value) {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_lavalamp));
            } else {
                mAutoColor.setSummary(getActivity().getString(
                        R.string.lockscreen_autocolor_summary));
            }
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mAutoColor) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, value ? 1 : 0);
            UpdateEnablement(resolver);
            return true;
        } else if (preference == mSolidUnits) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_COUNT, value);
            return true;
        } else if (preference == mFudgeFactor) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_FUDGE_FACTOR, value);
            return true;
        } else if (preference == mOpacity) {
            int value = (int) newValue;
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_SOLID_UNITS_OPACITY, value);
            return true;
        } else if (preference == mColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_VISUALIZER_COLOR, intHex);
            return true;
        } else if (preference == mPulseBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, value);
            return true;
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.SYSUI_KEYGUARD_BATTERY_BAR_COLOR, intHex);
            return true;
        }
        return false;
    }

    // Updates enablement of lockscreen visualizer toggles
    private void UpdateEnablement(ContentResolver resolver) {
        boolean mLavaLampEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_LAVALAMP_ENABLED, 1) != 0;
        boolean mAutoColorEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_AUTOCOLOR, 1) != 0;
        boolean visualizerEnabled = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCKSCREEN_VISUALIZER_ENABLED, 0) != 0;
        mLavaLamp.setEnabled(visualizerEnabled);
        mAutoColor.setEnabled(visualizerEnabled && !mLavaLampEnabled);
        mSolidUnits.setEnabled(visualizerEnabled);
        mFudgeFactor.setEnabled(visualizerEnabled);
        mOpacity.setEnabled(visualizerEnabled);
        mColor.setEnabled(visualizerEnabled && !mAutoColorEnabled && !mLavaLampEnabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OWLSNEST;
    }
}
