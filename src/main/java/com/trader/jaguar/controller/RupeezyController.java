package com.trader.jaguar.controller;

import com.trader.jaguar.service.RupeezyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("rupeezy/")
public class RupeezyController {

    @Autowired
    private RupeezyService rupeezyService;

    @GetMapping("/downloadMtfDataRupeezy")
    public void downloadMtfDataRupeezy() {
        rupeezyService.downloadMtfDataRupeezy();
    }

    @PostMapping(value = "/getEtf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Float> getEtf(@RequestPart MultipartFile file) {
        return rupeezyService.getEtf(file);
    }

    @GetMapping("/getAllBacktestDataRupeezy")
    public List<Map<String, Object>> getAllBacktestDataRupeezy(@RequestParam int numberOfDays, @RequestParam float percent) {
        return rupeezyService.getBacktestEtfRupeezy(numberOfDays, percent);
    }
}
