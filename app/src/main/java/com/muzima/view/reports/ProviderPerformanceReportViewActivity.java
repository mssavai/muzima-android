
/*
 * Copyright (c) The Trustees of Indiana University, Moi University
 * and Vanderbilt University Medical Center. All Rights Reserved.
 *
 * This version of the code is licensed under the MPL 2.0 Open Source license
 * with additional health care disclaimer.
 * If the user is an entity intending to commercialize any application that uses
 *  this code in a for-profit venture,please contact the copyright holder.
 */

package com.muzima.view.reports;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.api.model.FormTemplate;
import com.muzima.controller.FormController;
import com.muzima.domain.Credentials;
import com.muzima.model.AvailableForm;
import com.muzima.utils.StringUtils;
import com.muzima.utils.javascriptinterface.FormDataJavascriptInterface;
import com.muzima.utils.javascriptinterface.PerformanceLoggingJavascriptInterface;
import com.muzima.view.progressdialog.MuzimaProgressDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static android.webkit.ConsoleMessage.MessageLevel.ERROR;
import static java.text.MessageFormat.format;


public class ProviderPerformanceReportViewActivity extends ProviderReportViewActivity {
    private MuzimaProgressDialog progressDialog;
    private FormTemplate reportTemplate;
    private WebView webView;
    private Stack<String > navigationStack;
    private Credentials credentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentials = new Credentials(this);

        setContentView(R.layout.activity_form_webview);
        progressDialog = new MuzimaProgressDialog(this);
        FormController formController = ((MuzimaApplication) getApplicationContext()).getFormController();
        try {
            AvailableForm availableForm = (AvailableForm)getIntent().getSerializableExtra(REPORT);
            reportTemplate = formController.getFormTemplateByUuid(availableForm.getFormUuid());
        } catch (FormController.FormFetchException e) {
            Log.e(getClass().getSimpleName(),"Could not obtain report template");
        }

        setUpNavigationStack();
        setupWebView();
    }

    private void setUpNavigationStack(){
        navigationStack = new Stack<>();
    }

    private void setupWebView() {
        webView = findViewById(R.id.webView);
        webView.setWebChromeClient(createWebChromeClient( ));

        webView.getSettings( ).setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings( ).setJavaScriptEnabled(true);
        webView.getSettings( ).setDatabaseEnabled(true);
        webView.getSettings( ).setDomStorageEnabled(true);
        webView.getSettings( ).setBuiltInZoomControls(false);

        webView.addJavascriptInterface(new FormDataJavascriptInterface((MuzimaApplication) getApplicationContext()),
                "formDataInterface");
        webView.addJavascriptInterface(new PerformanceLoggingJavascriptInterface(ProviderPerformanceReportViewActivity.this),
                "loggingInterface");
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        //webView.loadDataWithBaseURL("file:///android_asset/www/reports/", prePopulateData( ),
           //     "text/html", "UTF-8", "");
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setBlockNetworkLoads(false);

        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put("loading","true");
        webView.loadUrl("file:///android_asset/www/reports/pls/index.html");
    }

    public void loadLandingPage(){
        loadPage("main.html", false);
    }

    public void loadMapsPage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navigationStack.push("map");
                webView.loadUrl("file:///android_asset/www/reports/pls/map.html");
            }
        });
    }

    public boolean isCurrentPageMapsPage(){
        String currentPage = navigationStack.peek();
        return StringUtils.equals("map",currentPage);
    }

    public void reloadCurrentPage(){
        if (!navigationStack.isEmpty()) {
            boolean isCurrentPageMapsPage = isCurrentPageMapsPage();
            String currentPageId = navigationStack.pop();
            System.out.println("Reloading "+currentPageId);
            if(isCurrentPageMapsPage) {
                loadMapsPage();
            } else {
                loadPage(currentPageId, false);
            }
        }
    }

    public String getCurrentPage(){
        return navigationStack.peek();
    }

    public void loadLoginPage(){
        loadPage("login.html", false);
    }

    public void loadPage(final String pageId, boolean isBackNavigation){
        if(isBackNavigation || navigationStack.isEmpty() || !StringUtils.equals(pageId,navigationStack.peek())) {
            if(pageId.equals("main.html")){
                navigationStack.empty();
                navigationStack.removeAllElements();
            }
            try {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:document.loadPage('" + pageId + "')");
                    }
                });

                if (!isBackNavigation || navigationStack.isEmpty()) {
                    navigationStack.push(pageId);
                }
            } catch (Error e) {
                Log.e("ProviderReportsView", "Can't load page", e);
            } catch (Exception e) {
                Log.e("ProviderReportsView", "Can't load page", e);
            }
        }
    }

    public void popTopPage(){
        if(navigationStack.size()>0) {
            navigationStack.pop();
        }
    }
    public boolean navigateToPreviousPage(){
        boolean canNavigateToPreviousPage = false;
        if(navigationStack.size()>1){

            canNavigateToPreviousPage = true;

            if(isCurrentPageMapsPage()){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupWebView();
                    }
                });
            } else {
                navigationStack.pop();
                String backPage = navigationStack.peek();
                loadPage(backPage, true);
            }
        }
        return canNavigateToPreviousPage;
    }

    public void exit(){
        finish();
    }

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient() {
            boolean isLandingPageLoaded = false;

            @Override
            public void onProgressChanged(WebView view, int progress) {
                ProviderPerformanceReportViewActivity.this.setProgress(progress * 1000);
                if (progress == 100) {
                    progressDialog.dismiss( );

                    if(!isLandingPageLoaded) {
                        loadLandingPage();
                        isLandingPageLoaded = true;
                    }
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = format("Javascript Log. Message: {0}, lineNumber: {1}, sourceId, {2}", consoleMessage.message( ),
                        consoleMessage.lineNumber( ), consoleMessage.sourceId( ));
                if (consoleMessage.messageLevel( ) == ERROR) {
                    Log.e(getClass().getSimpleName(), message);
                } else {
                    Log.d(getClass().getSimpleName(), message);
                }
                return true;
            }
        };
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss( );
        }
        super.onDestroy( );
    }

    @Override
    public void onBackPressed(){
        if(!navigateToPreviousPage()){
            super.onBackPressed();
        }
    }

    public String getUsername(){
        return credentials.getUserName();
    }
}



