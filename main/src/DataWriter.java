import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataWriter {
    private AmazonDynamoDB dbClient;
    private DynamoDB dynamoDB;
    private BasicAWSCredentials awsCredentials;
    private String ACCOUNT_TABLE = "secure-elections-table";
    private String REGISTRATION_TABLE = "secure-elections-voters";

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
    public boolean readFromTable(String username, String password){
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
            return true;

        return false;
    }

    /**
     * Helper function to determine whether a username exists in the table.
     * Called from Main when making a new account; We don't want duplicate usernames!
     * @param username
     * @return
     */
    public boolean readFromTable(String username)
    {
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

        if(scanResult.getItems().size() > 0)
            return true;

        return false;
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


}
