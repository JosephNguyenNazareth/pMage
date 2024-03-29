package com.pmsconnect.mage.user;

import com.pmsconnect.mage.connector.Connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Bridge {
    // app connection
    private String appName;
    private String userNameApp;
    private String passwordApp;
    private String projectLink;
    private String projectDir;

    // pms connection
    private String userNamePms;
    private String passwordPms;
    private String pmsName;
    private String pmsUrl;
    private String processId;

    public Bridge(String appName, String userNameApp, String passwordApp, String projectLink, String projectDir, String userNamePms, String passwordPms, String pmsName, String pmsUrl, String processId) {
        this.appName = appName;
        this.userNameApp = userNameApp;
        this.passwordApp = passwordApp;
        this.projectLink = projectLink;
        this.projectDir = projectDir;
        this.userNamePms = userNamePms;
        this.passwordPms = passwordPms;
        this.pmsName = pmsName;
        this.pmsUrl = pmsUrl;
        this.processId = processId;
        this.loadProperties();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUserNameApp() {
        return userNameApp;
    }

    public void setUserNameApp(String userNameApp) {
        this.userNameApp = userNameApp;
    }

    public String getPasswordApp() {
        return passwordApp;
    }

    public void setPasswordApp(String passwordApp) {
        this.passwordApp = passwordApp;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    public String getUserNamePms() {
        return userNamePms;
    }

    public void setUserNamePms(String userNamePms) {
        this.userNamePms = userNamePms;
    }

    public String getPasswordPms() {
        return passwordPms;
    }

    public void setPasswordPms(String passwordPms) {
        this.passwordPms = passwordPms;
    }

    public String getPmsName() {
        return pmsName;
    }

    public void setPmsName(String pmsName) {
        this.pmsName = pmsName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getPmsUrl() {
        return pmsUrl;
    }

    public void setPmsUrl(String pmsUrl) {
        this.pmsUrl = pmsUrl;
    }

    public void loadProperties() {
        try {
            InputStream file = Connector.class.getResourceAsStream("/application.properties");
            if (file!=null) System.getProperties().load(file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public String getPMSConfig() {
        return System.getProperty("pmsconfig");
    }

    public String getAppConfig() {
        return System.getProperty("appconfig");
    }
}
