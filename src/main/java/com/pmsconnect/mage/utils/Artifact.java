package com.pmsconnect.mage.utils;

public class Artifact {
    private String name;
    private String state;
    private boolean available;

    public Artifact(String name) {
        this.name = name;
        this.state = "initial";
        this.available = false;
    }

    public Artifact(String name, boolean available) {
        this.name = name;
        this.state = "initial";
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "name='" + name + '\'' +
                ", state='" + state + '\'' +
                ", available=" + available +
                '}';
    }
}
