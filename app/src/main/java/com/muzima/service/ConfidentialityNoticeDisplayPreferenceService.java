package com.muzima.service;

import android.util.Log;

import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.utils.MuzimaPreferences;

import static com.muzima.util.Constants.ServerSettings.CONFIDENTIALITY_NOTICE_DISPLAY_ENABLED_DEFAULT_VALUE;
public class ConfidentialityNoticeDisplayPreferenceService extends PreferenceService{

    private final MuzimaApplication application;

    public ConfidentialityNoticeDisplayPreferenceService(MuzimaApplication application){
        super(application.getApplicationContext());
        this.application = application;
    }
    public Boolean getConfidentialityNoticeDisplayPreferenceValue(){
        try {
            String key = context.getResources().getString(R.string.preference_confidentiality_notice_display);
            return MuzimaPreferences.getSecureSharedPreferences(context).getBoolean(key, CONFIDENTIALITY_NOTICE_DISPLAY_ENABLED_DEFAULT_VALUE);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error getting secure shared preferences");
            return false;
        }
    }

    public void updateConfidentialityNoticeDisplayPreferenceValue(){
        try {
            boolean confidentialityNoticeDisplayEnabled = application.getMuzimaSettingController().isConfidentialityNoticeDisplayEnabled();
            String key = context.getResources().getString(R.string.preference_confidentiality_notice_display);
            MuzimaPreferences.getSecureSharedPreferences(context).edit().putBoolean(key, confidentialityNoticeDisplayEnabled).apply();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error getting secure shared preferences");
        }
    }
}
