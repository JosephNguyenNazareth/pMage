package com.pmsconnect.mage.utils;

public class Transition {
    private String actionType;
    private String artifact;
    private String author;

    public Transition(String actionType, String artifact, String author) {
        this.actionType = actionType;
        this.artifact = artifact;
        this.author = author;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getAuthor() { return author; }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "actionType='" + actionType + '\'' +
                ", moduleName='" + artifact + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
