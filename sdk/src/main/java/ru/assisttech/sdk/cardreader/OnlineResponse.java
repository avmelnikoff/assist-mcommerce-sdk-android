package ru.assisttech.sdk.cardreader;

import ru.assisttech.sdk.AssistResult;

public class OnlineResponse {

    private AssistResult result;

    public OnlineResponse(AssistResult result) {
        this.result = result;
    }

    public AssistResult getData() {
        return result;
    }

    public String toString() {
        if (result == null)
            return "null";

        return "OnlineResponse:\n"
                + "  ApprovalCode: " + result.getApprovalCode() + "\n"
                + "    BillNumber: " + result.getBillNumber() + "\n"
                + "    OrderState: " + result.getOrderState() + "\n"
                + "    Extra info: " + result.getExtra() + "\n";
    }
}
