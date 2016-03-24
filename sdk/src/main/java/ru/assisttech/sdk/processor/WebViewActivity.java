package ru.assisttech.sdk.processor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.net.MalformedURLException;
import java.net.URL;

import ru.assisttech.sdk.R;


public class WebViewActivity extends Activity implements AssistWebProcessor.WebPageViewer {

    private static final String TAG = "WebViewActivity";
    private static final int MENU_ITEM_ID = 5;

    private FrameLayout placeholder;    /* used for WebView placement and makes it possible to remove WebView from Activity during screen rotation */
    private WebView wv;
    private boolean enableMenu;

    private static AssistWebProcessor webService;

	private static boolean ignoreSslErrors;
	
	public static void setService(AssistWebProcessor service) {
		if (service != null) {
            webService = service;
        }
	}

	public static void setIgnoreSslErrors(boolean value) {
		ignoreSslErrors = value;
	}

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);   // Enable window progress bar
        initUI();
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (wv != null) {
            placeholder.removeView(wv);
        }
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        wv.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        wv.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webService.useCamera()) {
            String url = wv.getUrl();
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("pay.cfm") && webService.isCardPageDetected()){
                    enableMenu = true;
                    invalidateOptionsMenu();
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUI() {
        setContentView(R.layout.web_activity);
        placeholder = (FrameLayout) findViewById(R.id.web_fragment);

        if (wv == null) {
            wv = new WebView(this);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setLoadsImagesAutomatically(true);
            wv.getSettings().setUseWideViewPort(true);
            wv.getSettings().setLoadWithOverviewMode(true);
            wv.getSettings().setBuiltInZoomControls(true);
            wv.setWebViewClient(new PayWebViewClient());
            wv.setWebChromeClient(new PayWebChromeClient());
            postRequest();
        }
        wv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        placeholder.addView(wv);

        webService.onServiceActivityCreated(this);
    }

    @Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dlg_title_warning));
		builder.setMessage(getString(R.string.dlg_msg_stop_payment_question));
		builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
                webService.stopPayment();
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
		AlertDialog dlg = builder.create();
		dlg.show();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (enableMenu) {
            menu.add(Menu.NONE, MENU_ITEM_ID, Menu.NONE, R.string.scan_card);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ITEM_ID) {
            webService.startCardScanning(this, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        webService.processActivityResult(requestCode, resultCode, data);
    }

    public void postRequest() {
        String url = webService.getURL().toString();
        String content = webService.buildRequest();
        Log.d(TAG, "POST address: " + url);
        Log.d(TAG, "POST content: " + content);
        wv.postUrl(url, content.getBytes());
    }

    @Override
    public void fillCardData(String number, String month, String year) {
        String script = "function fillForm() {"
                + "    document.getElementById('CardNumber').value='" + number + "';"
                + "    var month = document.getElementById('ExpireMonth');"
                + "    for (var i = 0; i < month.options.length; i++) {"
                + "        if (month.options[i].value == '" + month + "') {"
                + "            month.selectedIndex = i;"
                + "            break;"
                + "        }"
                + "    }"
                + "    var year = document.getElementById('ExpireYear');"
                + "    for (var i = 0; i < year.options.length; i++) {"
                + "        if (year.options[i].value == '" + year + "') {"
                + "            year.selectedIndex = i;"
                + "            break;"
                + "        }"
                + "    }"
                + "};"
                + "fillForm();";
        wv.loadUrl("javascript:" + script);
        enableMenu = true;
        invalidateOptionsMenu();
    }

    class PayWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d(TAG, "Progress: " + Integer.toString(newProgress));
            super.onProgressChanged(view, newProgress);
        }
    }

    class PayWebViewClient extends WebViewClient {
        // Set webview to ignore SSL error for pages with certificates unknown for Android
        // We check certificates manually using HttpsURLConnection
        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            if (ignoreSslErrors ) {
                Log.e(TAG, "SSL Error ignored");
                handler.proceed();
            } else {
                Log.e(TAG, "SSL Error: " + error.toString());
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading(): " + url);
            if (url.contains("result.cfm")) {
                webService.parseResultPage(toURL(url));
                return true;
            } else if (url.contains("body.cfm")) {
                webService.parseErrorPage(toURL(url));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "PageStarted: " + url);
            setProgressBarIndeterminateVisibility(true);
            enableMenu = false;
            invalidateOptionsMenu();
            wv.setScrollX(0);
            wv.setScrollY(0);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "PageFinished: " + url);
            setProgressBarIndeterminateVisibility(false);

            if (url.contains("body.cfm")) {
                webService.parseErrorPage(toURL(url));
            } else if (webService.useCamera() && url.contains("pay.cfm")) {
                webService.lookForCardFields(toURL(url), WebViewActivity.this);
            }
        }
    }

    private URL toURL(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return u;
    }
}
