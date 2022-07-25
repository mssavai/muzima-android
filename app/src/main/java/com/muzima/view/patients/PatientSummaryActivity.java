/*
 * Copyright (c) The Trustees of Indiana University, Moi University
 * and Vanderbilt University Medical Center. All Rights Reserved.
 *
 * This version of the code is licensed under the MPL 2.0 Open Source license
 * with additional health care disclaimer.
 * If the user is an entity intending to commercialize any application that uses
 *  this code in a for-profit venture,please contact the copyright holder.
 */

package com.muzima.view.patients;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.adapters.ListAdapter;
import com.muzima.adapters.forms.ClientSummaryFormsAdapter;
import com.muzima.adapters.patients.PatientAdapterHelper;
import com.muzima.adapters.relationships.RelationshipTypesAdapter;
import com.muzima.adapters.relationships.RelationshipsAdapter;
import com.muzima.api.model.Cohort;
import com.muzima.api.model.CohortMember;
import com.muzima.api.model.Location;
import com.muzima.api.model.MuzimaSetting;
import com.muzima.api.model.Observation;
import com.muzima.api.model.Patient;
import com.muzima.api.model.Person;
import com.muzima.api.model.Relationship;
import com.muzima.api.model.SmartCardRecord;
import com.muzima.api.model.User;
import com.muzima.api.service.SmartCardRecordService;
import com.muzima.controller.CohortController;
import com.muzima.controller.EncounterController;
import com.muzima.controller.FormController;
import com.muzima.controller.MuzimaSettingController;
import com.muzima.controller.NotificationController;
import com.muzima.controller.ObservationController;
import com.muzima.controller.PatientController;
import com.muzima.controller.PatientReportController;
import com.muzima.controller.RelationshipController;
import com.muzima.controller.SmartCardController;
import com.muzima.model.AvailableForm;
import com.muzima.model.collections.AvailableForms;
import com.muzima.model.shr.kenyaemr.Addendum.Identifier;
import com.muzima.model.shr.kenyaemr.Addendum.WriteResponse;
import com.muzima.model.shr.kenyaemr.InternalPatientId;
import com.muzima.model.shr.kenyaemr.KenyaEmrSHRModel;
import com.muzima.service.JSONInputOutputToDisk;
import com.muzima.utils.Constants;
import com.muzima.utils.Fonts;
import com.muzima.utils.LocationUtils;
import com.muzima.utils.StringUtils;
import com.muzima.utils.ThemeUtils;
import com.muzima.utils.smartcard.KenyaEmrShrMapper;
import com.muzima.utils.smartcard.SmartCardIntentIntegrator;
import com.muzima.utils.smartcard.SmartCardIntentResult;
import com.muzima.view.BaseActivity;
import com.muzima.view.SHRObservationsDataActivity;
import com.muzima.view.custom.MuzimaRecyclerView;
import com.muzima.view.encounters.EncountersActivity;
import com.muzima.view.forms.FormViewIntent;
import com.muzima.view.forms.PatientFormsActivity;
import com.muzima.view.forms.PersonDemographicsUpdateFormsActivity;
import com.muzima.view.forms.RegistrationFormsActivity;
import com.muzima.view.notifications.PatientNotificationActivity;
import com.muzima.view.observations.ChronologicalObsViewFragment;
import com.muzima.view.observations.ObservationsActivity;
import com.muzima.view.relationship.RelationshipsListActivity;
import com.muzima.view.reports.PatientReportActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.muzima.utils.DateUtils.getFormattedDate;
import static com.muzima.utils.smartcard.SmartCardIntentIntegrator.SMARTCARD_READ_REQUEST_CODE;
import static com.muzima.utils.smartcard.SmartCardIntentIntegrator.SMARTCARD_WRITE_REQUEST_CODE;
import static com.muzima.view.relationship.RelationshipsListActivity.INDEX_PATIENT;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ListView;

import com.muzima.controller.RelationshipController;
import com.muzima.controller.PatientController;
import com.muzima.adapters.relationships.RelationshipsAdapter;
import com.muzima.adapters.relationships.RelationshipTypesAdapter;

import es.dmoral.toasty.Toasty;

public class PatientSummaryActivity extends BaseActivity implements ListAdapter.BackgroundListQueryTaskListener,  ClientSummaryFormsAdapter.OnFormClickedListener{
    private static final String TAG = "PatientSummaryActivity";
    public static final String PATIENT = "patient";
    public static final boolean DEFAULT_SHR_STATUS = false;
    private static final boolean DEFAULT_RELATIONSHIP_STATUS = false;
    public static final int FORM_VIEW_ACTIVITY_RESULT = 1;

    private AlertDialog writeSHRDataOptionDialog;

    private BackgroundQueryTask mBackgroundQueryTask;

    private Patient patient;
    private ImageView imageView;
    private Boolean isRegisteredOnSHR;

    private TextView searchDialogTextView;
    private Button yesOptionSHRSearchButton;
    private Button noOptionSHRSearchButton;

    private SmartCardRecord smartCardRecord;
    private SmartCardController smartCardController;
    private MuzimaApplication muzimaApplication;
    private Location defaultLocation;
    private final ThemeUtils themeUtils = new ThemeUtils();
    private boolean isSHREnabled;
    private boolean isRelationshipEnabled;
    private List<AvailableForm> forms = new ArrayList<>();
    private ClientSummaryFormsAdapter formsAdapter;
    private ListView lvwPatientRelationships;
    private RelationshipsAdapter patientRelationshipsAdapter;
    private PatientController patientController;
    private RelationshipController relationshipController;
    private View noDataView;
    private Spinner relationshipType;
    private boolean actionModeActive = false;
    private ActionMode actionMode;
    private Person selectedRelatedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        themeUtils.onCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_summary_overview);
        muzimaApplication = (MuzimaApplication) getApplicationContext( );

        setSHREnabled();
        setSHRLayoutVisibility();
        setClinicalSummaryVisibility();
        setRelationshipEnabled();
        setTitle(R.string.title_activity_client_summary);

        Bundle intentExtras = getIntent( ).getExtras( );
        if (intentExtras != null) {
            patient = (Patient) intentExtras.getSerializable(PATIENT);

            logEvent("VIEW_CLIENT_SUMMARY","{\"patientuuid\":\""+patient.getUuid()+"\"}");

            isRegisteredOnSHR = patient.getIdentifier(Constants.Shr.KenyaEmr.PersonIdentifierType.CARD_SERIAL_NUMBER.name) == null;

            SmartCardController smartCardController = ((MuzimaApplication) getApplicationContext( )).getSmartCardController( );
            try {
                isRegisteredOnSHR = smartCardController.getSmartCardRecordByPersonUuid(patient.getUuid( )) != null;
            } catch (SmartCardController.SmartCardRecordFetchException e) {
                Log.e(getClass( ).getSimpleName( ), "Error while retrieving smartcard record", e);
            }
        }

        setupPatientMetadata( );
        notifyOfIdChange( );
        initializeView();

        try {
            SmartCardRecordService smartCardRecordService = muzimaApplication.getMuzimaContext( ).getSmartCardRecordService( );
            smartCardController = new SmartCardController(smartCardRecordService);

        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),"Encountered IOException while trying to set smartcard controller",e);
        }
        imageView = findViewById(R.id.sync_status_imageview);
        if (isRegisteredOnSHR) {
            prepareWriteToCardOptionDialog(getApplicationContext( ));
        } else {
            prepareNonSHRWriteToCardOptionDialog(getApplicationContext( ));
        }


        try {
            forms = muzimaApplication.getFormController().getRecommendedForms();
        } catch (FormController.FormFetchException e) {
            e.printStackTrace();
        }

        MuzimaRecyclerView formsListRecyclerView = findViewById(R.id.recycler_list);
        formsListRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext(), LinearLayoutManager.VERTICAL, false));

        formsAdapter = new ClientSummaryFormsAdapter(forms, this);
        formsListRecyclerView.setAdapter(formsAdapter);
        formsListRecyclerView.setNoDataLayout(findViewById(R.id.no_data_layout),
                getString(R.string.info_forms_unavailable),
                getString(R.string.info_no_forms_data_tip));

        loadChronologicalObsView();

        relationshipController = ((MuzimaApplication) getApplicationContext()).getRelationshipController();
        patientController = ((MuzimaApplication) getApplicationContext()).getPatientController();

        setupPatientRelationships();
    }

    public void initializeView(){
        LinearLayout historicalData = findViewById(R.id.historical_data);
        LinearLayout dataCollection = findViewById(R.id.data_collection);
        LinearLayout relationship_list_data = findViewById(R.id.relationship_list_data);

        List<Observation> observations = new ArrayList<>();
        AvailableForms forms = new AvailableForms();
        List<Relationship> relationships = new ArrayList<>(0);

        try {
            observations = ((MuzimaApplication) getApplication().getApplicationContext()).getObservationController().getObservationsByPatient(patient.getUuid());
            forms = ((MuzimaApplication) getApplication().getApplicationContext()).getFormController().getRecommendedForms();
            relationships = ((MuzimaApplication) getApplication().getApplicationContext()).getRelationshipController().getRelationshipsForPerson(patient.getUuid());
        }catch (ObservationController.LoadObservationException | FormController.FormFetchException | RelationshipController.RetrieveRelationshipException ex){
            Log.e(getClass().getSimpleName(),"Exception encountered while loading patients "+ex);
        }

        // Get current cohort definition
        boolean isContactListingEnabled =  isContactsListingEnabled((MuzimaApplication) this.getApplication());
        //relationship_list_data.setVisibility(isContactListingEnabled == true? 1 : 0);

        if((forms.size() == 0 && observations.size() == 0 && relationships.size() == 0) || (forms.size() > 0 && observations.size() > 0 && relationships.size() > 0)){
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    35
            );
            historicalData.setLayoutParams(param);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            dataCollection.setLayoutParams(params);

            LinearLayout.LayoutParams paramRLD = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    60
            );
            relationship_list_data.setLayoutParams(paramRLD);

        }else if(observations.size() > 0 && relationships.size() == 0 && forms.size() == 0 ){
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    50
            );
            historicalData.setLayoutParams(param);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    20
            );
            dataCollection.setLayoutParams(params);

            LinearLayout.LayoutParams paramRLD = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            relationship_list_data.setLayoutParams(paramRLD);
        }

        else if(observations.size() == 0 && relationships.size() > 0 && forms.size() == 0) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            historicalData.setLayoutParams(param);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    20
            );
            dataCollection.setLayoutParams(params);

            LinearLayout.LayoutParams paramRLD = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    50
            );
            relationship_list_data.setLayoutParams(paramRLD);

        }

        else if(observations.size() == 0 && relationships.size() == 0 && forms.size() > 0){
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            historicalData.setLayoutParams(param);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    40
            );
            dataCollection.setLayoutParams(params);

            LinearLayout.LayoutParams paramRLD = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            relationship_list_data.setLayoutParams(paramRLD);

        }

        else {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    35
            );
            historicalData.setLayoutParams(param);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    30
            );
            dataCollection.setLayoutParams(params);

            LinearLayout.LayoutParams paramRLD = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    35
            );
            relationship_list_data.setLayoutParams(paramRLD);

        }
    }

    private void notifyOfIdChange() {
        final JSONInputOutputToDisk jsonInputOutputToDisk = new JSONInputOutputToDisk(getApplication());
        List list = null;
        try {
            list = jsonInputOutputToDisk.readList();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Exception thrown when reading to phone disk", e);
        }
        if (list.size() == 0) {
            return;
        }

        final String patientIdentifier = patient.getIdentifier();
        if (list.contains(patientIdentifier)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true)
                    .setIcon(themeUtils.getIconWarning(this))
                    .setTitle(getString(R.string.general_notice))
                    .setMessage(getString(R.string.info_client_identifier_change))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            patient.removeIdentifier(Constants.LOCAL_PATIENT);
                            try {
                                jsonInputOutputToDisk.remove(patientIdentifier);
                            } catch (IOException e) {
                                Log.e(getClass().getSimpleName(), "Error occurred while saving patient which has local identifier removed!", e);
                            }
                        }
                    }).create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        themeUtils.onResume(this);
        handleSHREnabledChanged();
        executeBackgroundTask();
        patientRelationshipsAdapter.reloadData();
    }

    @Override
    protected void onStop() {
        if (mBackgroundQueryTask != null) {
            mBackgroundQueryTask.cancel(true);
        }
        super.onStop();
    }

    private void setupPatientMetadata() {

        TextView patientName = findViewById(R.id.patientName);
        patientName.setText(PatientAdapterHelper.getPatientFormattedName(patient));

        ImageView genderIcon = findViewById(R.id.genderImg);
        int genderDrawable = patient.getGender().equalsIgnoreCase("M") ? R.drawable.ic_male : R.drawable.ic_female;
        genderIcon.setImageDrawable(getResources().getDrawable(genderDrawable));

        TextView dob = findViewById(R.id.dob);
        if(patient.getBirthdate() != null) {
            dob.setText(String.format("DOB: %s", getFormattedDate(patient.getBirthdate())));
        }else{
            dob.setText(String.format(""));
        }

        TextView patientIdentifier = findViewById(R.id.patientIdentifier);
        patientIdentifier.setText(patient.getIdentifier());
    }

    private void loadChronologicalObsView(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        ChronologicalObsViewFragment chronologicalObsViewFragment = ChronologicalObsViewFragment.newInstance(muzimaApplication.getObservationController(), patient);
        transaction.replace(R.id.chronological_fragment, chronologicalObsViewFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_summary, menu);
        MenuItem SHRCardMenuItem = menu.findItem(R.id.SHR_client_summary);
        MenuItem relationshipsMenuItem = menu.findItem(R.id.client_relationship);
        MenuItem geoMappingMenuItem = menu.findItem(R.id.client_geomapping);

        if(isSHREnabled) {
            if (isRegisteredOnSHR) {
                SHRCardMenuItem.setIcon(R.drawable.ic_action_shr_card);
            } else {
                SHRCardMenuItem.setVisible(true);
                SHRCardMenuItem.setIcon(R.drawable.ic_action_shr_card);
            }
        } else {
            SHRCardMenuItem.setVisible(false);
        }

        if (!isRelationshipEnabled) {
            relationshipsMenuItem.setVisible(false);
        }

        if(!isGeoMappingFeatureEnabled()) {
            geoMappingMenuItem.setVisible(false);
        }

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.SHR_client_summary:
                //todo write card workspace.
                if (isRegisteredOnSHR) {
                    Log.e(getClass().getSimpleName(), "is Patient SHR");
                    prepareWriteToCardOptionDialog(getApplicationContext());
                    writeSHRDataOptionDialog.show();
                } else {
                    Log.e(getClass().getSimpleName(), "is Patient not SHR");
                    prepareNonSHRWriteToCardOptionDialog(getApplicationContext());
                    writeSHRDataOptionDialog.show();
                }
                break;
            case R.id.client_relationship:
                showRelationships();
                return true;

            case R.id.client_geomapping:
                navigateToClientLocationMap();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initiateSHRWriteToCard() {
        SmartCardController smartCardController = ((MuzimaApplication) getApplicationContext()).getSmartCardController();
        SmartCardRecord smartCardRecord = null;
        try {
            KenyaEmrShrMapper.updateSHRSmartCardRecordForPatient((MuzimaApplication) getApplicationContext(),patient.getUuid());
            smartCardRecord = smartCardController.getSmartCardRecordByPersonUuid(patient.getUuid());
        } catch (SmartCardController.SmartCardRecordFetchException e) {
            Snackbar.make(findViewById(R.id.client_summary_view), R.string.failure_obtain_smartcard_record+e.getMessage(), Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark))
                    .setAction(R.string.general_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initiateSHRWriteToCard();
                        }
                    })
                    .show();
            Log.e(getClass().getSimpleName(), "Could not obtain smartcard record for writing to card", e);
        } catch (KenyaEmrShrMapper.ShrParseException e) {
            Snackbar.make(findViewById(R.id.client_summary_view), R.string.failure_obtain_smartcard_record+e.getMessage(), Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark))
                    .setAction(R.string.general_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            initiateSHRWriteToCard();
                        }
                    })
                    .show();
            Log.e(getClass().getSimpleName(), getString(R.string.failure_updating_smartcard_record_for_writing), e);
        }
        if (smartCardRecord != null) {
            SmartCardIntentIntegrator SHRIntegrator = new SmartCardIntentIntegrator(this);
            SHRIntegrator.initiateCardWrite(smartCardRecord.getPlainPayload());
            Toast.makeText(getApplicationContext(), getString(R.string.hint_opening_card_reader), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        switch (requestCode) {
            case SMARTCARD_READ_REQUEST_CODE:
                processSmartCardReadResult(requestCode, resultCode, dataIntent);
                break;

            case SMARTCARD_WRITE_REQUEST_CODE:
                SmartCardIntentResult cardWriteIntentResult = null;
                try {
                    cardWriteIntentResult = SmartCardIntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
                    List<String> writeErrors = cardWriteIntentResult.getErrors();

                    if (writeErrors == null) {
                        Snackbar.make(findViewById(R.id.client_summary_view), R.string.success_writing_smartcard, Snackbar.LENGTH_LONG)
                                .show();
                        SmartCardRecord result = cardWriteIntentResult.getSmartCardRecord();

                        try {
                            SmartCardRecord smartCardRecord = smartCardController.getSmartCardRecordByPersonUuid(patient.getUuid());
                            smartCardRecord.setEncryptedPayload(result.getEncryptedPayload());
                            smartCardRecord.setWrittenToCard(true);
                            smartCardRecord.setSyncedToServer(false);
                            smartCardController.updateSmartCardRecord(smartCardRecord);

                            //Deserialize result.getEncryptedPayload() to WriteResponse
                            // get the card serial writeresponse.getcarddetails.
                            WriteResponse writeResponse = new ObjectMapper().readValue(result.getEncryptedPayload(), WriteResponse.class);
                            String cardSerial = null;
                            List<Identifier> addendumIdentifiers = writeResponse.getAddendum().getIdentifiers();
                            for (Identifier id : addendumIdentifiers) {
                                if(id.getIdentifierType().equals("CARD_SERIAL_NUMBER")) {
                                    cardSerial = id.getId();
                                    break;
                                }
                            }
                            KenyaEmrShrMapper.updatePatientDemographicsWithCardSerialNumberAsIdentifier(muzimaApplication,patient,cardSerial);

                        } catch (SmartCardController.SmartCardRecordFetchException e) {
                            Log.e(getClass().getSimpleName(),"Could not retrieve SHR from local storage");
                        } catch (SmartCardController.SmartCardRecordSaveException e) {
                            Log.e(getClass().getSimpleName(),"Could not save SHR from local storage");
                        }

                    } else if (writeErrors != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Snackbar.make(findViewById(R.id.client_summary_view), R.string.failure_writing_smartcard + writeErrors.get(0), Snackbar.LENGTH_LONG)
                                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark, null))
                                    .setAction(R.string.general_retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            initiateSHRWriteToCard();
                                        }
                                    })
                                    .show();
                        } else {

                            Snackbar.make(findViewById(R.id.client_summary_view), R.string.failure_writing_smartcard + writeErrors.get(0), Snackbar.LENGTH_LONG)
                                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_dark))
                                    .setAction(R.string.general_retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            initiateSHRWriteToCard();
                                        }
                                    })
                                    .show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                writeSHRDataOptionDialog.dismiss();
                writeSHRDataOptionDialog.cancel();
                break;
        }
    }

    public void showForms(View v) {
        Intent intent = new Intent(this, PatientFormsActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void showNotifications(View v) {
        Intent intent = new Intent(this, PatientNotificationActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void showObservations(View v) {
        Intent intent = new Intent(this, ObservationsActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void showEncounters(View v) {
        Intent intent = new Intent(this, EncountersActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void showSHRObservations(View v) {
        Intent intent = new Intent(PatientSummaryActivity.this, SHRObservationsDataActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }
    
    public void showReports(View v){
        Intent intent = new Intent(this, PatientReportActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void showRelationshipsView(View v) {
        Intent intent = new Intent(this, RelationshipsListActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    private void showRelationships() {
        Intent intent = new Intent(this, RelationshipsListActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    private void navigateToClientLocationMap() {
        Intent intent = new Intent(this, PatientLocationMapActivity.class);
        intent.putExtra(PATIENT, patient);
        startActivity(intent);
    }

    public void switchSyncStatus(View view) {
        imageView.setImageResource(R.drawable.ic_action_shr_synced);
    }

    @Override
    public void onFormClickedListener(int position) {
        AvailableForm form = forms.get(position);
        Intent intent = new FormViewIntent(this, form, patient , false);
        intent.putExtra(INDEX_PATIENT, patient);
        this.startActivityForResult(intent, FORM_VIEW_ACTIVITY_RESULT);
    }


    private static class PatientSummaryActivityMetadata {
        int recommendedForms;
        int incompleteForms;
        int completeForms;
        int newNotifications;
        int totalNotifications;
        int observations;
        int encounters;
        int reports;
    }

    class BackgroundQueryTask extends AsyncTask<Void, Void, PatientSummaryActivityMetadata> {

        @Override
        protected PatientSummaryActivityMetadata doInBackground(Void... voids) {
            MuzimaApplication muzimaApplication = (MuzimaApplication) getApplication();
            PatientSummaryActivityMetadata patientSummaryActivityMetadata = new PatientSummaryActivityMetadata();
            FormController formController = muzimaApplication.getFormController();
            NotificationController notificationController = muzimaApplication.getNotificationController();
            ObservationController observationController = muzimaApplication.getObservationController();
            EncounterController encounterController = muzimaApplication.getEncounterController();
            PatientReportController reportController = muzimaApplication.getPatientReportController();

            try {
                patientSummaryActivityMetadata.recommendedForms = formController.getRecommendedFormsCount();
                patientSummaryActivityMetadata.completeForms = formController.getCompleteFormsCountForPatient(patient.getUuid());
                patientSummaryActivityMetadata.incompleteForms = formController.getIncompleteFormsCountForPatient(patient.getUuid());
                patientSummaryActivityMetadata.observations = observationController.getObservationsCountByPatient(patient.getUuid());
                patientSummaryActivityMetadata.encounters = encounterController.getEncountersCountByPatient(patient.getUuid());
                patientSummaryActivityMetadata.reports = reportController.getPatientReportCountByPatientUuid(patient.getUuid());
                User authenticatedUser = ((MuzimaApplication) getApplicationContext()).getAuthenticatedUser();
                if (authenticatedUser != null) {
                    patientSummaryActivityMetadata.newNotifications =
                            notificationController.getNotificationsCountForPatient(patient.getUuid(), authenticatedUser.getPerson().getUuid(),
                                    Constants.NotificationStatusConstants.NOTIFICATION_UNREAD);
                    patientSummaryActivityMetadata.totalNotifications =
                            notificationController.getNotificationsCountForPatient(patient.getUuid(), authenticatedUser.getPerson().getUuid(), null);
                } else {
                    patientSummaryActivityMetadata.newNotifications = 0;
                    patientSummaryActivityMetadata.totalNotifications = 0;
                }
            } catch (FormController.FormFetchException e) {
                Log.w(getClass().getSimpleName(), "FormFetchException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (NotificationController.NotificationFetchException e) {
                Log.w(getClass().getSimpleName(), "NotificationFetchException occurred while fetching metadata in MainActivityBackgroundTask", e);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return patientSummaryActivityMetadata;
        }

        @Override
        protected void onPostExecute(PatientSummaryActivityMetadata patientSummaryActivityMetadata) {

        }
    }

    private void executeBackgroundTask() {
        mBackgroundQueryTask = new BackgroundQueryTask();
        mBackgroundQueryTask.execute();
    }

    private void prepareWriteToCardOptionDialog(Context context) {

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = layoutInflater.inflate(R.layout.write_to_card_option_dialog_layout, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PatientSummaryActivity.this);

        writeSHRDataOptionDialog = alertBuilder
                .setView(dialogView)
                .create();

        writeSHRDataOptionDialog.setCancelable(true);
        searchDialogTextView = dialogView.findViewById(R.id.patent_dialog_message_textview);
        yesOptionSHRSearchButton = dialogView.findViewById(R.id.yes_SHR_search_dialog);
        noOptionSHRSearchButton = dialogView.findViewById(R.id.no_SHR_search_dialog);
        searchDialogTextView.setText(R.string.hint_write_SHR_to_card);

        yesOptionSHRSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateSHRWriteToCard();
            }

        });

        noOptionSHRSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissWriteSHRDataDialogue();
            }
        });
    }

    private void prepareNonSHRWriteToCardOptionDialog(Context context) {

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = layoutInflater.inflate(R.layout.write_to_card_option_dialog_layout, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PatientSummaryActivity.this);

        writeSHRDataOptionDialog = alertBuilder
                .setView(dialogView)
                .create();

        writeSHRDataOptionDialog.setCancelable(true);
        searchDialogTextView = dialogView.findViewById(R.id.patent_dialog_message_textview);
        yesOptionSHRSearchButton = dialogView.findViewById(R.id.yes_SHR_search_dialog);
        noOptionSHRSearchButton = dialogView.findViewById(R.id.no_SHR_search_dialog);
        searchDialogTextView.setText(getString(R.string.hint_create_new_SHR));

        yesOptionSHRSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(defaultLocation == null){
                    try {
                        defaultLocation = LocationUtils.getDefaultEncounterLocationPreference(muzimaApplication);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(),"Could not determine default location",e);
                    }
                }

                if(defaultLocation != null) {
                    registerNewSHRRecord();
                } else {
                    dismissWriteSHRDataDialogue();

                    AlertDialog alertDialog = new AlertDialog.Builder(PatientSummaryActivity.this).create();
                    alertDialog.setTitle(getString(R.string.general_requirement));
                    alertDialog.setMessage(getString(R.string.hint_set_default_encounter_location_for_SHR));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.general_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }

        });

        noOptionSHRSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialogTextView.setText("");
                dismissWriteSHRDataDialogue();
            }
        });
    }

    private void dismissWriteSHRDataDialogue(){
        writeSHRDataOptionDialog.cancel();
        writeSHRDataOptionDialog.dismiss();
    }

    private void readSmartCard(){
        SmartCardIntentIntegrator SHRIntegrator = new SmartCardIntentIntegrator(this);
        SHRIntegrator.initiateCardRead();
        Toast.makeText(getApplicationContext(), R.string.hint_opening_card_reader, Toast.LENGTH_LONG).show();
    }

    private void processSmartCardReadResult(int requestCode, int resultCode, Intent dataIntent) {
        dismissWriteSHRDataDialogue();
        SmartCardIntentResult cardReadIntentResult = null;

        try {
            cardReadIntentResult = SmartCardIntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Could not get result", e);
        }

        if (cardReadIntentResult == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.failure_reading_card), Toast.LENGTH_LONG).show();
            return;
        }

        if (cardReadIntentResult.isSuccessResult()) {
            SmartCardRecord newSmartCardRecord = cardReadIntentResult.getSmartCardRecord();
            if (newSmartCardRecord != null) {
                String SHRPayload = newSmartCardRecord.getPlainPayload();
                if(!StringUtils.isEmpty(SHRPayload)) {
                    try {
                        KenyaEmrSHRModel kenyaEmrSHRModel = KenyaEmrShrMapper.createSHRModelFromJson(SHRPayload);
                        if (kenyaEmrSHRModel != null) {
                            if(kenyaEmrSHRModel.isNewSHRModel()){
                                InternalPatientId SHRInternalPatientId = kenyaEmrSHRModel.getPatientIdentification()
                                        .getInternalPatientIdByIdentifierType(Constants.Shr.KenyaEmr.PersonIdentifierType.CARD_SERIAL_NUMBER.shr_name);
                                if(SHRInternalPatientId != null && !StringUtils.isEmpty(SHRInternalPatientId.getID())){
                                    //ToDo: check whether card serial number already assigned
                                    registerNewSHRRecord(SHRInternalPatientId.getID());
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.hint_card_blank), Toast.LENGTH_LONG).show();
                                    registerNewSHRRecord();
                                }

                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(PatientSummaryActivity.this).create();
                                alertDialog.setTitle("Error");
                                alertDialog.setMessage(getString(R.string.hint_card_not_empty));
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.general_ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), getString(R.string.failure_obtaining_card_serial_number), Toast.LENGTH_LONG).show();
                        }
                    } catch (KenyaEmrShrMapper.ShrParseException e) {
                        Log.e(getClass().getSimpleName(), "EMR Error ", e);
                    }
                }
            }
        } else {
            Snackbar.make(findViewById(R.id.client_summary_view), "Card read failed." + cardReadIntentResult.getErrors(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.general_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            readSmartCard();
                        }
                    })
                    .show();
        }
    }
    private void registerNewSHRRecord(final String cardSerialNumber) {

        try {
            KenyaEmrSHRModel kenyaEmrSHRModel = KenyaEmrShrMapper.createInitialSHRModelForPatient(muzimaApplication, patient, cardSerialNumber);
            String jsonSHRModel = KenyaEmrShrMapper.createJsonFromSHRModel(kenyaEmrSHRModel);

            if (jsonSHRModel != null) {

                SmartCardRecord smartCardRecord = new SmartCardRecord();
                smartCardRecord.setPlainPayload(jsonSHRModel);
                smartCardRecord.setPersonUuid(patient.getUuid());
                smartCardRecord.setUuid(UUID.randomUUID().toString());
                smartCardRecord.setType(Constants.Shr.KenyaEmr.SMART_CARD_RECORD_TYPE);

                smartCardController.saveSmartCardRecord(smartCardRecord);

                Toast.makeText(getApplicationContext(), "SHR has been Recorded.", Toast.LENGTH_LONG).show();

                //create identifier with card serial number
                KenyaEmrShrMapper.updatePatientDemographicsWithCardSerialNumberAsIdentifier(muzimaApplication,patient,cardSerialNumber);
                //refresh UI
                dismissWriteSHRDataDialogue();
                recreate();

                //write SHR to card
                //initiateSHRWriteToCard();
            } else {
                Snackbar.make(findViewById(R.id.client_summary_view), "", Snackbar.LENGTH_LONG)
                        .setAction(R.string.general_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                registerNewSHRRecord(cardSerialNumber);
                            }
                        });
            }


        } catch (KenyaEmrShrMapper.ShrParseException e) {
            writeSHRDataOptionDialog.cancel();
            writeSHRDataOptionDialog.dismiss();
            Snackbar.make(findViewById(R.id.client_summary_view),"Unexpected Error Occured "+e.getMessage(),Snackbar.LENGTH_LONG)
                    .setAction(R.string.general_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            registerNewSHRRecord(cardSerialNumber);
                        }
                    })
                    .show();
        } catch (SmartCardController.SmartCardRecordSaveException e) {
            Log.e(getClass().getSimpleName(),""+e.getMessage());
        }
    }
    private void registerNewSHRRecord() {

        try {
            KenyaEmrSHRModel kenyaEmrSHRModel = KenyaEmrShrMapper.createInitialSHRModelForPatient(muzimaApplication, patient);
            String jsonSHRModel = KenyaEmrShrMapper.createJsonFromSHRModel(kenyaEmrSHRModel);

            if (jsonSHRModel != null) {

                SmartCardRecord smartCardRecord = new SmartCardRecord();
                smartCardRecord.setPlainPayload(jsonSHRModel);
                smartCardRecord.setPersonUuid(patient.getUuid());
                smartCardRecord.setUuid(UUID.randomUUID().toString());
                smartCardRecord.setType(Constants.Shr.KenyaEmr.SMART_CARD_RECORD_TYPE);

                smartCardController.saveSmartCardRecord(smartCardRecord);

                Toast.makeText(getApplicationContext(), "SHR has been Recorded.", Toast.LENGTH_LONG).show();

                //create identifier with card serial number
                //KenyaEmrShrMapper.updatePatientDemographicsWithCardSerialNumberAsIdentifier(muzimaApplication,patient,cardSerialNumber);
                //refresh UI
                dismissWriteSHRDataDialogue();
                recreate();

                //write SHR to card
                // initiateSHRWriteToCard();
            } else {
                Snackbar.make(findViewById(R.id.client_summary_view), "", Snackbar.LENGTH_LONG)
                        .setAction(R.string.general_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                registerNewSHRRecord();
                            }
                        });
            }


        } catch (KenyaEmrShrMapper.ShrParseException e) {
            writeSHRDataOptionDialog.cancel();
            writeSHRDataOptionDialog.dismiss();
            Snackbar.make(findViewById(R.id.client_summary_view),getString(R.string.general_unexpected_error_occurred)+e.getMessage(),Snackbar.LENGTH_LONG)
                    .setAction(R.string.general_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            registerNewSHRRecord();
                        }
                    })
                    .show();
        } catch (SmartCardController.SmartCardRecordSaveException e) {
            e.printStackTrace();
        }
    }

    private void handleSHREnabledChanged(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(muzimaApplication.getApplicationContext());
        boolean isPreferenceSHREnabled = preferences.getBoolean(muzimaApplication.getResources().getString(R.string.preference_enable_shr_key),PatientSummaryActivity.DEFAULT_SHR_STATUS);
        if (isSHREnabled != isPreferenceSHREnabled) {
            isSHREnabled = isPreferenceSHREnabled;
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            setSHRLayoutVisibility();
        }
    }

    private void setSHREnabled(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(muzimaApplication.getApplicationContext());
        isSHREnabled = preferences.getBoolean(muzimaApplication.getResources().getString(R.string.preference_enable_shr_key),PatientSummaryActivity.DEFAULT_SHR_STATUS);
    }

    private void setSHRLayoutVisibility(){
//        LinearLayout SHRLinearLayout=(LinearLayout)findViewById(R.id.SHR_linear_layout);
//        if(isSHREnabled) {
//            SHRLinearLayout.setVisibility(LinearLayout.VISIBLE);
//        } else {
//            SHRLinearLayout.setVisibility(LinearLayout.GONE);
//        }
    }

    private void setClinicalSummaryVisibility(){
//        LinearLayout clinicalSummaryLinearLayout = findViewById(R.id.client_summary_layout);
//        boolean isClinicalSummaryEnabled = muzimaApplication.getMuzimaSettingController().isClinicalSummaryEnabled();
//        if(isClinicalSummaryEnabled){
//            clinicalSummaryLinearLayout.setVisibility(LinearLayout.VISIBLE);
//        }else{
//            clinicalSummaryLinearLayout.setVisibility(LinearLayout.GONE);
//        }
    }

    private void setRelationshipEnabled(){
        isRelationshipEnabled = muzimaApplication.getMuzimaSettingController().isRelationshipEnabled();
    }

    private boolean isGeoMappingFeatureEnabled() {
        return muzimaApplication.getMuzimaSettingController().isGeoMappingEnabled();
    }

    private void setupPatientRelationships() {
        lvwPatientRelationships = findViewById(R.id.relationships_list);
        lvwPatientRelationships.setVisibility(View.VISIBLE);
        patientRelationshipsAdapter = new RelationshipsAdapter(this, R.layout.item_relationship, relationshipController,
                patient.getUuid(), patientController);
        patientRelationshipsAdapter.setBackgroundListQueryTaskListener(this);

        lvwPatientRelationships.setAdapter(patientRelationshipsAdapter);
        lvwPatientRelationships.setClickable(true);
        lvwPatientRelationships.setLongClickable(true);
        lvwPatientRelationships.setEmptyView(noDataView);
        lvwPatientRelationships.setTooltipText("List of Patient's relationships!");
        lvwPatientRelationships.setOnItemClickListener(listOnClickListener());
        lvwPatientRelationships.setOnItemLongClickListener(listOnLongClickListener());
    }

    private void setupNoDataView() {
        noDataView = findViewById(R.id.no_data_layout);
        TextView noDataMsgTextView = findViewById(R.id.no_data_msg);
        noDataMsgTextView.setText(getResources().getText(R.string.info_relationships_unavailable));
        noDataMsgTextView.setTypeface(Fonts.roboto_bold_condensed(this));
    }

    private void setupStillLoadingView() {
        noDataView = findViewById(R.id.no_data_layout);
        TextView noDataMsgTextView = findViewById(R.id.no_data_msg);
        noDataMsgTextView.setText(R.string.general_loading_relationships);
        noDataMsgTextView.setTypeface(Fonts.roboto_bold_condensed(this));
    }

    @Override
    public void onQueryTaskStarted() {}

    @Override
    public void onQueryTaskFinish() {
        if (patientRelationshipsAdapter.isEmpty())
            setupNoDataView();
    }

    @Override
    public void onQueryTaskCancelled() {}

    @Override
    public void onQueryTaskCancelled(Object errorDefinition) {};
    private AdapterView.OnItemClickListener listOnClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Relationship relationship = (Relationship) parent.getItemAtPosition(position);

                if (actionModeActive) {
                    if (!relationship.getSynced()) {
                        int selectedRelationshipsCount = getSelectedRelationships().size();
                        if (selectedRelationshipsCount == 0 && actionModeActive)
                            actionMode.finish();
                        else
                            actionMode.setTitle(String.valueOf(selectedRelationshipsCount));
                    } else {
                        Toasty.warning(PatientSummaryActivity.this, getApplicationContext().getString(R.string.relationship_delete_fail), Toast.LENGTH_SHORT, true).show();
                        lvwPatientRelationships.setItemChecked(position, false);
                    }
                } else {

                    Patient relatedPerson;
                    try {
                        selectedRelatedPerson = null;
                        if (StringUtils.equals(relationship.getPersonA().getUuid(), patient.getUuid()))
                            relatedPerson = patientController.getPatientByUuid(relationship.getPersonB().getUuid());
                        else
                            relatedPerson = patientController.getPatientByUuid(relationship.getPersonA().getUuid());

                        if (relatedPerson != null) {
                            Intent intent = new Intent(PatientSummaryActivity.this, PatientSummaryActivity.class);

                            intent.putExtra(PatientSummaryActivity.PATIENT, relatedPerson);
                            startActivity(intent);
                        } else {
                            // We pick the right related person and create them as a patient
                            if (StringUtils.equalsIgnoreCase(patient.getUuid(), relationship.getPersonA().getUuid())) {
                                selectedRelatedPerson = relationship.getPersonB();
                            } else {
                                selectedRelatedPerson = relationship.getPersonA();
                            }
                            selectAction();
                        }
                    } catch (PatientController.PatientLoadException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private AdapterView.OnItemLongClickListener listOnLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!actionModeActive) {
                    Relationship relationship = (Relationship) parent.getItemAtPosition(position);

                    if (!relationship.getSynced()) {
                        //actionMode = startActionMode(new RelationshipsListActivity.DeleteRelationshipsActionModeCallback());
                        actionModeActive = true;

                        lvwPatientRelationships.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        lvwPatientRelationships.setItemChecked(position, true);
                        actionMode.setTitle(String.valueOf(getSelectedRelationships().size()));
                    } else {
                        Toasty.warning(PatientSummaryActivity.this, getApplicationContext().getString(R.string.relationship_delete_fail), Toast.LENGTH_SHORT, true).show();
                        lvwPatientRelationships.setItemChecked(position, false);
                    }
                }
                return true;
            }
        };
    }

    private List<Relationship> getSelectedRelationships() {
        List<Relationship> relationships = new ArrayList<>();
        SparseBooleanArray checkedItemPositions = lvwPatientRelationships.getCheckedItemPositions();
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            if (checkedItemPositions.valueAt(i)) {
                relationships.add(((Relationship) lvwPatientRelationships.getItemAtPosition(checkedItemPositions.keyAt(i))));
            }
        }
        return relationships;
    }

    private void selectAction(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(PatientSummaryActivity.this);
        builderSingle.setIcon(R.drawable.ic_accept);
        builderSingle.setTitle(R.string.hint_person_action_prompt);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PatientSummaryActivity.this, android.R.layout.simple_selectable_list_item);
        arrayAdapter.add(getString(R.string.info_convert_person_to_patient));
        arrayAdapter.add(getString(R.string.info_update_person_demographics));

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                if(getString(R.string.info_convert_person_to_patient).equals(strName)){
                    showAlertDialog();
                } else {
                    OpenUpdatePersonDemographicsForm();
                }
            }
        });
        builderSingle.show();
    }

    private void OpenUpdatePersonDemographicsForm() {
        Intent intent = new Intent(this, PersonDemographicsUpdateFormsActivity.class);
        intent.putExtra(PersonDemographicsUpdateFormsActivity.PERSON, selectedRelatedPerson);
        intent.putExtra(INDEX_PATIENT, patient);
        startActivity(intent);
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(ThemeUtils.getIconWarning(this))
                .setTitle(getResources().getString(R.string.title_logout_confirm))
                .setMessage(getResources().getString(R.string.confirm_create_patient_from_person))
                .setPositiveButton(getString(R.string.general_yes), positiveClickListener())
                .setNegativeButton(getString(R.string.general_no), null)
                .create()
                .show();
    }

    private Dialog.OnClickListener positiveClickListener() {
        return new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createPatientFromRelatedPerson();
            }
        };
    }

    private void createPatientFromRelatedPerson() {
        Intent intent = new Intent(this, RegistrationFormsActivity.class);
        Patient pat = new Patient();
        pat.setUuid(selectedRelatedPerson.getUuid());
        pat.setBirthdate(selectedRelatedPerson.getBirthdate());
        pat.setBirthdateEstimated(selectedRelatedPerson.getBirthdateEstimated());
        pat.setGender(selectedRelatedPerson.getGender());
        pat.setNames(selectedRelatedPerson.getNames());

        intent.putExtra(PatientSummaryActivity.PATIENT, pat);
        intent.putExtra(INDEX_PATIENT, patient);
        startActivity(intent);
    }

    private boolean isContactsListingEnabled(final MuzimaApplication muzimaApplication) {

        if (muzimaApplication==null) {
            return false;
        }

        try {
            List<CohortMember> cohortMembers = muzimaApplication.getMuzimaContext().getCohortService().getCohortMembershipByPatient(patient);

            for(CohortMember cohortMember: cohortMembers){
                Cohort cohort = cohortMember.getCohort();
                if ("".equals(cohort.getUuid()) || "".equals(cohort.getUuid())) {
                    return true;
                }
            }

        }
        catch (java.io.IOException e) {
            Log.e("", "", e);
        }

        return false;
    }

}
