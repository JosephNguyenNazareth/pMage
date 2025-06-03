package com.pmsconnect.mage.project.coordination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum ActivityState {
    CREATED("CREATED"),
    READY("READY"),
    RESERVED("RESERVED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED"),
    SUSPENDED("SUSPENDED"),
    UNKNOWN("UNKNOWN");

    private final String state;

    @Override
    public String toString() {
        return state;
    }

    ActivityState(String state) {
        this.state = state;
    }
}
