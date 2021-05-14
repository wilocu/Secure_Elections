package com.example.main;

public class Registration {
    public String registrationID;
    public String fName;
    public String lName;
    public int age;
    public String email;
    public boolean citizenship;
    public boolean residency;
    public boolean felony;
    public boolean registrationSuccess;

    public Registration(String fName, String lName, int age, String email, boolean citizenship, boolean residency, boolean felony) {
        this.fName = fName;
        this.lName = lName;
        this.age = age;
        this.email = email;
        this.citizenship = citizenship;
        this.residency = residency;
        this.felony = felony;
    }

    public boolean isRegistrationSuccess() {
        return registrationSuccess;
    }

    public void setRegistrationSuccess(boolean registrationSuccess) {
        this.registrationSuccess = registrationSuccess;
    }

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCitizenship() {
        return citizenship;
    }

    public void setCitizenship(boolean citizenship) {
        this.citizenship = citizenship;
    }

    public boolean isResidency() {
        return residency;
    }

    public void setResidency(boolean residency) {
        this.residency = residency;
    }

    public boolean isFelony() {
        return felony;
    }

    public void setFelony(boolean felony) {
        this.felony = felony;
    }
}
