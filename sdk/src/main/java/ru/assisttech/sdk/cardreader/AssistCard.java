package ru.assisttech.sdk.cardreader;

import android.os.Parcel;

import java.util.Calendar;

public class AssistCard {

    private String pan;
    private String calrdholder;
    private String expireMonth;
    private String expireYear;
    private String cardType;
    private String track2data;
    private String emv;
    private String pinblock;
    private String issuebank;

    public String getPinblock() {
        return pinblock;
    }

    public void setPinblock(String pinblock) {
        this.pinblock = pinblock;
    }

    public String getIssuebank() {
        return issuebank;
    }

    public void setIssuebank(String issuebank) {
        this.issuebank = issuebank;
    }

    public String getCalrdholder() {
        return calrdholder;
    }

    public void setCalrdholder(String calrdholder) {
        this.calrdholder = calrdholder;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getEmv() {
        return emv;
    }

    public void setEmv(String emv) {
        this.emv = emv;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String month) {
        if((month != null) && (month.length() == 1))
            expireMonth = "0" + month;
        else
            expireMonth = month;
    }

    public void setExpireMonth(int month){
        setExpireMonth(String.valueOf(month));
    }

    public String getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(String expireYear) {
        if (expireYear.length() == 2) {
            Calendar calendar = Calendar.getInstance();
            int year = (calendar.get(Calendar.YEAR)/100)*100 + Integer.valueOf(expireYear);
            this.expireYear = String.valueOf(year);
        } else {
            this.expireYear = expireYear;
        }
    }

    public void setExpireYear(int year){
        setExpireYear(String.valueOf(year));
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getTrack2data() {
        return track2data;
    }

    public void setTrack2data(String track2data) {
        this.track2data = track2data;
    }

    public void read(Parcel in) {
        pan = in.readString();
        calrdholder = in.readString();
        expireMonth = in.readString();
        expireYear = in.readString();
        cardType = in.readString();
        track2data = in.readString();
        emv = in.readString();
        pinblock = in.readString();
        issuebank = in.readString();
    }

    public void write(Parcel out) {
        out.writeString(pan);
        out.writeString(calrdholder);
        out.writeString(expireMonth);
        out.writeString(expireYear);
        out.writeString(cardType);
        out.writeString(track2data);
        out.writeString(emv);
        out.writeString(pinblock);
        out.writeString(issuebank);
    }
}
