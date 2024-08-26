/*
 * Copyright (c) The Trustees of Indiana University, Moi University
 * and Vanderbilt University Medical Center. All Rights Reserved.
 *
 * This version of the code is licensed under the MPL 2.0 Open Source license
 * with additional health care disclaimer.
 * If the user is an entity intending to commercialize any application that uses
 * this code in a for-profit venture, please contact the copyright holder.
 */

package com.muzima.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;

import com.muzima.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Support system preferences including prefs not
 * displayed in the settings UI
 */
public class MuzimaPreferences {
    public static SharedPreferences getSecureSharedPreferences(Context context) throws Exception {
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "filename",
                "muzima_secret_shared_prefs",
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        return sharedPreferences;
    }

    private static final String ON_BOARDING_COMPLETED_PREFERENCE = "onboarding_completed_pref";

    public static void setOnBoardingCompletedPreference(Context context, boolean isLightMode) {
        setBooleanPreference(context, ON_BOARDING_COMPLETED_PREFERENCE, isLightMode);
    }

    public static boolean getOnBoardingCompletedPreference(Context context) {
        return getBooleanPreference(context, ON_BOARDING_COMPLETED_PREFERENCE, false);
    }

    public static boolean getIsLightModeThemeSelectedPreference(Context context) {
        try {
            return getSecureSharedPreferences(context)
                    .getBoolean(context.getResources().getString(R.string.preference_light_mode), false);
        } catch (Exception e) {
            Log.e("MuzimaPreferences", "Error getting secure shared preferences");
            return false;
        }
    }

    public static void setBooleanPreference(Context context, String key, boolean value) {
        try {
            getSecureSharedPreferences(context).edit().putBoolean(key, value).apply();
        } catch (Exception e){
            Log.e("MuzimaPreferences", "Error getting secure shared preferences");
        }
    }

    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        try {
            return getSecureSharedPreferences(context).getBoolean(key, defaultValue);
        } catch (Exception e){
            Log.e("MuzimaPreferences", "Error getting secure shared preferences");
            return defaultValue;
        }
    }

    public static void setStringPreference(Context context, String key, String value) {
        try {
            getSecureSharedPreferences(context).edit().putString(key, value).apply();
        } catch (Exception e){
            Log.e("MuzimaPreferences", "Error getting secure shared preferences");
        }
    }

    public static String getStringPreference(Context context, String key, String defaultValue) {
        try {
            return getSecureSharedPreferences(context).getString(key, defaultValue);
        } catch (Exception e){
            Log.e("MuzimaPreferences", "Error getting secure shared preferences");
            return defaultValue;
        }
    }
}
