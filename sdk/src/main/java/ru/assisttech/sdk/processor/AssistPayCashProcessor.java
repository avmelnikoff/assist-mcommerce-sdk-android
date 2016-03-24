package ru.assisttech.sdk.processor;

import android.content.Context;
import android.os.AsyncTask;

import ru.assisttech.sdk.AssistResult;

/**
 * Dummy service for cash payments
 * Payments are not registered in Assist payment system
 */
public class AssistPayCashProcessor extends AssistBaseProcessor {

    public AssistPayCashProcessor(Context context, AssistProcessorEnvironment environment) {
        super(context, environment);
    }

    @Override
    public boolean isCashService() {
        return true;
    }

    @Override
    protected void run() {
        new PayCashDummyTask().execute();
    }

    @Override
    protected void terminate() {}

    private class PayCashDummyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...params) {
            try {
                Thread.sleep(100, 0);
            } catch (InterruptedException e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void value) {
            AssistResult result = new AssistResult(AssistResult.OrderState.APPROVED);
            if (hasListener()) {
                getListener().onFinished(getTransaction().getId(), result);
            }
            finish();
        }
    }
}
