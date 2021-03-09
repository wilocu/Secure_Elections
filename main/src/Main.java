import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Main {

    //list of all the accounts
    public ArrayList<Account> accounts = new ArrayList<Account>();
    //list of all registered voters
    public ArrayList<Voter> voters = new ArrayList<Voter>();
    //list of all nonvoters
    public ArrayList<NonVoter> nonVoters = new ArrayList<NonVoter>();

    //Data writer to handle writing to DynamoDB
    public static DataWriter dataWriter = new DataWriter();

    public static void main(String[] args){
        try {
            Main program = new Main();

            /*
            //Practice accounts & voters & nonvoters for testing
            RegistrationNumber numA = new RegistrationNumber("005721006");
            SecurityQuestion sqA = new SecurityQuestion("What is your hometown?", "Rochester");
            Date dateA = new Date();
            Account accA = new Account(numA, "account1", "password1", sqA, dateA);
            program.accounts.add(accA);

            RegistrationNumber numB = new RegistrationNumber("000000050");
            SecurityQuestion sqB = new SecurityQuestion("What is your hometown?", "Buffalo");
            Date dateB = new Date();
            Account accB = new Account(numB, "account2", "password2", sqB, dateB);
            program.accounts.add(accB);

            //can vote
            Person personA = new Person("John", "Doe", "john123@gmail.com", true, 25, true, false);
            Voter voterA = new Voter(personA);
            program.voters.add(voterA);

            //cannot vote
            Person personB = new Person("Mary", "Sue", "mjs543@gmail.com", false, 18, true, false);
            NonVoter voterB = new NonVoter(personB);
            program.nonVoters.add(voterB);

            //can vote
            Person personC = new Person("Dude", "3", "rando987@gmail.com", true, 70, true, false);
            Voter voterC = new Voter(personC);
            program.voters.add(voterC);
            */

            Scanner scan = new Scanner(System.in);
            System.out.println("Election Portal!");
            System.out.println("Enter (1) to log in, or. \n" +
                "Enter (2) to create an account.");

            int input = parseInt(scan.nextLine());
            if (input == 1) {
                boolean loginSuccessful = program.login();

                if(loginSuccessful){
                    System.out.println("Enter (1) to register for an election.\n" +
                        "Enter (2) to vote.");
                    input = parseInt(scan.nextLine());
                    if(input == 1){
                        program.registerForElection();
                    }else if(input == 2){
                        program.voting();
                    }else{
                        System.out.println(input + " is not a valid option.");
                    }
                }

            } else if (input == 2) {
                program.createAccount();
            } else {
                System.out.println(input + " is not a valid input. If you need assistance," +
                    " type \'HELP\'.");
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public boolean login(){
        boolean login = false;
        int tries = 0; //get 3 tries to log in
        System.out.println("----------------LOGIN-------------------");
        Scanner scan = new Scanner(System.in);
        while(tries < 3) {
            System.out.println("Enter your username: ");
            String username = scan.nextLine();
            System.out.println("Enter your password: ");
            String password = scan.nextLine();

            if(dataWriter.readFromTable(username, password))
            {
                System.out.println("Logged in successfully.");
                System.out.println("Welcome " + username);
                login = true;
                return login;
            }

//            for (Account acc : accounts) {
//                if (username.equals(acc.getUsername()) && password.equals(acc.getPassword())) {
//                    System.out.println("Logged in successfully.");
//                    System.out.println("Welcome " + acc.getUsername());
//                    login = true;
//                    return login;
//                }
//            }

            tries += 1;
            System.out.println("This combination of Username and Password was not found. Please try again.");
        }
        System.out.println("You have reached the maximum number of login attempts." +
            "Please try again later.");
        return login;
    }

    /*
     * An account is created with Registration Number, username, password,
     * a Security Question, and date.
     */
    public void createAccount(){
        System.out.println("---------------CREATE ACCOUNT----------------");
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter a username for your account: ");
        String username = scan.nextLine();

        System.out.println("Enter a password for your account: ");
        String password = scan.nextLine();

        RegistrationNumber regNumber = new RegistrationNumber("000000001");

        String q1 = "What is the name of your hometown?";  //I just made up a question
        System.out.println("Security Question 1: " + q1);
        String q1Answer = scan.nextLine();
        SecurityQuestion question1 = new SecurityQuestion(q1, q1Answer);

        Date date = new Date();

        Account user1 = new Account(regNumber, username, password, question1, date);
        dataWriter.writeToTable(user1);
        accounts.add(user1);
        System.out.println("Congratulations! Your account was created successfully!");

        voterRegistration();

    }

    public void voterRegistration() {
        System.out.println("-------------VOTER REGISTRATION INFO-------------------");
        //Person person1 = new Person();
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter your first name: ");
        String fName = scan.nextLine();

        System.out.println("Enter your last name: ");
        String lName = scan.nextLine();

        //System.out.println("Enter your date of birth (DD/MM/YYYY): ");
        System.out.println("Enter your age: ");
        int age = parseInt(scan.nextLine());

        System.out.println("Enter your email address: ");
        String email = scan.nextLine();

        System.out.println("Are you a United States citizen?");
        String c = scan.nextLine();
        boolean citizen = false;
        //if person answers yes, return true, otherwise leave at false
        if(c.equalsIgnoreCase("yes")){
            citizen = true;
        }

        //change logic so user inputs a date and we calculate it
        System.out.println("Have you lived in NY state for at least 1 month?");
        String r = scan.nextLine();
        boolean residency = false;
        if(r.equalsIgnoreCase("yes")){
            residency = true;
        }

        System.out.println("Have you ever been convicted of a felony?");
        String f = scan.nextLine();
        boolean felon = false;
        if(f.equalsIgnoreCase("yes")){
            felon = true;
        }

        Person person1 = new Person(fName, lName, email, citizen, age, residency, felon);

        //persons get sorted into the voter or nonvoter lists
        if(person1.getAge() >= 18 && citizen && residency && !felon){
            Voter voter1 = new Voter(person1);
        }else{
            NonVoter nonVoter1 = new NonVoter(person1);
        }
    }

    public void registerForElection(){
        System.out.println("----------------REGISTER FOR AN ELECTION---------------");;
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the number of the election you want to register for:");
        System.out.println("Presidential 2024 Election .......... 1\n" +
                           "Other Election ...................... 2\n");
        int input = parseInt(scan.nextLine());
        if(input == 1){

        }else if(input == 2){

        }else{
            System.out.println(input + " is not a valid input. ");
        }
    }





    public void voting(){

    }

}
