package sdx.talk.entity;


public class CandidateInfoEntity {
    private int label;
    private String id;
    private String candidate;

    public CandidateInfoEntity(int label, String id, String candidate) {
        this.label = label;
        this.id = id;
        this.candidate = candidate;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }
}

