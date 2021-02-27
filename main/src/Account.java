public class Account {

    public Person person;
    public String username;
    public String password;
    //public list of elections

    public Account(Person person, String username, String password) {
        this.person = person;
        this.username = username;
        this.password = password;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Person getPerson() {
        return person;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
