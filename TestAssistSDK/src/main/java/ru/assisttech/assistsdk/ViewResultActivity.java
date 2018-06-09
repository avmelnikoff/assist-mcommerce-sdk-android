package ru.assisttech.assistsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import ru.assisttech.sdk.storage.AssistTransaction;

public class ViewResultActivity extends Activity {

    public static final String TRANSACTION_ID_EXTRA = "transaction_id_extra";
	private long transactionID;

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

    private void initUI() {
        TextView tvResultOrderNumber = (TextView) findViewById(R.id.tvResultOrderNumber);
        TextView tvResultOrderComment = (TextView) findViewById(R.id.tvResultOrderComment);
        TextView tvResultOrderAmount = (TextView) findViewById(R.id.tvResultOrderAmount);
        TextView tvResultStatus = (TextView) findViewById(R.id.tvResultStatus);
        TextView tvResultInfo = (TextView) findViewById(R.id.tvResultExtraInfo);

        ApplicationConfiguration cfg = ApplicationConfiguration.getInstance();
        AssistTransaction transaction = cfg.getPaymentEngine().transactionStorage().getTransaction(transactionID);
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
}

