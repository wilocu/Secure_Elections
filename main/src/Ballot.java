import java.util.Date;

public class Ballot {

    public String ballotId;
    public Date date;
    public Voter voter;
    public ElectionId electionId;
    public String vote;

    public Ballot(String ballotId, Date date, Voter voter, ElectionId electionId, String vote) {
        this.ballotId = ballotId;
        this.date = date;
        this.voter = voter;
        this.electionId = electionId;
        this.vote = vote;
    }

    public void setBallotId(String ballotId) {
        this.ballotId = ballotId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setVoter(Voter voter) {
        this.voter = voter;
    }

    public void setElectionId(ElectionId electionId) {
        this.electionId = electionId;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    public String getBallotId() {
        return ballotId;
    }

    public Date getDate() {
        return date;
    }

    public Voter getVoter() {
        return voter;
    }

    public ElectionId getElectionId() {
        return electionId;
    }

    public String getVote() {
        return vote;
    }
}
