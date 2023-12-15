package com.trader.jaguar.controller;

import com.trader.jaguar.service.MtfSorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class MtfSortingController {

    @Autowired
    private MtfSorting mtfSorting;

    @PostMapping(value = "/getAngelMtf",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Map<String, Double> getAngelMtf(@RequestPart MultipartFile file) {
        return mtfSorting.angelOneMtfSort(file);
    }
}
