package com.trader.jaguar.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MtfSorting {
    Map<String,Double> angelOneMtfSort(MultipartFile data);
}
