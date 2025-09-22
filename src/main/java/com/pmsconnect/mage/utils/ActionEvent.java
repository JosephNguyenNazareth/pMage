package com.pmsconnect.mage.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ActionEvent {
    private String appAction;
    private String pmsAction;
    private String contextInfo;
    private String runtimeElement;

    public ActionEvent() {
    }

    public ActionEvent(String action, String contextInfo, String pmsEvent) {
        this.appAction = action;
        this.contextInfo = contextInfo;
        this.pmsAction = pmsEvent;
        this.runtimeElement = "";
    }

    public ActionEvent(String[] actionEventToken) {
        this.appAction = actionEventToken[0];
        this.contextInfo = actionEventToken[1];
        this.pmsAction = actionEventToken[2];
        this.runtimeElement = "";
    }

    public String getAppAction() {
        return appAction;
    }

    public void setAppAction(String appAction) {
        this.appAction = appAction;
    }

    public String getPmsAction() {
        return pmsAction;
    }

    public void setPmsAction(String pmsAction) {
        this.pmsAction = pmsAction;
    }

    public String getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(String contextInfo) {
        this.contextInfo = contextInfo;
    }

    public String getRuntimeElement() {
        return runtimeElement;
    }

    public void setRuntimeElement(String runtimeElement) {
        this.runtimeElement = runtimeElement;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ActionEvent))
            return false;
        if (other == this)
            return true;
        ActionEvent otherActionEvent = (ActionEvent) other;
        return otherActionEvent.getAppAction().equals(this.getAppAction())
                && otherActionEvent.getPmsAction().equals(this.getPmsAction())
                && otherActionEvent.getContextInfo().equals(this.getContextInfo());
    }

    @Override
    public String toString() {
        return appAction + ", " + contextInfo + ", " + pmsAction + ", " + runtimeElement;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("appEvent", appAction);
        map.put("pmsEvent", pmsAction);
        map.put("contextInfo", contextInfo);
        map.put("runtimeElement", runtimeElement);
        return map;
    }
}
