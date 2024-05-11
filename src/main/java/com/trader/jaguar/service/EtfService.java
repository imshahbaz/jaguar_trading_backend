package com.trader.jaguar.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EtfService {
    Map<String, Double> getEtf(MultipartFile file);

    Map<String, String> backTestEtf(int numberOfDays, String etfName, float percent);
}
