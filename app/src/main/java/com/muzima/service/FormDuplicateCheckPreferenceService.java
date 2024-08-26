package com.muzima.service;

import android.content.res.Resources;

import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.utils.MuzimaPreferences;

public class FormDuplicateCheckPreferenceService extends PreferenceService{
    private final MuzimaApplication application;

    public FormDuplicateCheckPreferenceService(MuzimaApplication muzimaApplication) {
        super(muzimaApplication);
        this.application = muzimaApplication;
    }

    public void updateFormDuplicateCheckPreferenceSettings(){
        boolean isFormDuplicateCheckEnabled = application.getMuzimaSettingController()
                .isFormDuplicateCheckEnabled();
        Resources resources = context.getResources();
        String key = resources.getString(R.string.preference_duplicate_form_data_key);

        MuzimaPreferences.setBooleanPreference(context,key, isFormDuplicateCheckEnabled);
    }

    public Boolean isFormDuplicateCheckSettingEnabled(){
        Resources resources = context.getResources();
        String key = resources.getString(R.string.preference_duplicate_form_data_key);
        return MuzimaPreferences.getBooleanPreference(context, key, false);
    }
}
