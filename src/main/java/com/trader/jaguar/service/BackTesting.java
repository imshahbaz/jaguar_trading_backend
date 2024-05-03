package com.trader.jaguar.service;

import com.trader.jaguar.model.data.Stock;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BackTesting {
    List<Stock> csvToStock(MultipartFile file, String date, int days) throws IOException;

    void processCInkSheet(MultipartFile file) throws IOException;

    void backtestTrades();
}
