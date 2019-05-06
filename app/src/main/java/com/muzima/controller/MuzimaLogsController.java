package com.muzima.controller;

import com.muzima.api.model.LogStatistic;
import com.muzima.api.service.LogStatisticService;

import java.io.IOException;
import java.util.List;

public class MuzimaLogsController {
    private final LogStatisticService logStatisticService;
    public MuzimaLogsController(LogStatisticService logStatisticService){
        this.logStatisticService = logStatisticService;
    }

    public List<LogStatistic> getAllLogStatistics() throws IOException {
        return logStatisticService.getLogStatistics();
    }

    public void downloadlogStatistics() throws IOException {
        logStatisticService.downloadLogStatistics();
    }

}
