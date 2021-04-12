import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class DataWriter {
    private AmazonDynamoDB dbClient;
    private DynamoDB dynamoDB;
    private BasicAWSCredentials awsCredentials;
    private String TABLE = "secure-elections-table";

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
                .withTableName(TABLE)
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
                .withTableName(TABLE)
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
    public void writeToTable(Account account){
        Item accountItem = new Item()
                .withString("id", UUID.randomUUID().toString())
                .withString("number", account.number.id)
                .withString("username", account.username)
                .withString("password", account.password)
                .withString("question", account.question.question)
                .withString("answer", account.question.answer)
                .withString("dateOfCreation", account.dateOfCreation.toString())
                .withString("voter", Boolean.toString(account.voter));

        dynamoDB.getTable(TABLE).putItem(accountItem);
    }


}
