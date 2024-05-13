package com.trader.jaguar.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RupeezyService {
    void downloadMtfDataRupeezy();

    Map<String, Float> getEtf(MultipartFile file);

    List<Map<String,Object>> getBacktestEtfRupeezy(int numberOfDays, float percent);
}
