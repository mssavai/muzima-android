package com.muzima.utils.javascriptinterface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.muzima.MuzimaApplication;
import com.muzima.api.model.Patient;
import com.muzima.api.model.PatientIdentifierType;
import com.muzima.api.model.SmartCardSharedHealthRecord;
import com.muzima.api.model.algorithm.PatientAlgorithm;
import com.muzima.controller.MuzimaSettingController;
import com.muzima.controller.PatientController;
import com.muzima.controller.SmartCardController;
import com.muzima.utils.smartcard.SmartCardIntentIntegrator;
import com.muzima.view.forms.GenericRegistrationPatientJSONMapper;
import com.muzima.view.webview.JavascriptAppWebViewActivitity;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.json.JSONException;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SharedHealthRecordViewerJavascriptInterface extends WebViewJavascriptInterface {
    private static String TAG = SharedHealthRecordViewerJavascriptInterface.class.getSimpleName();
    JavascriptAppWebViewActivitity activity;
    boolean readSharedHealthRecordOperationCompleted = false;
    //String sharedHealthRecord;
    public SharedHealthRecordViewerJavascriptInterface(JavascriptAppWebViewActivitity activity){
        this.activity = activity;

    }
    @JavascriptInterface
    public String getSHRModelFromLocalStorageByPatientUuid(String patientUuid){
        SmartCardSharedHealthRecord shr = ((MuzimaApplication)activity.getApplicationContext())
                .getSmartCardController().getSmartCardSharedHealthRecordByPatientUuid(patientUuid);
        if(shr != null){
            return shr.getPlainSHRPayload();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status","FAIL");
        jsonObject.put("errorMessage","No record found");
        return jsonObject.toJSONString();
    }

    @JavascriptInterface
    public void readSharedHealthRecordFromCard(){
        SmartCardIntentIntegrator integrator = new SmartCardIntentIntegrator(activity);
        integrator.initiateCardRead();
    }

    @JavascriptInterface
    public void writeSharedHealthRecordToCard(String shr){
        SmartCardIntentIntegrator integrator = new SmartCardIntentIntegrator(activity);
        try {
            integrator.initiateCardWrite(shr);
        } catch (IOException e) {
            Toast.makeText(activity,"Could not write SHR to card",Toast.LENGTH_LONG).show();
            Log.e(TAG,"Could not write SHR to card",e);
        }
    }

    public void onReadSharedHealthRecordFromCardActivityResultSuccess(String sharedHealthRecord){
        activity.loadUrl("javascript:document.loadShrRecord_callback('"+sharedHealthRecord+"')");
    }

    public void onReadSharedHealthRecordFromCardActivityResultError(String errorMessage){
        activity.loadUrl("javascript:document.setShrReadError_callback('"+errorMessage+"')");
    }

    public void onWriteSharedHealthRecordFromCardActivityResultSuccess(String sharedHealthRecord){
        Toast.makeText(activity,"SHR record written to card successfully",Toast.LENGTH_LONG).show();
        activity.loadUrl("javascript:document.setWriteRecordResult_callback('"+sharedHealthRecord+"')");
    }

    public void onWriteSharedHealthRecordFromCardActivityResultError(String errorMessage){
        activity.loadUrl("javascript:document.setShrWriteError_callback('"+errorMessage+"')");
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

    @JavascriptInterface
    public String getPatientByIdentifier(String identifier, String identifierType){
        try {
            PatientController patientController = ((MuzimaApplication)activity.getApplicationContext()).getPatientController();
            List<PatientIdentifierType> identifiers = patientController.getPatientIdentifierTypeByName(identifierType);
            Patient patient = ((MuzimaApplication)activity.getApplicationContext()).getPatientController().getPatientByIdentifier(identifier);
            return getPatientJsonObject(patient);
        } catch (PatientController.PatientLoadException e) {
            Log.e(TAG, "Cannot retrieve patient", e);
        }
        return null;
    }
    @JavascriptInterface
    public String createAndSavePatientFromPayload(String payload){
        PatientController patientController = ((MuzimaApplication)activity.getApplicationContext()).getPatientController();
        MuzimaSettingController settingController = ((MuzimaApplication)activity.getApplicationContext()).getMuzimaSettingController();
        JSONObject jsonObject = new JSONObject();
        try {
            Patient patient = new GenericRegistrationPatientJSONMapper().getPatient(payload,patientController,settingController);
            if(patient.getBirthdate() == null){
                patient.setBirthdate(new Date());
            }
            patientController.savePatient(patient);
            Toast.makeText(activity,"Patient record created successfully",Toast.LENGTH_LONG).show();
            return getPatientJsonObject(patient);
        } catch (JSONException e) {
            Log.e(TAG,"Could not parse Patient payload:"+payload,e);
            jsonObject.put("status","FAIL");
            jsonObject.put("errorMessage","could not extract patient record");
        } catch (PatientController.PatientSaveException e){
            Log.e(TAG,"Could not save Patient record",e);
            jsonObject.put("errorMessage","Could not save patient record");
        }
        return jsonObject.toJSONString();
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

    @JavascriptInterface
    public String getNewUuid(){
        return UUID.randomUUID().toString();
    }
}
