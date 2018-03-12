package com.muzima.view.webviewapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.muzima.MuzimaApplication;
import com.muzima.R;
import com.muzima.api.model.Form;
import com.muzima.api.model.FormTemplate;
import com.muzima.controller.FormController;
import com.muzima.model.BaseForm;
import com.muzima.model.shr.SHRModel;
import com.muzima.utils.javascriptinterface.SharedHealthRecordViewerJavascriptInterface;
import com.muzima.utils.javascriptinterface.WebViewJavascriptInterface;
import com.muzima.utils.smartcard.SmartCardIntentIntegrator;
import com.muzima.utils.smartcard.SmartCardIntentResult;
import com.muzima.view.BroadcastListenerActivity;
import com.muzima.view.forms.AudioComponent;
import com.muzima.view.forms.BarCodeComponent;
import com.muzima.view.forms.ImagingComponent;
import com.muzima.view.forms.VideoComponent;
import com.muzima.view.progressdialog.MuzimaProgressDialog;

import static android.webkit.ConsoleMessage.MessageLevel.ERROR;
import static java.text.MessageFormat.format;

public class JavascriptAppWebViewActivitity extends BroadcastListenerActivity {
    private static final String TAG = JavascriptAppWebViewActivitity.class.getSimpleName();
    public static final String PATIENT = "patient";
    public static final String APP_INTERFACE = "appInterface";
    public static final String APP_TITLE = "appTitle";
    public static final String APP_SOURCE_FORM = "appSourceForm";

    private WebView webView;
    private MuzimaProgressDialog progressDialog;

    private SharedHealthRecordViewerJavascriptInterface webViewJavascriptInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar( );
        String appTitle = getIntent().getStringExtra(APP_TITLE);

        webViewJavascriptInterface = createWebViewJavascriptInterface();
        actionBar.setTitle(appTitle);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_form_webview);
        progressDialog = new MuzimaProgressDialog(this);

        showProgressBar(getString(R.string.hint_loading_progress));
        try {
            setupWebView( );
        } catch (Throwable t) {
            Log.e(TAG, t.getMessage( ), t);
        }
        super.onStart( );
    }

    protected SharedHealthRecordViewerJavascriptInterface createWebViewJavascriptInterface(){
        return new SharedHealthRecordViewerJavascriptInterface(this);
    }


    protected void setupWebView() throws FormController.FormFetchException{
        FormController formController = ((MuzimaApplication)getApplicationContext()).getFormController();
        BaseForm baseForm = (BaseForm)getIntent().getSerializableExtra(APP_SOURCE_FORM);
        FormTemplate formTemplate = formController.getFormTemplateByUuid(baseForm.getFormUuid());

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebChromeClient(createWebChromeClient( ));

        getSettings( ).setRenderPriority(WebSettings.RenderPriority.HIGH);
        getSettings( ).setJavaScriptEnabled(true);
        getSettings( ).setDatabaseEnabled(true);
        getSettings( ).setDomStorageEnabled(true);
        getSettings( ).setBuiltInZoomControls(true);
        webView.addJavascriptInterface(webViewJavascriptInterface, APP_INTERFACE);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <link rel=\"stylesheet\" href=\"css/onsen/onsenui.css\">\n" +
                "  <link rel=\"stylesheet\" href=\"css/onsen/onsen-css-components.min.css\">\n" +
                "  <script src=\"js/onsen/onsenui.min.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <ons-navigator swipeable id=\"myNavigator\" page=\"page1.html\"></ons-navigator>\n" +
                "\n" +
                "<template id=\"page1.html\">\n" +
                "  <ons-page id=\"page1\">\n" +
                "    <ons-toolbar>\n" +
                "      <div class=\"center\">Page 1</div>\n" +
                "    </ons-toolbar>\n" +
                "\n" +
                "    <p>This is the first page.</p>\n" +
                "\n" +
                "    <ons-button id=\"push-button\">Push page</ons-button>\n" +
                "  </ons-page>\n" +
                "</template>\n" +
                "\n" +
                "<template id=\"page2.html\">\n" +
                "  <ons-page id=\"page2\">\n" +
                "    <ons-toolbar>\n" +
                "      <div class=\"left\"><ons-back-button>Page 1</ons-back-button></div>\n" +
                "      <div class=\"center\"></div>\n" +
                "    </ons-toolbar>\n" +
                "\n" +
                "    <p>This is the second page.</p>\n" +
                "  </ons-page>\n" +
                "</template>\n" +
                "<script>\n" +
                "document.addEventListener('init', function(event) {\n" +
                "  var page = event.target;\n" +
                "\n" +
                "  if (page.id === 'page1') {\n" +
                "    page.querySelector('#push-button').onclick = function() {\n" +
                "      document.querySelector('#myNavigator').pushPage('page2.html', {data: {title: 'Page 2'}});\n" +
                "    };\n" +
                "  } else if (page.id === 'page2') {\n" +
                "    page.querySelector('ons-toolbar .center').innerHTML = page.data.title;\n" +
                "  }\n" +
                "});\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>\n";
        webView.loadDataWithBaseURL("file:///android_asset/www/forms/", html,//formTemplate.getHtml(),
                "text/html", "UTF-8", "");
    }

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient( ) {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                JavascriptAppWebViewActivitity.this.setProgress(progress * 1000);
                if (progress == 100) {
                    progressDialog.dismiss( );
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = format("Javascript Log. Message: {0}, lineNumber: {1}, sourceId, {2}", consoleMessage.message( ),
                        consoleMessage.lineNumber( ), consoleMessage.sourceId( ));
                if (consoleMessage.messageLevel( ) == ERROR) {
                    Log.e(TAG, message);
                } else {
                    Log.d(TAG, message);
                }
                return true;
            }
        };
    }

    private WebSettings getSettings() {
        return webView.getSettings( );
    }

    public void showProgressBar(final String message) {
        runOnUiThread(new Runnable( ) {
            public void run() {
                progressDialog.show(message);
            }
        });
    }

    public void loadUrl(String url){
        webView.loadUrl(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        try {
            Log.e(TAG,"onActivityResult:DDDDDDDDDDDDDDDD: "+requestCode);
            SmartCardIntentResult intentResult = SmartCardIntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
            if(intentResult.isSuccessResult()) {
                Log.e(TAG,"REQUEST SUCCESSFUL: ");
                if (SmartCardIntentIntegrator.isReadRequest(requestCode)) {
                    Log.e(TAG,"READ REQUEST SUCCESSFUL: ");
                    webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultSuccess(SHRModel.createJsonSHRModel(intentResult.getSHRModel()));
                } else if (SmartCardIntentIntegrator.isWriteRequest(requestCode)) {
                    Log.e(TAG,"WRITE REQUEST SUCCESSFUL: ");

                    webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultSuccess(SHRModel.createJsonSHRModel(intentResult.getSHRModel()));
                }
            } else {
                if(SmartCardIntentIntegrator.isReadRequest(requestCode)){
                    Log.e(TAG,"READ REQUEST NOT SUCCESSFUL: ");
                    webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultError(intentResult.getErrors());
                }
                if(SmartCardIntentIntegrator.isWriteRequest(requestCode)){
                    Log.e(TAG,"WRITE REQUEST NOT SUCCESSFUL: ");
                    webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultError(intentResult.getErrors());
                }
            }
        } catch (Exception e){
            Log.e(TAG,"Could not retrieve result: ",e);
            if(SmartCardIntentIntegrator.isReadRequest(requestCode)){
                webViewJavascriptInterface.onReadSharedHealthRecordFromCardActivityResultError("Could not read SHR record: "+e.getMessage());
            }
            if(SmartCardIntentIntegrator.isWriteRequest(requestCode)){
                webViewJavascriptInterface.onWriteSharedHealthRecordFromCardActivityResultError("Could not write SHR record: "+e.getMessage());
            }
        }
    }
}
