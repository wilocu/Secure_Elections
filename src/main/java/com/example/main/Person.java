package com.example.main;

public class Person {

    public String firstName;
    public String lastName;
    public String email;
    public Boolean citizenship;
    public int age;
    public Boolean residentForAMonth;
    public Boolean felony;

    public Person(String firstName, String lastName, String email, Boolean citizenship, int age, Boolean residentForAMonth, Boolean felony) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.citizenship = citizenship;
        this.age = age;
        this.residentForAMonth = residentForAMonth;
        this.felony = felony;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCitizenship(Boolean citizenship) {
        this.citizenship = citizenship;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setResidentForAMonth(Boolean residentForAMonth) {
        this.residentForAMonth = residentForAMonth;
    }

    public void setFelony(Boolean felony) {
        this.felony = felony;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getCitizenship() {
        return citizenship;
    }

    public int getAge() {
        return age;
    }

    public Boolean getResidentForAMonth() {
        return residentForAMonth;
    }

    public Boolean getFelony() {
        return felony;
    }
}
