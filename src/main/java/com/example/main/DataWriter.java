package com.example.main;

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
import com.amazonaws.services.dynamodbv2.xspec.M;
import com.amazonaws.services.xray.model.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.*;

@RestController
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

    @RequestMapping(value="/login", method = RequestMethod.POST)
    public ModelAndView login(@ModelAttribute User user){
        String username = user.getUsername();
        String password = user.getPassword();
        String userSalt = this.getSalt(username);
        String encryptedPassword = this.getEncryptedPassword(username);
        ModelAndView model = new ModelAndView();
        if(!EncryptPassword.checkPassword(password, encryptedPassword, userSalt)){
            model.setViewName("redirect:/");
            return model;
        }
        user.setPassword(encryptedPassword);
        Map<String, AttributeValue> userAccount = this.readFromTable(username, encryptedPassword);
        if(userAccount == null){
            model.setViewName("redirect:/");
            return model;
        }
        user.setId(userAccount.get("id").getS());
        if(userAccount != null){
            model.setViewName("welcome");
            model.addObject("user", user);
            model.addObject("already_registered", "none");
            model.addObject("not_registered", "none");
            model.addObject("already_registered_txt", "");
        }else
            model.setViewName("redirect:/");
        return model;
    }

    @RequestMapping(value="/logout", method = RequestMethod.POST)
    public ModelAndView logout(){
        ModelAndView model = new ModelAndView();
        model.setViewName("redirect:/");
        return model;
    }

    @RequestMapping(value="/election_reg", method = RequestMethod.POST)
    public ModelAndView electionRegister(@ModelAttribute("user") User user, @ModelAttribute("electionId") String electionId) {
        ModelAndView model = new ModelAndView();

        if(this.registerForElection(electionId, user.getId()).getStatusCodeValue() == HttpStatus.BAD_REQUEST.value()){
            model.addObject("already_registered", "block");
            model.addObject("not_registered", "none");
            model.addObject("already_registered_txt", "You are already registered for this election!");
        }else{
            model.addObject("already_registered", "none");
            model.addObject("not_registered", "block");
            model.addObject("already_registered_txt", "You have successfully registered for this election!");
        }
        model.setViewName("welcome");
        return model;
    }

    @RequestMapping(value="/go_vote", method = RequestMethod.POST)
    public ModelAndView goVote(@ModelAttribute("user") User user){
        ModelAndView model = new ModelAndView();
        Map<String, AttributeValue> userInfo = this.readFromTable(user.getUsername(), user.getPassword());
        if(userInfo.get("elections") == null){
            model.addObject("user", user);
            model.addObject("can_vote", "none");
            model.addObject("cannot_vote", "block");
            model.addObject("voting_txt", "You are either not registered for an election, or are not eligible to vote.");
            model.setViewName("voting");
            return model;
        }
        else if(userInfo.get("voter").getS().equals("true") && !userInfo.get("elections").getM().isEmpty()){
            model.addObject("user", user);
            model.addObject("can_vote", "block");
            model.addObject("cannot_vote", "none");
            model.addObject("voting_txt", "");
        }else{
            model.addObject("user", user);
            model.addObject("can_vote", "none");
            model.addObject("cannot_vote", "block");
            model.addObject("voting_txt", "You are either not registered for an election, or are not eligible to vote.");
        }

        model.setViewName("voting");
        return model;
    }

    @RequestMapping(value="/cast_vote", method = RequestMethod.POST)
    public ModelAndView castVote(@ModelAttribute("user") User user, @ModelAttribute("candidate_vote") String candidate){
        ModelAndView model = new ModelAndView();

        int candidateNum = Integer.parseInt(candidate);
        String userId = user.getId();
        String electionId = this.getElectionID("1");
        model.addObject("can_vote", "none");
        model.addObject("cannot_vote", "block");

        if(!this.castVote(userId, electionId, candidateNum)){
            model.addObject("voting_txt", "You have already voted in this election!");
        }else{
            model.addObject("voting_txt", "Your vote was successfully cast!");
        }

        model.setViewName("voting");
        return model;
    }

    @RequestMapping(value="/go_home", method = RequestMethod.POST)
    public ModelAndView goHome(@ModelAttribute("user") User user){
        ModelAndView model = new ModelAndView();
        model.setViewName("welcome");
        return model;
    }

    @RequestMapping(value="/view_profile", method = RequestMethod.POST)
    public ModelAndView viewProfile(@ModelAttribute("user") User user){
        ModelAndView model = new ModelAndView();
        VoterRegistration userProfile = this.viewVoterRegistration(user.getId());
        model.addObject("user", user);
        model.addObject("userProfile", userProfile);
        model.setViewName("profile");
        return model;
    }

    @RequestMapping(value="/register", method = RequestMethod.POST)
    public ModelAndView register(@ModelAttribute NewUser newUser){
        String username = newUser.getConfirm_username();

        ModelAndView model = new ModelAndView();
        if(this.readFromTable(username))
            model.setViewName("redirect:/");
        else {
            model.setViewName("voter_reg");
            model.addObject("user", newUser);
        }
        return model;
    }

    @RequestMapping(value="/complete_reg", method = RequestMethod.POST)
    public ModelAndView completeRegistration(@ModelAttribute("user") NewUser user, @ModelAttribute NewVoter newVoter){

        Registration newReg = new Registration(newVoter.getFname(), newVoter.getLname(), newVoter.getAge(),
                                               newVoter.getEmail_addr(), newVoter.isCitizen(), newVoter.isResident(),
                                               newVoter.isFelon());
        if (newVoter.getAge() >= 18 && newVoter.isCitizen() && newVoter.isResident() && !newVoter.isFelon())
            newReg.registrationSuccess = true;
        else
            newReg.registrationSuccess = false;
        Date date = new Date();
        String salt = EncryptPassword.salt(20);
        String encryptedPassword = EncryptPassword.generatePassword(user.getConfirm_pw(), salt);

        Account newUser = new Account(new RegistrationNumber("000000001"), user.getConfirm_username(), encryptedPassword,
                                      new SecurityQuestion("What is the name of your hometown?", user.getSecurity_quest()),
                                      date, newReg.registrationSuccess, salt);
        String newAccountID = this.writeToTable(newUser);
        newReg.registrationID = newAccountID;
        this.writeToTable(newReg);
        User user1 = new User();
        user1.setId(newAccountID);
        user1.setUsername(user.getConfirm_username());

        user1.setPassword(encryptedPassword);

        ModelAndView model = new ModelAndView();
        model.setViewName("welcome");
        model.addObject("user", user1);
        return model;
    }

    /**
     * Helper function to check if a given username and password match an entry in the table.
     * Called from Main when logging in as a user.
     * @param username
     * @param password
     * @return
     */
    public Map<String, AttributeValue> readFromTable(String username, String password){
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
            return scanResult.getItems().get(0);

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

    public String getSalt(String username){
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
            return scanResult.getItems().get(0).get("salt").getS();

        return "";
    }

    public String getEncryptedPassword(String username){
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
            return scanResult.getItems().get(0).get("password").getS();

        return "";
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
                .withString("voter", Boolean.toString(account.voter))
                .withString("salt", account.getSalt());

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

    public ResponseEntity<HttpStatus> registerForElection(String electionID, String userID){
        Map<String, Boolean> electionsList = dynamoDB.getTable(ACCOUNT_TABLE).getItem("id", userID).getMap("elections");
        if(electionsList == null)
            electionsList = new HashMap<>();

        if(electionsList.containsKey(electionID)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        else{
            electionsList.put(electionID, false);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", userID)
                    .withUpdateExpression("set elections = :elections")
                    .withValueMap(new ValueMap().withMap(":elections", electionsList));
            dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }

    public ResponseEntity<HttpStatus> updateUsername(String newName, String id){
        UpdateItemSpec updateItemSpec= new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("set username = :newname")
                .withValueMap(new ValueMap().withString(":newname", newName));
        dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> updatePassword(String newPassword, String id){
        UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", id)
                .withUpdateExpression("set password = :password")
                .withValueMap(new ValueMap().withString(":password", newPassword));
        dynamoDB.getTable(ACCOUNT_TABLE).updateItem(updateItemSpec);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public class VoterRegistration{
        private String fname;
        private String lname;
        private String age;
        private String email;
        private String citizen;
        private String resident;
        private String felon;

        public VoterRegistration(String fname, String lname, String age, String email, String citizen, String resident, String felon) {
            this.fname = fname;
            this.lname = lname;
            this.age = age;
            this.email = email;
            this.citizen = citizen;
            this.resident = resident;
            this.felon = felon;
        }

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

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCitizen() {
            return citizen;
        }

        public void setCitizen(String citizen) {
            this.citizen = citizen;
        }

        public String getResident() {
            return resident;
        }

        public void setResident(String resident) {
            this.resident = resident;
        }

        public String getFelon() {
            return felon;
        }

        public void setFelon(String felon) {
            this.felon = felon;
        }
    }

    public VoterRegistration viewVoterRegistration(String id){
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(REGISTRATION_TABLE).getItem(getItemSpec);
        VoterRegistration voterReg = new VoterRegistration(item.get("fname").toString(), item.get("lname").toString(),
                                                           item.get("age").toString(), item.get("email").toString(),
                                                           item.get("citizen").toString(), item.get("resident").toString(),
                                                           item.get("felon").toString());
        return voterReg;
    }

    public class ElectionResult{
        private String id;
        private Map<String, Integer> results;

        public ElectionResult(String id, Map<String, Integer> results) {
            this.id = id;
            this.results = results;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Map<String, Integer> getResults() {
            return results;
        }

        public void setResults(Map<String, Integer> results) {
            this.results = results;
        }
    }

    public ResponseEntity<List<ElectionResult>> viewElectionResults(String id){
        List<ElectionResult> electionResults = new ArrayList<>();
        GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);
        Item item = dynamoDB.getTable(ACCOUNT_TABLE).getItem(getItemSpec);
        Map<String, Boolean> electionMap = item.getMap("elections");
        String[] electionIDs = electionMap.keySet().toArray(new String[electionMap.size()]);
        for(int i = 0; i < electionIDs.length; i++){
            GetItemSpec getElectionSpec = new GetItemSpec().withPrimaryKey("electionID", electionIDs[i]);
            Item electionItem = dynamoDB.getTable(ELECTIONS_TABLE).getItem(getElectionSpec);
            Map<String, Integer> resultsMap = electionItem.getMap("results");
            electionResults.add(new ElectionResult(id, resultsMap));
        }
        return ResponseEntity.status(HttpStatus.OK).body(electionResults);
    }

    public boolean castVote(String id, String electionID, int candidate){
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
            return true;
        }
        return false;
    }

}
