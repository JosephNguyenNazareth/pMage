package com.pmsconnect.mage.project.coordination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityState {
    public static String STARTED = "started";
    public static String FINISHED = "finished";
    public static String UNKNOWN = "unknown";

    public static Map<String, String> correspondStatesStarted = new HashMap<>();
    public static Map<String, String> correspondStatesFinished = new HashMap<>();

    public static void addCorrespondingStateStarted(String pms, String state) {
        correspondStatesStarted.computeIfAbsent(pms, k -> state);
    }

    public static void addCorrespondingStateFinished(String pms, String state) {
        correspondStatesFinished.computeIfAbsent(pms, k -> state);
    }
}
