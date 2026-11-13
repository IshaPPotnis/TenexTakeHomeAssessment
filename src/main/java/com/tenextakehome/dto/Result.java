package com.tenextakehome.dto;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private String log;
    private String parsedOutput;
    private String notes;
    private List<String> matchedIncident = new ArrayList<>();

    public Result() {
    }

    public Result(String log, String parsedOutput, String notes) {
        this(log, parsedOutput, notes, List.of());
    }

    public Result(String log, String parsedOutput, String notes, List<String> matchedIncident) {
        this.log = log;
        this.parsedOutput = parsedOutput;
        this.notes = notes;
        setMatchedIncident(matchedIncident);
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getParsedOutput() {
        return parsedOutput;
    }

    public void setParsedOutput(String parsedOutput) {
        this.parsedOutput = parsedOutput;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getMatchedIncident() {
        return matchedIncident;
    }

    public void setMatchedIncident(List<String> matchedIncident) {
        this.matchedIncident = matchedIncident == null ? new ArrayList<>() : new ArrayList<>(matchedIncident);
    }
}
