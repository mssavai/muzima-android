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
        String key = context.getResources().getString(R.string.preference_confidentiality_notice_display);
        return MuzimaPreferences.getBooleanPreference(context, key, CONFIDENTIALITY_NOTICE_DISPLAY_ENABLED_DEFAULT_VALUE);
    }

    public void updateConfidentialityNoticeDisplayPreferenceValue(){
        boolean confidentialityNoticeDisplayEnabled = application.getMuzimaSettingController().isConfidentialityNoticeDisplayEnabled();
        String key = context.getResources().getString(R.string.preference_confidentiality_notice_display);
        MuzimaPreferences.setBooleanPreference(context, key, confidentialityNoticeDisplayEnabled);
    }
}
