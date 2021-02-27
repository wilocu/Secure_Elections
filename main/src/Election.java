import java.util.Date;
import java.util.List;

public class Election {

    public String electionCode;
    public Date date;
    public List<String> participants;

    public Election(String electionCode, Date date, List<String> participants) {
        this.electionCode = electionCode;
        this.date = date;
        this.participants = participants;
    }

    public void setElectionCode(String electionCode) {
        this.electionCode = electionCode;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getElectionCode() {
        return electionCode;
    }

    public Date getDate() {
        return date;
    }

    public List<String> getParticipants() {
        return participants;
    }
}
