import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
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

    public void printTables(){
        Table table = dynamoDB.getTable(TABLE);
        Item item = table.getItem("id", 1);
        System.out.println(item.toJSONPretty());
    }

    public void writeToTable(Account account){
        Item accountItem = new Item()
                .withString("id", UUID.randomUUID().toString())
                .withString("number", account.number.id)
                .withString("username", account.username)
                .withString("password", account.password)
                .withString("question", account.question.question)
                .withString("answer", account.question.answer)
                .withString("dateOfCreation", account.dateOfCreation.toString());

        dynamoDB.getTable(TABLE).putItem(accountItem);
    }


}
