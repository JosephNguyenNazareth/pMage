package com.pmsconnect.mage.utils;

public class ActionEvent {
    private String appEvent;
    private String pmsEvent;
    private String contextInfo;
    private String task;

    public ActionEvent() {
    }

    public ActionEvent(String action, String contextInfo, String pmsEvent, String task) {
        this.appEvent = action;
        this.contextInfo = contextInfo;
        this.pmsEvent = pmsEvent;
        this.task = task;
    }

    public ActionEvent(String[] actionEventToken) {
        this.appEvent = actionEventToken[0];
        this.contextInfo = actionEventToken[1];
        this.pmsEvent = actionEventToken[2];
        this.task = actionEventToken[3];
    }

    public String getAppEvent() {
        return appEvent;
    }

    public void setAppEvent(String appEvent) {
        this.appEvent = appEvent;
    }

    public String getPmsEvent() {
        return pmsEvent;
    }

    public void setPmsEvent(String pmsEvent) {
        this.pmsEvent = pmsEvent;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(String contextInfo) {
        this.contextInfo = contextInfo;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ActionEvent))
            return false;
        if (other == this)
            return true;
        ActionEvent otherActionEvent = (ActionEvent) other;
        return otherActionEvent.getAppEvent().equals(this.getAppEvent())
                && otherActionEvent.getPmsEvent().equals(this.getPmsEvent())
                && otherActionEvent.getContextInfo().equals(this.getContextInfo())
                && otherActionEvent.getTask().equals(this.getTask());
    }

    @Override
    public String toString() {
        return appEvent + ", " + contextInfo + ", " + pmsEvent + ", " + task;
    }
}
