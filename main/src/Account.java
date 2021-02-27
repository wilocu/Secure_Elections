import java.util.Date;

public class Account {

    public RegistrationNumber number;
    public String username;
    public String password;
    public SecurityQuestion question;
    public Date dateOfCreation;

    public Account(RegistrationNumber number, String username, String password, SecurityQuestion question, Date dateOfCreation) {
        this.number = number;
        this.username = username;
        this.password = password;
        this.question = question;
        this.dateOfCreation = dateOfCreation;
    }

    public void setNumber(RegistrationNumber number) {
        this.number = number;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQuestion(SecurityQuestion question) {
        this.question = question;
    }

    public void setDateOfCreation(Date dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public RegistrationNumber getNumber() {
        return number;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SecurityQuestion getQuestion() {
        return question;
    }

    public Date getDateOfCreation() {
        return dateOfCreation;
    }
}
