package ru.assisttech.assistsdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.assisttech.sdk.storage.AssistTransaction;

public class ViewResultActivity extends Activity {

    public static final String TAG = "ViewResultActivity";
    public static final String TRANSACTION_ID_EXTRA = "transaction_id_extra";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DEVICE_BT_ADDRESS = 2;

	private long transactionID;
    private ProgressDialog dialog;
    private AssistTransaction transaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		transactionID = getIntent().getLongExtra(TRANSACTION_ID_EXTRA, -1);
        initUI();
	}
	
	@Override
	public void onBackPressed() {
		toMainScreen();		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initUI() {

        TextView tvResultOrderNumber = (TextView) findViewById(R.id.tvResultOrderNumber);
        TextView tvResultOrderComment = (TextView) findViewById(R.id.tvResultOrderComment);
        TextView tvResultOrderAmount = (TextView) findViewById(R.id.tvResultOrderAmount);
        TextView tvResultStatus = (TextView) findViewById(R.id.tvResultStatus);
        TextView tvResultInfo = (TextView) findViewById(R.id.tvResultExtraInfo);

        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        transaction = cfg.getPaymentEngine().transactionStorage().getTransaction(transactionID);
        if(transaction != null){
            tvResultOrderNumber.setText(transaction.getOrderNumber());
            tvResultOrderComment.setText(transaction.getOrderComment());
            tvResultOrderAmount.setText(transaction.getOrderAmount() + " " + transaction.getOrderCurrency().toString());
            if(transaction.getResult() != null)
            {
                tvResultStatus.setText(transaction.getResult().getOrderState().toText());
                String extraInfo = transaction.getResult().getApprovalCode();
                if(extraInfo == null){
                    extraInfo = transaction.getResult().getExtra();
                }else{
                    extraInfo += ": ";
                    extraInfo += transaction.getResult().getExtra();
                }
                tvResultInfo.setText(extraInfo);

                if(transaction.getPaymentMethod() == AssistTransaction.PaymentMethod.CASH)
                {
                    TextView tvMode = (TextView) findViewById(R.id.tvResultPaymentMode);
                    tvMode.setText("Cash");
                }
            }
        }

        findViewById(R.id.btnResultClose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toMainScreen();
            }
        });
    }
	
	private void toMainScreen() {
		Intent intent = new Intent(ViewResultActivity.this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);		
	}

    private void enableBluetooth() {
        Log.d(TAG, "enableBluetooth()");
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgress() {
        showProgress(null);
    }

    private void showProgress(String message) {
        // Construct a progress dialog to prevent user from actions until connection is finished.
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            if (TextUtils.isEmpty(message)) {
                dialog.setMessage(getString(R.string.please_wait));
            } else {
                dialog.setMessage(message);
            }
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return true;
                }
            });
            dialog.show();
        }
    }

    private void hideProgress() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

}

