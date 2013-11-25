package com.muzima.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.muzima.view.preferences.SettingsActivity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ValidateURLTask extends AsyncTask<String, Void, Boolean> {

    private final SettingsActivity settingsActivity;
    private ProgressDialog progressDialog;

    public ValidateURLTask(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(settingsActivity);
        progressDialog.setMessage("Step 1: Validating URL");
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(strings[0] + "/ws/rest/v1/session");
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        progressDialog.dismiss();
        settingsActivity.validationURLResult(aBoolean);
        super.onPostExecute(aBoolean);
    }
}