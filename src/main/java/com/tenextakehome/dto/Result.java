package com.tenextakehome.dto;

public class Result {

    private String log;
    private String parsedOutput;
    private String notes;

    public Result() {
    }

    public Result(String log, String parsedOutput, String notes) {
        this.log = log;
        this.parsedOutput = parsedOutput;
        this.notes = notes;
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
}
