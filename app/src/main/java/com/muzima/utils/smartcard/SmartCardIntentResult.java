package com.muzima.utils.smartcard;

import com.muzima.model.shr.SHRModel;

public class SmartCardIntentResult {
    private SHRModel shrModel;
    private Throwable errors;

    public void setSHRModel(SHRModel shrModel) {
        this.shrModel = shrModel;
    }

    public SHRModel getSHRModel() {
        return shrModel;
    }

    public void setErrors(Throwable errors) {
        this.errors = errors;
    }

    public Throwable getErrors() {
        return errors;
    }
}
