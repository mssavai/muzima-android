package com.muzima.utils.javascriptinterface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import com.muzima.MuzimaApplication;
import com.muzima.api.model.Patient;
import com.muzima.api.model.algorithm.PatientAlgorithm;
import com.muzima.controller.PatientController;
import com.muzima.utils.smartcard.SmartCardIntentIntegrator;
import com.muzima.view.webview.JavascriptAppWebViewActivitity;
import net.minidev.json.JSONValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharedHealthRecordViewerJavascriptInterface extends WebViewJavascriptInterface {
    private static String TAG = SharedHealthRecordViewerJavascriptInterface.class.getSimpleName();
    JavascriptAppWebViewActivitity activity;
    boolean readSharedHealthRecordOperationCompleted = false;
    String sharedHealthRecord;
    public SharedHealthRecordViewerJavascriptInterface(JavascriptAppWebViewActivitity activity){
        this.activity = activity;

    }
    @JavascriptInterface
    public String getSHRModelFromLocalStorageByPatientUuid(String patientUuid){
        Log.e(TAG,"Creating SHR model");
        return "{ " +
                "\"SHR\": \"***wholly encryted by the middleware/library***\", " +
                "\"ADDENDUM\": {" +
                "\"CARD_DETAILS\": {\"STATUS\": \"ACTIVE/INACTIVE\",\"REASON\": \"LOST/DEATH/DAMAGED\",\"LAST_UPDATED\": \"20180101\",\"LAST_UPDATED_FACILITY\": \"10829\"}," +
                "\"IDENTIFIERS\": [" +
                "{\"ID\": \"12345678-ADFGHJY-0987654-NHYI890\",\"IDENTIFIER_TYPE\": \"CARD_SERIAL_NUMBER\",\"ASSIGNING_AUTHORITY\": \"CARD_REGISTRY\",\"ASSIGNING_FACILITY\": \"10829\"}," +
                "{\"ID\": \"12345678\",\"IDENTIFIER_TYPE\": \"HEI_NUMBER\",\"ASSIGNING_AUTHORITY\": \"MCH\",\"ASSIGNING_FACILITY\": \"10829\"}," +
                "{\"ID\": \"12345678\",\"IDENTIFIER_TYPE\": \"CCC_NUMBER\",\"ASSIGNING_AUTHORITY\": \"CCC\",\"ASSIGNING_FACILITY\": \"10829\"}," +
                "{\"ID\": \"001\",\"IDENTIFIER_TYPE\": \"HTS_NUMBER\",\"ASSIGNING_AUTHORITY\": \"HTS\",\"ASSIGNING_FACILITY\": \"10829\"}," +
                "{\"ID\": \"12345678\",\"IDENTIFIER_TYPE\": \"PMTCT_NUMBER\",\"ASSIGNING_AUTHORITY\": \"PMTCT\",\"ASSIGNING_FACILITY\": \"10829\"}" +
                "] }" +
                "}";
    }

    @JavascriptInterface
    public void readSharedHealthRecordFromCard(){
        SmartCardIntentIntegrator integrator = new SmartCardIntentIntegrator(activity);
        integrator.initiateCardRead();
    }

    @JavascriptInterface
    public void writeSharedHealthRecordToCard(){
        SmartCardIntentIntegrator integrator = new SmartCardIntentIntegrator(activity);
        integrator.initiateCardRead();
    }

    public void onReadSharedHealthRecordFromCardActivityResultSuccess(String sharedHealthRecord){
        activity.loadUrl("javascript:document.loadShrRecord('"+sharedHealthRecord+"')");
    }

    public void onReadSharedHealthRecordFromCardActivityResultError(String errorMessage){
        activity.loadUrl("javascript:document.setShrReadError('"+errorMessage+"')");
    }

    public void onWriteSharedHealthRecordFromCardActivityResultSuccess(String sharedHealthRecord){
        activity.loadUrl("javascript:document.setWriteRecordResult('"+sharedHealthRecord+"')");
    }

    public void onWriteSharedHealthRecordFromCardActivityResultError(String errorMessage){
        activity.loadUrl("javascript:document.setShrWriteError('"+errorMessage+"')");
    }

    @JavascriptInterface
    public String getAllPatientListFromLocalStorage(){
        try {
            List<Patient> patients = ((MuzimaApplication)activity.getApplicationContext()).getPatientController().getAllPatients();
            List<String> patientJsonList = new ArrayList<>();
            for(Patient patient:patients){
                patientJsonList.add(getPatientJsonObject(patient));
            }
            return JSONValue.toJSONString(patientJsonList);
        } catch (PatientController.PatientLoadException e) {
            Log.e(TAG, "Cannot retrieve patient list", e);
        }
        return null;
    }

    @JavascriptInterface
    public String getPatientListFromLocalStorage(String patientUuid){
        try {
            Patient patient = ((MuzimaApplication)activity.getApplicationContext()).getPatientController().getPatientByUuid(patientUuid);
            return getPatientJsonObject(patient);
        } catch (PatientController.PatientLoadException e) {
            Log.e(TAG, "Cannot retrieve patient", e);
        }
        return null;
    }

    private String getPatientJsonObject(Patient patient){
        PatientAlgorithm patientAlgorithm = new PatientAlgorithm();
        String patientJson = null;
        try {
            patientJson = patientAlgorithm.serialize(patient,true);
        } catch (IOException e) {
            Log.e(TAG, "Cannot serialize patient", e);
        }
        return patientJson;
    }
}
