package ru.assisttech.sdk.processor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;
import java.util.Map.Entry;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import ru.assisttech.sdk.AssistMerchant;
import ru.assisttech.sdk.AssistPaymentData;
import ru.assisttech.sdk.AssistResult;
import ru.assisttech.sdk.AssistSDK;
import ru.assisttech.sdk.FieldName;
import ru.assisttech.sdk.cardreader.AssistCard;
import ru.assisttech.sdk.identification.InstallationInfo;
import ru.assisttech.sdk.identification.SystemInfo;
import ru.assisttech.sdk.network.AssistNetworkEngine.NetworkResponseProcessor;

import ru.assisttech.sdk.R;
import ru.assisttech.sdk.network.HttpResponse;

public class AssistWebProcessor extends AssistBaseProcessor {
	
	private static final String TAG = "AssistWebService";

	protected static final int SCAN_REQUEST_CODE = 3;
	private static final int NOT_FOUND = -1;

	private boolean useCamera;
	private Activity webViewActivity;
	private URL errorURL;
	private CardPageHandler cardPageHandler;

    private boolean resultParsingIsInProcess;

	public interface WebPageViewer {
        void fillCardData(String number, String month, String year);
	}

	public AssistWebProcessor(Context context, AssistProcessorEnvironment environment, boolean useCamera) {
		super(context, environment);
		this.useCamera = useCamera;
		cardPageHandler = new CardPageHandler();
	}

    public void lookForCardFields(URL page, WebPageViewer viewer) {

        cardPageHandler.setViewer(viewer);

        getNetEngine().loadAndProcessPage(
                page,
                new NetworkConnectionErrorListener(),
                cardPageHandler.getDetector()
        );
    }

    public void startCardScanning(Activity caller, WebPageViewer viewer) {

        cardPageHandler.setViewer(viewer);

        Intent scanIntent = new Intent(caller, CardIOActivity.class);
        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); 		// default: true
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); 			// default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); 	// default: false

        caller.startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
    }

	public void processActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SCAN_REQUEST_CODE:
			finishCardScanning(requestCode, data);
			break;
		}
	}

    public void parseResultPage(URL resultPageUrl) {
        if (!resultParsingIsInProcess) {
            resultParsingIsInProcess = true;
            getNetEngine().loadAndProcessPage(
                    resultPageUrl,
                    new NetworkConnectionErrorListener(),
                    new ResultPageProcessor()
            );
        }
    }

    public void parseErrorPage(URL errorPageUrl) {
        errorURL = errorPageUrl;
        getNetEngine().loadAndProcessPage(
                errorPageUrl,
                new NetworkConnectionErrorListener(),
                new ErrorPageProcessor()
        );
    }

    /**
     * Supposed to be called from WebViewActivity to interrupt payment process
     */
    public void stopPayment() {
        if (hasListener()) {
            getListener().onTerminated(getTransaction().getId());
        }
        finish();
    }
	
	void onServiceActivityCreated(Activity activity) {
		webViewActivity = activity;
        if (hasListener()) {
            getListener().onActivityCreated(activity);
        }
	}
	
	boolean useCamera() {
		return useCamera;
	}

	boolean isCardPageDetected() {
		return cardPageHandler.isPageDetected();
	}

    @Override
    protected void run() {
        WebViewActivity.setService(this);
        WebViewActivity.setIgnoreSslErrors(getNetEngine().isCustomSslCertificateUsed());
        getCaller().startActivity(new Intent(getCaller(), WebViewActivity.class));
    }

    @Override
    protected void terminate() {
        getNetEngine().stopTasks();
    }
	
	/**
	 * Web request assembling (HTTP protocol, URL encoded pairs)
	 */	
	String buildRequest() {

        AssistPaymentData data = getEnvironment().getData();
        AssistMerchant m = getEnvironment().getMerchant();
        SystemInfo sysInfo = getEnvironment().getPayEngine().getSystemInfo();
        InstallationInfo iInfo = getEnvironment().getPayEngine().getInstInfo();

		StringBuilder content = new StringBuilder();
        try {
    		Map<String, String> params = data.getFields();

            StringBuilder stringToSign = null;
            if (!params.containsKey(FieldName.Signature)) {
                stringToSign = new StringBuilder();
            }

			content.append(URLEncoder.encode("Merchant_ID", "UTF-8")).append("=");
			content.append(URLEncoder.encode(m.getID(), "UTF-8")).append("&");

            if (stringToSign != null) {
                stringToSign.append(m.getID()).append(";");
            }

            Log.d(TAG, "Request parameters:");
        	for (Entry<String, String> item: params.entrySet()) {
        		// Parameters to exclude from authorization request
				Log.d(TAG, item.getKey() + ": " + item.getValue());
        		if (!item.getKey().equals(FieldName.Shop)) {
	        		content.append(URLEncoder.encode(item.getKey(), "UTF-8")).append("=");
	        		content.append(URLEncoder.encode(item.getValue(), "UTF-8")).append("&");
        		}        		

        		if (stringToSign != null && isSignatureField(item.getKey())) {
        			stringToSign.append(item.getValue()).append(";");
        		}
        	}

            if (stringToSign != null) {
                stringToSign.deleteCharAt(stringToSign.lastIndexOf(";"));
                content.append(URLEncoder.encode(FieldName.Signature, "UTF-8")).append("=");
                content.append(URLEncoder.encode(
                        createSignature(data.getPrivateKey(), stringToSign.toString()), "UTF-8"))
                        .append("&");
            }
                       
            // Mobile application specific parameters
	        content.append(URLEncoder.encode(FieldName.Latitude, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.lattitude(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.Longitude, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.longitude(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.Device, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.device(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.DeviceUniqueID, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.fingerprint(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.ApplicationName, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(iInfo.appName(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.ApplicationVersion, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(iInfo.versionName(), "UTF-8")).append("&");

            content.append(URLEncoder.encode(FieldName.SDKVersion, "UTF-8")).append("=");
			content.append(URLEncoder.encode(AssistSDK.getSdkVersion(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.MacAddress, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.macAddress(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.AndroidID, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.androidID(), "UTF-8")).append("&");

	        content.append(URLEncoder.encode(FieldName.OsLanguage, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(sysInfo.language(), "UTF-8")).append("&");

            String regId = getEnvironment().getPayEngine().getRegistrationId();
	        content.append(URLEncoder.encode(FieldName.registration_id, "UTF-8")).append("=");
	        content.append(URLEncoder.encode(regId, "UTF-8")).append("&");

        	content.append(URLEncoder.encode("URL_RETURN_OK", "UTF-8")).append("=");
    		content.append(URLEncoder.encode(ResultPageProcessor.SUCCESS_URL, "UTF-8")).append("&");

        	content.append(URLEncoder.encode("URL_RETURN_NO", "UTF-8")).append("=");
    		content.append(URLEncoder.encode(ResultPageProcessor.DECLINE_URL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        return content.toString();
	}
	
	private boolean isSignatureField(String fieldName) {
		return (/*fieldName.equals(FieldName.Merchant_ID) ||*/
		   	    fieldName.equals(FieldName.OrderNumber) ||
		        fieldName.equals(FieldName.OrderAmount) ||
		        fieldName.equals(FieldName.OrderCurrency));
	}
	
	private String createSignature(PrivateKey key, String inputString) {
		String signatureB64 = null;
		
        if (key != null) {
        	try {
            	Signature sig = Signature.getInstance("MD5WithRSA");
	            sig.initSign(key);
	            sig.update(inputString.getBytes("UTF-8"));                       
		        // Encode signature to Base64 form
	            signatureB64 = Base64.encodeToString(sig.sign(), Base64.DEFAULT);		        
		        //Log.d(TAG, "Signature: " + signatureB64);
		        			        
        	} catch (NoSuchAlgorithmException|
                     InvalidKeyException|
                     SignatureException|
					 UnsupportedEncodingException e) {
        		e.printStackTrace();
	        }
        }      
        return ((signatureB64 != null) ? signatureB64 : "");
	}
		
	private void finishCardScanning(int requestCode, Intent data) {

        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {

                AssistCard assistCard = new AssistCard();

                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                assistCard.setPan(scanResult.cardNumber);

                if (scanResult.isExpiryValid()) {
                    assistCard.setExpireMonth(scanResult.expiryMonth);
                    assistCard.setExpireYear(scanResult.expiryYear);
                }
                cardPageHandler.fillInPage(assistCard);
            }
        }
	}				
	
	/**
	 * Parses web page for error description {@link NetworkResponseProcessor}
	 */
	private class ErrorPageProcessor implements NetworkResponseProcessor {

        private String errorInfo;

        @Override
		public void asyncProcessing(HttpResponse response) {
	   		try {
                // Parse HTML page
                Document doc = Jsoup.parse(response.getData());
                Element body = doc.body();
                Elements els = body.getAllElements();
                StringBuilder info = new StringBuilder();
                for (int i = 0; i < els.size(); i++) {
                    if (els.get(i).hasText()) {
                        info.append(els.get(i).text());
                        info.append(" ");
                    }
                }
                errorInfo = info.toString();
	   		} catch (NullPointerException e) {
	   			e.printStackTrace();
	   			errorInfo = buildMessage(R.string.response_checking_error, e.getMessage());
	   		}			    						    				
		}
		
		@Override
		public void syncPostProcessing() {				
			String resultInfo = "";
			String pageURL = errorURL.toString(); 
			int start = pageURL.indexOf("ErrorID=");
			if (start != NOT_FOUND) {
				start += ("ErrorID=").length();
				int end = pageURL.indexOf('&', start);
				if (end != NOT_FOUND) {
					resultInfo = pageURL.substring(start, end);
				} else {
					resultInfo = pageURL.substring(start);
				}
			}
			resultInfo += (": " + errorInfo);
            if (hasListener()) {
                getListener().onError(getTransaction().getId(), resultInfo);
            }
            finish();
		}
	}

	/**
	 * Tries to find out if there are card data fields on the page
	 */
	private class CardPageHandler {

		private CardFieldDetector detector = new CardFieldDetector();
		private boolean pageDetected;
        private WebPageViewer viewer;
        private AssistCard card;

        public CardFieldDetector getDetector() {
            return detector;
        }

        private void setPageDetected(boolean value) {
            pageDetected = value;
        }

		public boolean isPageDetected() {
			return pageDetected;
		}

        public void setViewer(WebPageViewer viewer) {
            this.viewer = viewer;
        }

        private WebPageViewer getViewer() {
            return viewer;
        }

        void fillInPage(AssistCard assistCard) {
            card = assistCard;
            viewer.fillCardData(assistCard.getPan(), assistCard.getExpireMonth(), assistCard.getExpireYear());
        }

        private AssistCard getCard () {
            return card;
        }

		/**
		 * Parses web page and finds out if there is card number field
		 */
		private class CardFieldDetector implements NetworkResponseProcessor {

			public static final String CARD_NUMBER = "CardNumber";
			private Document doc;
			private boolean isDetected;

			@Override
			public void asyncProcessing(HttpResponse response) {
                isDetected = false;
				try {
                    // Parse and check HTML page
					doc = Jsoup.parse(response.getData());
					Element cn = doc.getElementById(CARD_NUMBER);
					if (cn != null) {
                        isDetected = true;
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void syncPostProcessing() {
                setPageDetected(isDetected);
				if (isDetected) {
                    AssistCard card = getCard();
                    if (card == null) {
                        startCardScanning(webViewActivity, viewer);
                    } else {
                        getViewer().fillCardData(card.getPan(), card.getExpireMonth(), card.getExpireYear());
                    }
				}
			}
		}
	}

	/**
	 * Parses web page for payment status and additional information {@link NetworkResponseProcessor}
	 */
	private class ResultPageProcessor implements NetworkResponseProcessor {
		
		public static final String SUCCESS_URL = "http://succeed_payment.url/";
		public static final String DECLINE_URL = "http://declined_payment.url/";
		
		protected String info = null;
		protected String billNumber = null;
		protected boolean approved = true;
		protected boolean error = false;

		@Override
		public void asyncProcessing(HttpResponse response) {

			Log.d(TAG, "Result page: " + response);
 			// Parse HTML page			 
    		try{
				Document doc = Jsoup.parse(response.getData());
				Element body = doc.body();
				
				Elements i1results = body.getElementsByClass("Info1_result");
				if (!i1results.isEmpty()){
					Element inf = i1results.first();
					info = inf.text();
				}
				
				Elements forms = body.getElementsByClass("Form");
				if (!forms.isEmpty()){
					Element form = forms.first();					
					String data = form.toString();
					//Log.d(TAG, "data: " + data);
		    		int i = data.indexOf(SUCCESS_URL + "?billnumber=");
		    		if (i == NOT_FOUND) {
		    			approved = false;
		    			i = data.indexOf(DECLINE_URL + "?billnumber=");
		    			if (i != NOT_FOUND) {
							i += (DECLINE_URL + "?billnumber=").length();
						}
		    		} else {    			    
		    			i += (SUCCESS_URL + "?billnumber=").length();
		    		}
					
		    		if (i != NOT_FOUND) {
		    			int end = data.indexOf('&', i);
		    			if (end != NOT_FOUND) {
		    				billNumber = data.substring(i, end);
		    			}
		    		}    		    			    			    														
				}
    		} catch (NullPointerException e) {
    			e.printStackTrace();  	    								
				info = buildMessage(R.string.response_checking_error, e.getMessage());
				error = true;					
    		} finally {
    			if(info == null)
    				info = "";
    			if(billNumber == null)
    				billNumber = "";	    			
    		}	
		}
		
		@Override
		public void syncPostProcessing() {
			AssistResult result = new AssistResult();
    		if (!error) {
	    		if (approved) {
					result.setOrderState(AssistResult.OrderState.APPROVED);
	    		} else {
					result.setOrderState(AssistResult.OrderState.DECLINED);
	    		}
	    		result.setBillNumber(billNumber);
    		}	
    		result.setExtra(info);
            if (hasListener()) {
                getListener().onFinished(getTransaction().getId(), result);
            }
            finish();
            resultParsingIsInProcess = false;
		}
	}
	
	/**
	 * Concatenates two strings
	 */
	private String buildMessage(int stringId, String exceptionText){
		return getContext().getString(stringId) + ". (" + exceptionText + ")";
	}	
}
