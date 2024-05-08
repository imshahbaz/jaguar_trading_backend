package com.trader.jaguar.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface BackTesting {

    void processCInkSheet(MultipartFile file) throws IOException;

    Map<String, Object> backtestTrades(int daysToHold, float profitPercent);
}
