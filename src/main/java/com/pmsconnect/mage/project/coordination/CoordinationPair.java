package com.pmsconnect.mage.project.coordination;

public class CoordinationPair {
    private String predecessorProcess;
    private String successorProcess;
    private String predecessorPoint;
    private String prePointDesiredState;
    private String prePointActualState;
    private String successorPoint;
    private String sucPointDesiredState;
    private String sucPointActualState;

    public CoordinationPair(String predecessorProcess, String successorProcess, String predecessorPoint, String prePointDesiredState, String successorPoint, String sucPointDesiredState) {
        this.predecessorProcess = predecessorProcess;
        this.successorProcess = successorProcess;
        this.predecessorPoint = predecessorPoint;
        this.prePointDesiredState = prePointDesiredState;
        this.prePointActualState = ActivityState.UNKNOWN;
        this.successorPoint = successorPoint;
        this.sucPointDesiredState = sucPointDesiredState;
        this.sucPointActualState = ActivityState.UNKNOWN;
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

    public String getPrePointDesiredState() {
        return prePointDesiredState;
    }

    public void setPrePointDesiredState(String prePointDesiredState) {
        this.prePointDesiredState = prePointDesiredState;
    }

    public String getPrePointActualState() {
        return prePointActualState;
    }

    public void setPrePointActualState(String prePointActualState) {
        this.prePointActualState = prePointActualState;
    }

    public String getSuccessorPoint() {
        return successorPoint;
    }

    public void setSuccessorPoint(String successorPoint) {
        this.successorPoint = successorPoint;
    }

    public String getSucPointDesiredState() {
        return sucPointDesiredState;
    }

    public void setSucPointDesiredState(String sucPointDesiredState) {
        this.sucPointDesiredState = sucPointDesiredState;
    }

    public String getSucPointActualState() {
        return sucPointActualState;
    }

    public void setSucPointActualState(String sucPointActualState) {
        this.sucPointActualState = sucPointActualState;
    }

    @Override
    public String toString() {
        return "CoordinationPair{" +
                "predecessorProcess='" + predecessorProcess + '\'' +
                ", successorProcess='" + successorProcess + '\'' +
                ", predecessorPoint='" + predecessorPoint + '\'' +
                ", prePointDesiredState='" + prePointDesiredState + '\'' +
                ", prePointActualState='" + prePointActualState + '\'' +
                ", successorPoint='" + successorPoint + '\'' +
                ", sucPointDesiredState='" + sucPointDesiredState + '\'' +
                ", sucPointActualState='" + sucPointActualState + '\'' +
                '}';
    }
}
