import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public class DataWriter {
    private AmazonDynamoDB dbClient;
    private DynamoDB dynamoDB;
    private BasicAWSCredentials awsCredentials;
    private String ACCOUNT_TABLE = "secure-elections-table";
    private String REGISTRATION_TABLE = "secure-elections-voters";
    private String ELECTIONS_TABLE = "secure-elections-election";

    public DataWriter() {
        this.awsCredentials = new BasicAWSCredentials("AKIA6L33Q4ZCSBIHY5VV", "tdU5bOVkkeKzDhV/hAxWgmONwYPbcGXdOX9SeMut");
        this.dbClient = AmazonDynamoDBClientBuilder.standard().
                        withRegion(Regions.US_EAST_1).
                        withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
        this.dynamoDB = new DynamoDB(dbClient);
    }

    /**
     * Helper function to check if a given username and password match an entry in the table.
     * Called from Main when logging in as a user.
     * @param username
     * @param password
     * @return
     */
    public String readFromTable(String username, String password){
        Map<String, String> attributeNames = new HashMap<>();
        attributeNames.put("#username", "username");
        attributeNames.put("#password", "password");

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":usernameValue", new AttributeValue().withS(username));
        attributeValues.put(":passwordValue", new AttributeValue().withS(password));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ACCOUNT_TABLE)
                .withFilterExpression("#username = :usernameValue AND #password = :passwordValue")
                .withExpressionAttributeNames(attributeNames)
                .withExpressionAttributeValues(attributeValues);

        ScanResult scanResult = dbClient.scan(scanRequest);
        if(scanResult.getItems().size() > 0)
            return scanResult.getItems().get(0).get("id").getS();

        return null;
    }

    /**
     * Helper function to determine whether a username exists in the table.
     * Called from Main when making a new account; We don't want duplicate usernames!
     * @param username
     * @return
     */
    public boolean readFromTable(String username) {
        Map<String, String> attributeNames = new HashMap<>();
        attributeNames.put("#username", "username");

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":usernameValue", new AttributeValue().withS(username));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ACCOUNT_TABLE)
                .withFilterExpression("#username = :usernameValue")
                .withExpressionAttributeNames(attributeNames)
                .withExpressionAttributeValues(attributeValues);

        ScanResult scanResult = dbClient.scan(scanRequest);

        if (scanResult.getItems().size() > 0)
            return true;

        return false;
    }

    public String getElectionID(String id){
        Map<String, String> attributeNames = new HashMap<>();
        attributeNames.put("#id", "id");

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":id", new AttributeValue().withS(id));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ELECTIONS_TABLE)
                .withFilterExpression("#id = :id")
                .withExpressionAttributeNames(attributeNames)
                .withExpressionAttributeValues(attributeValues);

        ScanResult scanResult = dbClient.scan(scanRequest);
        if(scanResult.getItems().size() > 0){
            return scanResult.getItems().get(0).get("electionID").getS();
        }

        return null;
    }

    /**
     * Given an account, write it to the database.
     * @param account
     */
    public String writeToTable(Account account){
        String accountID = UUID.randomUUID().toString();
        Item accountItem = new Item()
                .withString("id", accountID)
                .withString("number", account.number.id)
                .withString("username", account.username)
                .withString("password", account.password)
                .withString("question", account.question.question)
                .withString("answer", account.question.answer)
                .withString("dateOfCreation", account.dateOfCreation.toString())
                .withString("voter", Boolean.toString(account.voter));

        dynamoDB.getTable(ACCOUNT_TABLE).putItem(accountItem);
        return accountID;
    }

    public void writeToTable(Registration registration){
        Item registrationItem = new Item()
                .withString("id", registration.registrationID)
                .withString("fname", registration.fName)
                .withString("lname", registration.lName)
                .withInt("age", registration.age)
                .withString("email", registration.email)
                .withBoolean("citizen", registration.citizenship)
                .withBoolean("resident", registration.residency)
                .withBoolean("felon", registration.felony);
        dynamoDB.getTable(REGISTRATION_TABLE).putItem(registrationItem);
    }

    public void registerForElection(String electionID, String userID){
        Map<String, Boolean> electionsList = dynamoDB.getTable(ACCOUNT_TABLE).getItem("id", userID).getMap("elections");
        if(electionsList == null)
            electionsList = new HashMap<>();

        if(electionsList.containsKey(electionID))
            System.out.println("You have already registered for this election.");
        else{
            electionsList.put(electionID, false);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", userID)
                    .withUpdateExpression("set elections = :elections")
                    .withValueMap(new ValueMap().withMap(":elections", electionsList));
            dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
        }

    }

    public void updateUsername(String newName, String id){
        UpdateItemSpec updateItemSpec= new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("set username = :newname")
                .withValueMap(new ValueMap().withString(":newname", newName));
        dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
    }

    public void updatePassword(String newPassword, String id){
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("set password = :password")
                .withValueMap(new ValueMap().withString(":password", newPassword));
        dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
    }

    public void viewVoterRegistration(String id){
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(REGISTRATION_TABLE).getItem(getItemSpec);
        System.out.printf("- First Name: %s\n", item.get("fname").toString());
        System.out.printf("- Last Name: %s\n", item.get("lname").toString());
        System.out.printf("- Age: %s\n", item.get("age").toString());
        System.out.printf("- Email: %s\n", item.get("email").toString());
        System.out.printf("- United States Citizen: %s\n", item.get("citizen").toString());
        System.out.printf("- Resident of NY State: %s\n", item.get("resident").toString());
        System.out.printf("- Convicted of felony: %s\n\n", item.get("felon").toString());
    }

    public void viewElectionResults(String id){
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(ACCOUNT_TABLE).getItem(getItemSpec);
        Map<String, Boolean> electionMap = item.getMap("elections");
        String[] electionIDs = electionMap.keySet().toArray(new String[electionMap.size()]);
        for(int i = 0; i < electionIDs.length; i++){
            GetItemSpec getElectionSpec = new GetItemSpec().withPrimaryKey("electionID", electionIDs[i]);
            Item electionItem = dynamoDB.getTable(ELECTIONS_TABLE).getItem(getElectionSpec);
            System.out.printf("Election #%s\n", electionItem.get("id").toString());
            Map<String, Integer> resultsMap = electionItem.getMap("results");
            Iterator mapIterator = resultsMap.entrySet().iterator();
            while (mapIterator.hasNext()){
                Map.Entry element = (Map.Entry)mapIterator.next();
                System.out.println("- Candidate: " + element.getKey().toString() + "\n-\tNumber of votes: " + element.getValue().toString());
            }
            System.out.println("");
        }
    }

    public void castVote(String id, String electionID, int candidate){
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(ACCOUNT_TABLE).getItem(getItemSpec);
        Map<String, Boolean> electionMap = item.getMap("elections");
        if(!electionMap.get(electionID)) {
            electionMap.put(electionID, true);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id)
                    .withUpdateExpression("set elections = :elections")
                    .withValueMap(new ValueMap().withMap(":elections", electionMap));
            dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);

            GetItemSpec electionTableSpec = new GetItemSpec().withPrimaryKey("electionID", electionID);
            Item electionItem = dynamoDB.getTable(ELECTIONS_TABLE).getItem(electionTableSpec);
            Map<String, BigDecimal> resultsMap = electionItem.getMap("results");
            String candidateName = resultsMap.keySet().toArray(new String[electionMap.size()])[candidate-1];
            BigDecimal voteSum = new BigDecimal(resultsMap.get(candidateName).intValue() + 1);
            resultsMap.put(candidateName, voteSum);
            UpdateItemSpec updateElectionSpec = new UpdateItemSpec().withPrimaryKey("electionID", electionID)
                    .withUpdateExpression("set results = :results")
                    .withValueMap(new ValueMap().withMap(":results", resultsMap));
            dynamoDB.getTable(ELECTIONS_TABLE).updateItem(updateElectionSpec);
        }


    }

    public void printCandidates(String id){
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(ACCOUNT_TABLE).getItem(getItemSpec);
        Map<String, Boolean> electionMap = item.getMap("elections");
        String[] electionIDs = electionMap.keySet().toArray(new String[electionMap.size()]);
        for(int i = 0; i < electionIDs.length; i++){
            GetItemSpec getElectionSpec = new GetItemSpec().withPrimaryKey("electionID", electionIDs[i]);
            Item electionItem = dynamoDB.getTable(ELECTIONS_TABLE).getItem(getElectionSpec);
            System.out.printf("Election #%s\n", electionItem.get("id").toString());
            Map<String, Integer> resultsMap = electionItem.getMap("results");
            Iterator mapIterator = resultsMap.entrySet().iterator();
            int count = 1;
            while (mapIterator.hasNext()){
                Map.Entry element = (Map.Entry)mapIterator.next();
                System.out.println("Enter (" + count + ") to vote for " + element.getKey().toString());
                count++;
            }
            System.out.println("");
        }

    }


}
