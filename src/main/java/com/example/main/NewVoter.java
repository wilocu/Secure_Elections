package com.example.main;

public class NewVoter {
    private String fname;
    private String lname;
    private int age;
    private String email_addr;
    private boolean citizen;
    private boolean resident;
    private boolean felon;

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail_addr() {
        return email_addr;
    }

    public void setEmail_addr(String email_addr) {
        this.email_addr = email_addr;
    }

    public boolean isCitizen() {
        return citizen;
    }

    public void setCitizen(boolean citizen) {
        this.citizen = citizen;
    }

    public boolean isResident() {
        return resident;
    }

    public void setResident(boolean resident) {
        this.resident = resident;
    }

    public boolean isFelon() {
        return felon;
    }

    public void setFelon(boolean felon) {
        this.felon = felon;
    }
}
