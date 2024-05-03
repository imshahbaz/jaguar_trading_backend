package com.trader.jaguar.controller;

import com.trader.jaguar.model.data.Stock;
import com.trader.jaguar.service.BackTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class BackTestController {

    @Autowired
    private BackTesting backTesting;

    @PostMapping(value = "/backTest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Stock> backTest(@RequestPart MultipartFile file, @RequestParam(name = "initialDate") String date, @RequestParam int days) throws IOException {
        return backTesting.csvToStock(file, date, days);
    }
}
