package ru.assisttech.assistsdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import ru.assisttech.sdk.engine.AssistBasePayEngine;
import ru.assisttech.sdk.engine.PayEngineListener;
import ru.assisttech.sdk.storage.AssistTransaction;
import ru.assisttech.sdk.storage.AssistTransactionStorage;

public class TransDetailsActivity extends UpButtonActivity implements PayEngineListener {

    private TextView odv;
    private TextView onv;
    private TextView ocv;
    private TextView oav;
    private TextView osv;
    private TextView oiv;

    private AssistBasePayEngine engine;
    private AssistTransaction tr;

    private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trans_details);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        engine = cfg.getPaymentEngine();
        AssistTransactionStorage storage = engine.transactionStorage();
		
		odv = (TextView) findViewById(R.id.tvtransDetailsOrderDate);
		onv = (TextView) findViewById(R.id.tvtransDetailsOrderNo);
		ocv = (TextView) findViewById(R.id.tvtransDetailsOrderComment);
		oav = (TextView) findViewById(R.id.tvtransDetailsOrderAmount);
		osv = (TextView) findViewById(R.id.tvtransDetailsOrderStatus);
		oiv = (TextView) findViewById(R.id.tvtransDetailsOrderExtraInfo);

		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tr = storage.getTransaction(extras.getLong(TransactionsActivity.TRANS_ID));
            updateUI(tr);
        }

		findViewById(R.id.btnUpdateStatus).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                showProgress();
                engine.setEngineListener(TransDetailsActivity.this);
			    engine.getOrderResult(TransDetailsActivity.this, tr.getId());
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

    @Override
    public void onFinished(Activity activity, AssistTransaction assistTransaction) {
        updateUI(assistTransaction);
        hideProgress();
    }

    @Override
    public void onCanceled(Activity activity, AssistTransaction assistTransaction) {
    }

    @Override
    public void onFailure(Activity activity, String info) {
        hideProgress();
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkError(Activity activity, String message) {
        hideProgress();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateUI(AssistTransaction tr) {
        odv.setText(tr.getOrderDateDevice());
        onv.setText(tr.getOrderNumber());
        ocv.setText(tr.getOrderComment());
        oav.setText(tr.getOrderAmount() + " " + tr.getOrderCurrency());
        osv.setText(tr.getResult().getOrderState().toText());
        String extraInfo = tr.getResult().getExtra();
        if (TextUtils.isEmpty(extraInfo)) {
            oiv.setText("-");
        } else {
            oiv.setText(extraInfo);
        }
    }

    private void showProgress() {
        // Construct a progress dialog to prevent user from actions until connection is finished.
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(getString(R.string.please_wait));
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
