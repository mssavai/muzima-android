/*
 * Copyright (c) 2014 - 2017. The Trustees of Indiana University, Moi University
 * and Vanderbilt University Medical Center.
 *
 * This version of the code is licensed under the MPL 2.0 Open Source license
 * with additional health care disclaimer.
 * If the user is an entity intending to commercialize any application that uses
 *  this code in a for-profit venture,please contact the copyright holder.
 */

package com.muzima.view;

import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.api.model.Patient;
import com.muzima.api.model.Tag;
import com.muzima.api.model.User;
import com.muzima.controller.CohortController;
import com.muzima.controller.FormController;
import com.muzima.controller.NotificationController;
import com.muzima.controller.PatientController;
import com.muzima.domain.Credentials;
import com.muzima.model.AvailableForm;
import com.muzima.model.collections.AvailableForms;
import com.muzima.scheduler.RealTimeFormUploader;
import com.muzima.service.WizardFinishPreferenceService;
import com.muzima.view.cohort.CohortActivity;
import com.muzima.view.forms.FormViewIntent;
import com.muzima.view.forms.FormsActivity;
import com.muzima.view.webviewapp.JavascriptAppWebViewActivitity;
import com.muzima.view.forms.RegistrationFormsActivity;
import com.muzima.view.notifications.NotificationsListActivity;
import com.muzima.view.patients.PatientsListActivity;
import org.apache.lucene.queryParser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.muzima.utils.Constants.NotificationStatusConstants.NOTIFICATION_UNREAD;

public class MainActivity extends BroadcastListenerActivity {
    private static final String TAG = "MainActivity";
    private View mMainView;
    private BackgroundQueryTask mBackgroundQueryTask;
    private Credentials credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentials = new Credentials(this);
        mMainView = getLayoutInflater().inflate(R.layout.activity_dashboard, null);
        setContentView(mMainView);
        RealTimeFormUploader.getInstance().uploadAllCompletedForms(getApplicationContext());
        setupActionbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showIncompleteWizardWarning();
        executeBackgroundTask();
    }

    private void showIncompleteWizardWarning() {
        if (!new WizardFinishPreferenceService(this).isWizardFinished()) {
            if (checkIfDisclaimerIsAccepted()) {
                Toast
                        .makeText(getApplicationContext(), getString(R.string.error_wizard_interrupted), Toast.LENGTH_LONG)
                        .show();
            }

        }
    }

    private boolean checkIfDisclaimerIsAccepted() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String disclaimerKey = getResources().getString(R.string.preference_disclaimer);
        boolean disclaimerAccepted = settings.getBoolean(disclaimerKey, false);
        return disclaimerAccepted;
    }

    @Override
    protected void onStop() {
        if (mBackgroundQueryTask != null) {
            mBackgroundQueryTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ((MuzimaApplication) getApplication()).logOut();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setIcon(getResources().getDrawable(R.drawable.ic_warning))
                .setTitle(getResources().getString(R.string.title_logout_confirm))
                .setMessage(getResources().getString(R.string.warning_logout_confirm))
                .setPositiveButton(getString(R.string.general_yes), dialogYesClickListener())
                .setNegativeButton(getString(R.string.general_no), null)
                .create()
                .show();
    }

    private Dialog.OnClickListener dialogYesClickListener() {
        return new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MuzimaApplication) getApplication()).logOut();
                finish();
                System.exit(0);
            }
        };
    }

    /**
     * Called when the user clicks the Cohort area
     */
    public void cohortList(View view) {
        Intent intent = new Intent(this, CohortActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Clients area or Search Clients Button
     */
    public void patientList(View view) {
        Intent intent = new Intent(this, PatientsListActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Forms area
     */
    public void formsList(View view) {
        Intent intent = new Intent(this, FormsActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Notifications area
     */
    public void notificationsList(View view) {
        Intent intent = new Intent(this, NotificationsListActivity.class);
        startActivity(intent);
    }

    /**
     * Called when the user clicks the Register Client Button
     */
    public void registerClient(View view) {
        Intent intent = new Intent(this, RegistrationFormsActivity.class);
        startActivity(intent);
    }

    private AvailableForms getSHRViewerForm(FormController formController){
        AvailableForms availableForms = null;
        try {
            List<String> tagUuids = new ArrayList<>();
            List<Tag> tags = formController.getAllTags();
            for(Tag tag:tags){
                if(tag.getName().equalsIgnoreCase("shr_viewer")){
                    tagUuids.add(tag.getUuid());
                }
            }
            availableForms = formController.getAvailableFormByTags(tagUuids);
        } catch (FormController.FormFetchException e) {
            Log.e(TAG, "Error while retrieving registration forms from Lucene");
        }
        return availableForms;
    }

    private void startWebViewActivity(AvailableForm form) {
        Intent intent = new Intent(this,JavascriptAppWebViewActivitity.class);
        String appTitle = "SHR Editor/viewer";
        intent.putExtra(JavascriptAppWebViewActivitity.APP_TITLE, appTitle);
        intent.putExtra(JavascriptAppWebViewActivitity.APP_SOURCE_FORM, form);
        startActivity(intent);
    }

    public void showSHRViewer(){
        FormController formController = ((MuzimaApplication) getApplicationContext()).getFormController();
        AvailableForms availableForms = getSHRViewerForm(formController);
        if(availableForms.size() == 1){
            startWebViewActivity(availableForms.get(0));
        } else if(availableForms.size() == 0) {
            Toast.makeText(this,"Could not find SHR Editor/viewer",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,"Could not uniquely identify SHR Editor/viewer",Toast.LENGTH_LONG).show();
        }
    }

    public class BackgroundQueryTask extends AsyncTask<Void, Void, HomeActivityMetadata> {

        @Override
        protected HomeActivityMetadata doInBackground(Void... voids) {
            MuzimaApplication muzimaApplication = (MuzimaApplication) getApplication();
            HomeActivityMetadata homeActivityMetadata = new HomeActivityMetadata();
            CohortController cohortController = muzimaApplication.getCohortController();
            PatientController patientController = muzimaApplication.getPatientController();
            FormController formController = muzimaApplication.getFormController();
            NotificationController notificationController = muzimaApplication.getNotificationController();
            try {
                homeActivityMetadata.totalCohorts = cohortController.countAllCohorts();
                homeActivityMetadata.syncedCohorts = cohortController.countSyncedCohorts();
                homeActivityMetadata.syncedPatients = patientController.countAllPatients();
                homeActivityMetadata.incompleteForms = formController.countAllIncompleteForms();
                homeActivityMetadata.completeAndUnsyncedForms = formController.countAllCompleteForms();

                // Notifications
                User authenticatedUser = ((MuzimaApplication) getApplicationContext()).getAuthenticatedUser();
                if (authenticatedUser != null) {
                    homeActivityMetadata.newNotifications = notificationController
                            .getAllNotificationsByReceiverCount(authenticatedUser.getPerson().getUuid(), NOTIFICATION_UNREAD);
                    homeActivityMetadata.totalNotifications = notificationController
                            .getAllNotificationsByReceiverCount(authenticatedUser.getPerson().getUuid(), null);
                } else {
                    homeActivityMetadata.newNotifications = 0;
                    homeActivityMetadata.totalNotifications = 0;
                }
            } catch (CohortController.CohortFetchException e) {
                Log.w(TAG, "CohortFetchException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (PatientController.PatientLoadException e) {
                Log.w(TAG, "PatientLoadException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (FormController.FormFetchException e) {
                Log.w(TAG, "FormFetchException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (NotificationController.NotificationFetchException e) {
                Log.w(TAG, "NotificationFetchException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (ParseException e) {
                Log.w(TAG, "ParseException occurred while fetching metadata in MainActivityBackgroundTask", e);
            }
            return homeActivityMetadata;
        }

        @Override
        protected void onPostExecute(HomeActivityMetadata homeActivityMetadata) {
            TextView cohortsDescriptionView = (TextView) mMainView.findViewById(R.id.cohortDescription);
            cohortsDescriptionView.setText(getString(R.string.hint_dashboard_cohorts_description,
                    homeActivityMetadata.syncedCohorts, homeActivityMetadata.totalCohorts));

            TextView patientDescriptionView = (TextView) mMainView.findViewById(R.id.patientDescription);
            patientDescriptionView.setText(getString(R.string.hint_dashboard_clients_description,
                    homeActivityMetadata.syncedPatients));

            TextView formsDescription = (TextView) mMainView.findViewById(R.id.formDescription);
            formsDescription.setText(getString(R.string.hint_dashboard_forms_description,
                    homeActivityMetadata.incompleteForms, homeActivityMetadata.completeAndUnsyncedForms));

            TextView notificationsDescription = (TextView) mMainView.findViewById(R.id.notificationDescription);
            notificationsDescription.setText(getString(R.string.hint_dashboard_notifications_description,
                    homeActivityMetadata.newNotifications, homeActivityMetadata.totalNotifications));

            TextView currentUser = (TextView) findViewById(R.id.currentUser);
            currentUser.setText(getResources().getString(R.string.general_welcome) + " " + credentials.getUserName());
        }
    }

    private static class HomeActivityMetadata {
        int totalCohorts;
        int syncedCohorts;
        int syncedPatients;
        int incompleteForms;
        int completeAndUnsyncedForms;
        int newNotifications;
        int totalNotifications;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_shr_viewer:
                showSHRViewer();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupActionbar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void executeBackgroundTask() {
        mBackgroundQueryTask = new BackgroundQueryTask();
        mBackgroundQueryTask.execute();
    }
}
