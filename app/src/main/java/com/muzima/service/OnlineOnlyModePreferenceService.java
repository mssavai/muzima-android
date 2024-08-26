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
        String key = context.getResources().getString(R.string.preference_online_only_mode);
        return MuzimaPreferences.getBooleanPreference(context, key, ONLINE_ONLY_MODE_ENABLED_DEFAULT_VALUE);
    }

    public void updateOnlineOnlyModePreferenceValue(){
        boolean onlineOnlyModeEnabled = application.getMuzimaSettingController().isOnlineOnlyModeEnabled();
        String key = context.getResources().getString(R.string.preference_online_only_mode);
        MuzimaPreferences.setBooleanPreference(context, key,onlineOnlyModeEnabled);
    }
}
