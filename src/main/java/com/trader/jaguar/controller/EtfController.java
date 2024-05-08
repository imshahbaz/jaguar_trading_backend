package com.trader.jaguar.controller;

import com.trader.jaguar.service.EtfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/etf")
public class EtfController {

    @Autowired
    private EtfService etfService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Double> getEtf(@RequestPart MultipartFile file) {
        return etfService.getEtf(file);
    }

    @GetMapping("/getBacktestEtf")
    public Map<String, String> getBacktestEtf(@RequestParam int numberOfDays, @RequestParam String etfName) {
        return etfService.backTestEtf(numberOfDays, etfName);
    }
}
