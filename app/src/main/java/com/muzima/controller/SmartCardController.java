package com.muzima.controller;

import com.muzima.api.model.SmartCardSharedHealthRecord;
import com.muzima.api.service.SmartCardSharedHealthRecordService;

import java.io.IOException;


public class SmartCardController {
    public static final String TAG ="SmartCardController";

    SmartCardSharedHealthRecordService sharedHealthRecordService;

    public SmartCardController(SmartCardSharedHealthRecordService sharedHealthRecordService){
        this.sharedHealthRecordService = sharedHealthRecordService;
    }
    public void saveSmartCardSharedHealthRecord(SmartCardSharedHealthRecord sharedHealthRecord){
        try {
            sharedHealthRecordService.saveSmartCardSharedHealthRecord(sharedHealthRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void updateSmartCardSharedHealthRecord(SmartCardSharedHealthRecord sharedHealthRecord){
        try {
            sharedHealthRecordService.updateSmartCardSharedHealthRecord(sharedHealthRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public SmartCardSharedHealthRecord getSmartCardSharedHealthRecordByUuid(String uuid){
        try {
            return sharedHealthRecordService.getSmartCardSharedHealthRecordByUuid(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public SmartCardSharedHealthRecord getSmartCardSharedHealthRecordByPatientUuid(String patientUuid){
        try {
            return sharedHealthRecordService.getSmartCardSharedHealthRecordByPatientUuid(patientUuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    void syncSmartCardSharedHealthRecord(SmartCardSharedHealthRecord sharedHealthRecord){
        try {
            sharedHealthRecordService.syncSmartCardSharedHealthRecord(sharedHealthRecord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
