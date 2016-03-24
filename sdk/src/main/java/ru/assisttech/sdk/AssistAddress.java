package ru.assisttech.sdk;

public class AssistAddress {

    public static final String DEFAULT_SERVER           = "https://payments.t.paysecure.ru";

    public static final String REGISTRATION_SERVICE     = "/registration/mobileregistration.cfm";
    public static final String WEB_SERVICE              = "/pay/order.cfm";

    public static final String XPOS_SERVICE             = "/xpos/payment.cfm";
    public static final String GET_ORDER_STATUS_SERVICE = "/orderresult/mobileorderresult.cfm";
    public static final String CANCEL_SERVICE 		     = "/cancel/wscancel.cfm";

    public static final String SILENTPAY_SERVICE        = "/pay/silentpay.cfm";
    public static final String GET_3DS_SERVICE          = "/get3dsec/ws3dsec.cfm";
}
