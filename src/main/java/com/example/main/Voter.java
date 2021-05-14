package com.example.main;

public class Voter {

    public Person person;

    public Voter(Person person) {
        this.person = person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
