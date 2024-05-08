package com.trader.jaguar.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.service.EtfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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

    @Override
    public Map<String, String> backTestEtf(int numberOfDays, String etfName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<String, String[]> datePrice = new ConcurrentHashMap<>();
        String path = String.format("src/main/resources/csvs/%s.csv", etfName);
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(path)).withSkipLines(1).build()) {
            List<String[]> allData = csvReader.readAll();
            allData.forEach(rec -> datePrice.put(BackTestingImpl.formatDate(rec[0]), new String[]{rec[1], rec[2]}));
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }

        LocalDate current = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate startDate = current.minusDays(numberOfDays);

        for (int i = 0; i < 10; i++) {
            String[] price = datePrice.get(startDate.format(formatter));
            if (price == null) {
                startDate = startDate.plusDays(1);
            } else break;
        }

        boolean newTrade = true;
        double buyPrice = 0;
        int numOfTrades = 0;
        int success = 0;

        for (int i = 0; i < numberOfDays; i++) {
            String[] price = datePrice.get(startDate.format(formatter));
            if (price == null) {
                startDate = startDate.plusDays(1);
                continue;
            }
            if (newTrade) {
                buyPrice = Double.parseDouble(price[0]);
                numOfTrades++;
            }
            double high = Double.parseDouble(price[1]);
            if (high >= 1.02 * buyPrice) {
                success++;
                newTrade = true;
            } else {
                newTrade = false;
            }
            startDate = startDate.plusDays(1);
        }
        Map<String, String> response = LinkedHashMap.newLinkedHashMap(2);
        response.put("Success", success + "");
        response.put("Total", numOfTrades + "");
        return response;
    }

}
