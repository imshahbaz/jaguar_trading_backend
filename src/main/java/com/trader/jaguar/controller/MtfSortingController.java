package com.trader.jaguar.controller;

import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.service.MtfSorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class MtfSortingController {

    @Autowired
    private MtfSorting mtfSorting;

    @PostMapping(value = "/csvToMtf", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<Leverage> csvToMtf(@RequestPart MultipartFile file, short percent) throws IOException {
        return mtfSorting.csvToMtf(file, percent);
    }

    @PostMapping(value = "/getEtfMargin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, Object>> getEtfMargin(@RequestPart MultipartFile file) throws IOException {
        return mtfSorting.getEtfMargin(file);
    }

}
