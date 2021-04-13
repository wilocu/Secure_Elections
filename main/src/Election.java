import com.amazonaws.services.dynamodbv2.xspec.N;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Election {

    public ElectionId electionid;
    public Date date;
    public List<Voter> participants;
    public Map<String, Integer> results;

    public Election(ElectionId electionid, Date date, List<Voter> participants, Map<String, Integer> results) {
        this.electionid = electionid;
        this.date = date;
        this.participants = participants;
        this.results = results;
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
