package com.example.main;

public class NewUser {
    private String confirm_username;
    private String confirm_pw;
    private String security_quest;

    public String getConfirm_username() {
        return confirm_username;
    }

    public void setConfirm_username(String confirm_username) {
        this.confirm_username = confirm_username;
    }

    public String getConfirm_pw() {
        return confirm_pw;
    }

    public void setConfirm_pw(String confirm_pw) {
        this.confirm_pw = confirm_pw;
    }

    public String getSecurity_quest() {
        return security_quest;
    }

    public void setSecurity_quest(String security_quest) {
        this.security_quest = security_quest;
    }
}
