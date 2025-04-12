package com.pmsconnect.mage.project.coordination;

public class ArtifactPair implements CoordinationPair {
    private String predecessorProcess;
    private String successorProcess;
    private String preArtifact;
    private String preArtifactState;
    private String sucArtifact;
    private String sucArtifactState;

    public ArtifactPair(String predecessorProcess, String successorProcess, String preArtifact, String preArtifactState, String sucArtifact, String sucArtifactState) {
        this.predecessorProcess = predecessorProcess;
        this.successorProcess = successorProcess;
        this.preArtifact = preArtifact;
        this.preArtifactState = preArtifactState;
        this.sucArtifact = sucArtifact;
        this.sucArtifactState = sucArtifactState;
    }

    public String getPredecessorProcess() {
        return predecessorProcess;
    }

    public void setPredecessorProcess(String predecessorProcess) {
        this.predecessorProcess = predecessorProcess;
    }

    public String getSuccessorProcess() {
        return successorProcess;
    }

    public void setSuccessorProcess(String successorProcess) {
        this.successorProcess = successorProcess;
    }

    public String getPreArtifact() {
        return preArtifact;
    }

    public void setPreArtifact(String preArtifact) {
        this.preArtifact = preArtifact;
    }

    public String getPreArtifactState() {
        return preArtifactState;
    }

    public void setPreArtifactState(String preArtifactState) {
        this.preArtifactState = preArtifactState;
    }

    public String getSucArtifact() {
        return sucArtifact;
    }

    public void setSucArtifact(String sucArtifact) {
        this.sucArtifact = sucArtifact;
    }

    public String getSucArtifactState() {
        return sucArtifactState;
    }

    public void setSucArtifactState(String sucArtifactState) {
        this.sucArtifactState = sucArtifactState;
    }
}
