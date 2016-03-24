package ru.assisttech.sdk;

public class AssistMerchant {

    private String id;
    private String login;
    private String password;

    public AssistMerchant(String id) {
        this.id = id;
    }

    public AssistMerchant(String id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public String getID() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
