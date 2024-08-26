package com.muzima.service;

import android.util.Log;

import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.utils.MuzimaPreferences;

import static com.muzima.util.Constants.ServerSettings.ONLINE_ONLY_MODE_ENABLED_DEFAULT_VALUE;

public class OnlineOnlyModePreferenceService extends PreferenceService{

    private final MuzimaApplication application;

    public OnlineOnlyModePreferenceService(MuzimaApplication application){
        super(application.getApplicationContext());
        this.application = application;
    }

    public Boolean getOnlineOnlyModePreferenceValue(){
        try {
        String key = context.getResources().getString(R.string.preference_online_only_mode);
        return MuzimaPreferences.getSecureSharedPreferences(context).getBoolean(key,ONLINE_ONLY_MODE_ENABLED_DEFAULT_VALUE);
        } catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error getting secure shared preferences");
            return false;
        }
    }

    public void updateOnlineOnlyModePreferenceValue(){
        try {
        boolean onlineOnlyModeEnabled = application.getMuzimaSettingController().isOnlineOnlyModeEnabled();
            String key = context.getResources().getString(R.string.preference_online_only_mode);
            MuzimaPreferences.getSecureSharedPreferences(context).edit().putBoolean(key,onlineOnlyModeEnabled).apply();
        } catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error getting secure shared preferences");
        }
    }
}
