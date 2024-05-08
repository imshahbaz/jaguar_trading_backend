package com.trader.jaguar.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.service.EtfService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EtfServiceImpl implements EtfService {

    @Override
    public Map<String, Double> getEtf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<String, Double> etf = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.skip(1);
            List<String[]> allData = csvReader.readAll();
            for (String[] record : allData) {
                Leverage leverage = Constants.mstock.get(record[0].trim());
                if (leverage != null) {
                    etf.put(leverage.getSymbol(), leverage.getPercent());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }

        return etf;
    }
}
