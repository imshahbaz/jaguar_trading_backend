package com.trader.jaguar.controller;

import com.trader.jaguar.service.BackTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class BackTestController {

    @Autowired
    private BackTesting backTesting;

    @PostMapping(value = "/processCInkSheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void processCInkSheet(@RequestPart MultipartFile file) throws IOException {
        backTesting.processCInkSheet(file);
    }

    @GetMapping("/getBacktestResult")
    public Map<String, Object> getBacktestResult(@RequestParam int daysToHold, @RequestParam float profitPercent) {
        return backTesting.backtestTrades(daysToHold, profitPercent);
    }

}
