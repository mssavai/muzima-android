package com.muzima.utils.smartcard;

import com.muzima.api.model.SmartCardSharedHealthRecord;

public class SmartCardIntentResult {
    private SmartCardSharedHealthRecord shrModel;
    private String errors;

    public void setSHRModel(SmartCardSharedHealthRecord shrModel) {
        this.shrModel = shrModel;
    }

    public SmartCardSharedHealthRecord getSHRModel() {
        return shrModel;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getErrors() {
        return errors;
    }

    public boolean isSuccessResult(){
        return shrModel != null;
    }
}
