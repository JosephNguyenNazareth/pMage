package com.pmsconnect.mage.project.coordination;

public class ActivityPair implements CoordinationPair {
    private String predecessorProcess;
    private String successorProcess;
    private String preActivity;
    private String sucActivity;

    public ActivityPair(String predecessorProcess, String successorProcess, String preActivity, String sucActivity) {
        this.predecessorProcess = predecessorProcess;
        this.successorProcess = successorProcess;
        this.preActivity = preActivity;
        this.sucActivity = sucActivity;
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

    public String getPreActivity() {
        return preActivity;
    }

    public void setPreActivity(String preActivity) {
        this.preActivity = preActivity;
    }

    public String getSucActivity() {
        return sucActivity;
    }

    public void setSucActivity(String sucActivity) {
        this.sucActivity = sucActivity;
    }
}
