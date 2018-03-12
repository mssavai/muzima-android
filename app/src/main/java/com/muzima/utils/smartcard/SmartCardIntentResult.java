package com.muzima.utils.smartcard;

import com.muzima.model.shr.SHRModel;

public class SmartCardIntentResult {
    private SHRModel shrModel;
    private String errors;

    public void setSHRModel(SHRModel shrModel) {
        this.shrModel = shrModel;
    }

    public SHRModel getSHRModel() {
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
