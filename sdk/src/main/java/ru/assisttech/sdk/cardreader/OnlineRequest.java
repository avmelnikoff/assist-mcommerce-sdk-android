package ru.assisttech.sdk.cardreader;

import android.os.Parcel;
import android.os.Parcelable;

public class OnlineRequest implements Parcelable {

    private AssistCard card;
    private TransactionType type;

    public enum TransactionType {
        EMV,
        MAGNETIC_STRIPE
    }

    public OnlineRequest(AssistCard card, TransactionType type) {
        this.card = card;
        this.type = type;
    }

    private OnlineRequest(Parcel in) {
        type = TransactionType.valueOf(in.readString());
        card = new AssistCard();
        card.read(in);
    }

    public AssistCard getCard() {
        return card;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type.toString());
        card.write(dest);
    }

    public static final Parcelable.Creator<OnlineRequest> CREATOR
            = new Parcelable.Creator<OnlineRequest>() {
        public OnlineRequest createFromParcel(Parcel in) {
            return new OnlineRequest(in);
        }

        public OnlineRequest[] newArray(int size) {
            return new OnlineRequest[size];
        }
    };
}
