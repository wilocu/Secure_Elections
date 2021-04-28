package com.example.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;

import static java.lang.Integer.parseInt;

@SpringBootApplication
public class Main {

    //list of all the accounts
    public ArrayList<Account> accounts = new ArrayList<Account>();
    //list of all registered voters
    public ArrayList<Voter> voters = new ArrayList<Voter>();
    //list of all nonvoters
    public ArrayList<NonVoter> nonVoters = new ArrayList<NonVoter>();
    // The DynamoDB ID of the current user.
    public String currentID = "";

    //Data writer to handle writing to DynamoDB
    public static DataWriter dataWriter = new DataWriter();

    public static void main(String[] args) {
        JFrame quitProgramFrame = new JFrame("END PROGRAM");
        quitProgramFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        quitProgramFrame.setSize(400, 400);
        quitProgramFrame.setVisible(true);
        SpringApplication.run(Main.class, args);
    }
}