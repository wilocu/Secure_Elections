import java.util.Date;
import java.util.List;

public class Election {

    public ElectionId electionid;
    public Date date;
    public List<Voter> participants;

    public Election(ElectionId electionid, Date date, List<Voter> participants) {
        this.electionid = electionid;
        this.date = date;
        this.participants = participants;
    }

    public void setElectionid(ElectionId electionid) {
        this.electionid = electionid;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setParticipants(List<Voter> participants) {
        this.participants = participants;
    }

    public ElectionId getElectionid() {
        return electionid;
    }

    public Date getDate() {
        return date;
    }

    public List<Voter> getParticipants() {
        return participants;
    }
}
