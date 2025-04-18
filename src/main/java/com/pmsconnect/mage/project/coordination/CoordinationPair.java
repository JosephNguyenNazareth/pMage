package com.pmsconnect.mage.project.coordination;

public class CoordinationPair {
    private String predecessorProcess;
    private String successorProcess;
    private String predecessorPoint;
    private String prePointState;
    private String successorPoint;
    private String sucPointState;

    public CoordinationPair(String predecessorProcess, String successorProcess, String predecessorPoint, String prePointState, String successorPoint, String sucPointState) {
        this.predecessorProcess = predecessorProcess;
        this.successorProcess = successorProcess;
        this.predecessorPoint = predecessorPoint;
        this.prePointState = prePointState;
        this.successorPoint = successorPoint;
        this.sucPointState = sucPointState;
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

    public String getPredecessorPoint() {
        return predecessorPoint;
    }

    public void setPredecessorPoint(String predecessorPoint) {
        this.predecessorPoint = predecessorPoint;
    }

    public String getPrePointState() {
        return prePointState;
    }

    public void setPrePointState(String prePointState) {
        this.prePointState = prePointState;
    }

    public String getSuccessorPoint() {
        return successorPoint;
    }

    public void setSuccessorPoint(String successorPoint) {
        this.successorPoint = successorPoint;
    }

    public String getSucPointState() {
        return sucPointState;
    }

    public void setSucPointState(String sucPointState) {
        this.sucPointState = sucPointState;
    }

    @Override
    public String toString() {
        return "CoordinationPair{" +
                "predecessorProcess='" + predecessorProcess + '\'' +
                ", successorProcess='" + successorProcess + '\'' +
                ", predecessorPoint='" + predecessorPoint + '\'' +
                ", prePointState='" + prePointState + '\'' +
                ", successorPoint='" + successorPoint + '\'' +
                ", sucPointState='" + sucPointState + '\'' +
                '}';
    }
}
