package com.tenextakehome.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProcessLogsRequest {

    @NotEmpty(message = "Logs list must not be empty")
    private List<String> logs;

    @NotNull(message = "Parser ID is required")
    private Long parserId;

    public ProcessLogsRequest() {
    }

    public ProcessLogsRequest(List<String> logs, Long parserId) {
        this.logs = logs;
        this.parserId = parserId;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public Long getParserId() {
        return parserId;
    }

    public void setParserId(Long parserId) {
        this.parserId = parserId;
    }
}
