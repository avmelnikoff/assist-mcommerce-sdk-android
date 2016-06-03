package ru.assisttech.sdk.storage;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;

public class AssistTransactionsLoader extends CursorLoader {

    public static final String ARG_STRING_ORDER_NUMBER = "order_number";

    private AssistTransactionStorage storage;
    private Bundle bundle;

    public AssistTransactionsLoader(AssistTransactionStorage storage, Context context, Bundle bundle) {
        super(context);
        this.storage = storage;
        this.bundle = bundle;
    }

    @Override
    public Cursor loadInBackground() {
        if (bundle != null) {
            return storage.getDataWithNoFiltration(bundle.getString(ARG_STRING_ORDER_NUMBER));
        } else {
            return storage.getData();
        }
    }
}
