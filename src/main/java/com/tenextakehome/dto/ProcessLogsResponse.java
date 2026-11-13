package com.tenextakehome.dto;

import java.util.List;

public class ProcessLogsResponse {

    private List<Result> results;

    public ProcessLogsResponse() {
    }

    public ProcessLogsResponse(List<Result> results) {
        this.results = results;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
