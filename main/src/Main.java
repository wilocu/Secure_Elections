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
    // The DynamoDB ID of the current user.
    public String currentID = "";

    //Data writer to handle writing to DynamoDB
    public static DataWriter dataWriter = new DataWriter();

    public static void main(String[] args) {
        start();
    }

    public static void start() {
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
            System.out.println("Enter (0) to exit.\n" +
                    "Enter (1) to log in. \n" +
                    "Enter (2) to create an account.");

            int input = parseInt(scan.nextLine());
            if (input == 0) {
                return;
            } else if (input == 1) {
                boolean loginSuccessful = program.login();

                if (loginSuccessful) {
                    loggedInState(program);
                }

            } else if (input == 2) {
                program.createAccount();
            } else {
                System.out.println(input + " is not a valid input. If you need assistance," +
                        " type \'HELP\'.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void loggedInState(Main program) {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("You are logged in.\n" +
                    "-----------------------");
            System.out.println("Enter (0) to exit.\n" +
                    "Enter (1) to register for an election.\n" +
                    "Enter (2) to vote.\n" +
                    "Enter (3) to view your profile.\n" +
                    "Enter (4) to view results.\n" +
                    "Enter (5) to log out.\n");
            int input = parseInt(scan.nextLine());
            if (input == 0) {
                return;
            } else if (input == 1) {
                program.registerForElection();
            } else if (input == 2) {
                program.voting();
            } else if (input == 3) {
                program.profile();
            } else if (input == 4) {
                program.viewResult();
            } else if (input == 5) {
                System.out.println("Logging out...");
                start();
            } else {
                System.out.println(input + " is not a valid option.");
            }
        }
    }

    private void viewResult() {
        Scanner scan = new Scanner(System.in);
        System.out.println("----------------Results-------------------");
        dataWriter.viewElectionResults(this.currentID);
        System.out.println("Enter (1) to go back;");
        int input = parseInt(scan.nextLine());
        if (input == 1) {
            System.out.println("");
        } else {
            System.out.println(input + " is not a valid input. ");
        }
    }

    public boolean login() {
        boolean login = false;
        int tries = 0; //get 3 tries to log in
        System.out.println("----------------LOGIN-------------------");
        Scanner scan = new Scanner(System.in);
        while (tries < 3) {
            System.out.println("Enter your username: ");
            String username = scan.nextLine();
            System.out.println("Enter your password: ");
            String password = scan.nextLine();

            String accountID = dataWriter.readFromTable(username, password);
            if (accountID != null) {
                this.currentID = accountID;
                System.out.println("Logged in successfully.");
                System.out.println("Welcome " + username);
                login = true;
                return login;
            }

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
    public void createAccount() {
        String username = "";
        String password = "";
        boolean accountAlreadyExists = true;

        System.out.println("---------------CREATE ACCOUNT----------------");
        Scanner scan = new Scanner(System.in);

        while (accountAlreadyExists) {
            System.out.println("Enter a username for your account: ");
            username = scan.nextLine();

            System.out.println("Enter a password for your account: ");
            password = scan.nextLine();

            boolean passwordNotConfirmed = true;
            while (passwordNotConfirmed) {
                System.out.println("Confirm your password: ");
                String confirmPassword = scan.nextLine();
                if (confirmPassword.equals(password))
                    passwordNotConfirmed = false;
                else {
                    System.out.println("Passwords do not match. Try again? y/n");
                    if (scan.nextLine().equalsIgnoreCase("n"))
                        return;
                }
            }
            if (dataWriter.readFromTable(username)) {
                System.out.println("An account with that username already exists.");
                System.out.println("Try again? y/n");
                if (scan.nextLine().equalsIgnoreCase("n"))
                    return;
            } else
                accountAlreadyExists = false;

        }

        RegistrationNumber regNumber = new RegistrationNumber("000000001");

        String q1 = "What is the name of your hometown?";  //I just made up a question
        System.out.println("Security Question 1: " + q1);
        String q1Answer = scan.nextLine();
        SecurityQuestion question1 = new SecurityQuestion(q1, q1Answer);

        Date date = new Date();

        System.out.println("Congratulations! Your account was created successfully!");

        Registration newRegistration = voterRegistration();
        Account user1 = new Account(regNumber, username, password, question1, date, newRegistration.registrationSuccess);
        String newAccountID = dataWriter.writeToTable(user1);
        this.currentID = newAccountID;
        accounts.add(user1);
        newRegistration.registrationID = newAccountID;
        dataWriter.writeToTable(newRegistration);
        loggedInState(this);
    }

    public Registration voterRegistration() {
        while (true) {
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

            System.out.println("Are you a United States citizen? yes/no");
            String c = scan.nextLine();
            boolean citizen = false;
            //if person answers yes, return true, otherwise leave at false
            if (c.equalsIgnoreCase("yes") || c.equalsIgnoreCase("y")) {
                citizen = true;
            }

            //change logic so user inputs a date and we calculate it
            System.out.println("Have you lived in NY state for at least 1 month? yes/no");
            String r = scan.nextLine();
            boolean residency = false;
            if (r.equalsIgnoreCase("yes") || c.equalsIgnoreCase("y")) {
                residency = true;
            }

            System.out.println("Have you ever been convicted of a felony? yes/no");
            String f = scan.nextLine();
            boolean felon = false;
            if (f.equalsIgnoreCase("yes") || c.equalsIgnoreCase("y")) {
                felon = true;
            }

            System.out.println("You have entered: ");
            System.out.printf("- First Name: %s\n", fName);
            System.out.printf("- Last Name: %s\n", lName);
            System.out.printf("- Age: %d\n", age);
            System.out.printf("- Email: %s\n", email);
            System.out.printf("- Citizenship: %b\n", citizen);
            System.out.printf("- Resident of NY: %b\n", residency);
            System.out.printf("- Convicted of Felony: %b\n\n", felon);
            System.out.println("Are these answers correct? y/n");
            if (scan.nextLine().equalsIgnoreCase("y")) {
                Person person1 = new Person(fName, lName, email, citizen, age, residency, felon);
                Registration registration = new Registration(fName, lName, age, email, citizen, residency, felon);

                //persons get sorted into the voter or nonvoter lists
                if (person1.getAge() >= 18 && citizen && residency && !felon) {
                    Voter voter1 = new Voter(person1);
                    registration.registrationSuccess = true;
                    return registration;
                } else {
                    NonVoter nonVoter1 = new NonVoter(person1);
                    registration.registrationSuccess = false;
                    return registration;
                }
            }
        }
    }

    public void profile() {
        while (true) {
            System.out.println("----------------Profile---------------");
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter (0) to exit\n" +
                    "Enter (1) to change username.\n" +
                    "Enter (2) to change password.\n" +
                    "Enter (3) to view your requirements.\n" +
                    "Enter (4) to go back.\n");
            int input = parseInt(scan.nextLine());
            if (input == 0) {
                return;
            } else if (input == 1) {
                updateUsername();
            } else if (input == 2) {
                updatePassword();
            } else if (input == 3) {
                viewVoterReg();
            } else if (input == 4) {
                System.out.println("");
                return;
            } else {
                System.out.println(input + " is not a valid input. ");
            }
        }
    }

    public void viewVoterReg() {
        System.out.println("Your voter registration info: ");
        dataWriter.viewVoterRegistration(this.currentID);
    }

    public void updatePassword() {
        Scanner scan = new Scanner(System.in);
        String newPassword = "";
        while (true) {
            System.out.println("Enter a new password. Enter 'q' to cancel.");
            newPassword = scan.nextLine();
            if (newPassword.equalsIgnoreCase("q"))
                break;
            else {
                while (true) {
                    System.out.println("Re-enter password to confirm.");
                    String confirmPassword = scan.nextLine();
                    if (confirmPassword.equalsIgnoreCase("q")) {
                        System.out.println("Transaction Cancelled.");
                        return;
                    }
                    if (confirmPassword.equals(newPassword)) {
                        dataWriter.updatePassword(newPassword, this.currentID);
                        System.out.println("Password Changed!");
                        return;
                    } else
                        System.out.println("Passwords did not match. Try again, or enter 'q' to cancel.");
                }
            }
        }
    }

    public void updateUsername() {
        Scanner scan = new Scanner(System.in);
        String newName = "";
        while (true) {
            System.out.println("Enter a new username. Enter 'q' to cancel.");
            newName = scan.nextLine();
            if (newName.equalsIgnoreCase("q"))
                break;
            else {
                System.out.println("You have entered: " + newName);
                System.out.println("Is this correct? y/n");
                if (scan.nextLine().equalsIgnoreCase("y")) {
                    dataWriter.updateUsername(newName, this.currentID);
                    break;
                }
            }
        }
    }

    public void registerForElection() {
        System.out.println("----------------REGISTER FOR AN ELECTION---------------");
        ;
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the number of the election you want to register for:");
        System.out.println("Enter (1) to register for the Presidential 2024 Election\n" +
                "Enter (2) to go back\n");
        int input = parseInt(scan.nextLine());
        if (input == 1) {
            dataWriter.registerForElection(dataWriter.getElectionID("1"), this.currentID);
        } else if (input == 2) {
            return;
        } else {
            System.out.println(input + " is not a valid input. ");
        }
    }


    public void voting() {
        Scanner scan = new Scanner(System.in);
        System.out.println("----------------voting-------------------");
        dataWriter.printCandidates(this.currentID);
        int input = parseInt(scan.nextLine());
        if (input == 1 || input == 2) {
            System.out.println("Confirm your vote? y/n");
            String s = scan.next();
            if(s.equalsIgnoreCase("n")){
                voting();
            }else{
                // election id can be hardcoded for now. Need to change in future.
                dataWriter.castVote(this.currentID, dataWriter.getElectionID("1"), input);
            }
        }
        else {
            System.out.println("Invalid input");
            voting();
        }
        System.out.println("Your vote has bee counted");

    }
}