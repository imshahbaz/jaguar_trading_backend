package com.trader.jaguar.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.Stock;
import com.trader.jaguar.service.BackTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BackTestingImpl implements BackTesting {

    public static String strategyName = "";


    public static void readAllDataAtOnce(String name, Map<String, String> map, AtomicLong total, AtomicLong success, int daysToHold, float profitPercent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<String, String[]> datePrice = new ConcurrentHashMap<>();
        String path = String.format("src/main/resources/csvs/%s.csv", name);
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(path)).withSkipLines(1).build()) {
            List<String[]> allData = csvReader.readAll();
            allData.forEach(rec -> datePrice.put(formatDate(rec[0]), new String[]{rec[1], rec[2]}));
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }

        map.keySet().forEach(key -> {
            LocalDate signalDate = LocalDate.parse(key, formatter);
            LocalDate tradeDate = signalDate.plusDays(1);
            if (tradeDate.isAfter(LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(daysToHold)))
                return;

            total.getAndIncrement();
            float buyPrice = 0;
            for (int i = 0; i < 5; i++) {
                String[] prices = datePrice.get(tradeDate.format(formatter));
                if (prices == null) {
                    tradeDate = tradeDate.plusDays(1);
                    continue;
                }
                buyPrice = Float.parseFloat(prices[0]);
                break;
            }

            for (int i = 0; i < daysToHold; i++) {
                String[] prices = datePrice.get(tradeDate.format(formatter));
                if (prices == null) {
                    tradeDate = tradeDate.plusDays(1);
                    continue;
                }
                if (Float.parseFloat(prices[1]) >= ((100 + profitPercent) / 100) * buyPrice) {
                    success.getAndIncrement();
                    break;
                }
                tradeDate = tradeDate.plusDays(1);
            }

        });
    }

    public static String formatDate(String date) {
        String[] split = date.split("-");
        return String.format("%s-%s-%s", split[2], split[1], split[0]);
    }


    @Override
    public void processCInkSheet(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }
        List<Stock> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;


            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                dataList.add(Stock.builder().symbol(fields[1]).date(fields[0]).build());
            }
        }
        List<Stock> finalList = dataList.parallelStream().filter(s -> Constants.mstock.containsKey(s.getSymbol()))
                .toList();


        List<String[]> trades = finalList.parallelStream().map(stock -> new String[]{stock.getSymbol(), stock.getDate()})
                .toList();

        try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/resources/csvs/trades.csv"))) {
            writer.writeAll(trades, false);
        }

        strategyName = file.getOriginalFilename().split(",")[0].split(" ")[1];

    }

    @Override
    public Map<String, Object> backtestTrades(int daysToHold, float profitPercent) {
        Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
        String path = String.format("src/main/resources/csvs/%s.csv", "trades");
        try (CSVReader csvReader = new CSVReader(new FileReader(path))) {
            List<String[]> allData = csvReader.readAll();
            for (String[] record : allData) {
                if (MapUtils.isEmpty(map.get(record[0]))) {
                    map.put(record[0], new ConcurrentHashMap<>() {{
                        put(record[1], record[0]);
                    }});
                } else map.get(record[0]).put(record[1], record[0]);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
        AtomicLong success = new AtomicLong(0);
        AtomicLong total = new AtomicLong(0);
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            readAllDataAtOnce(entry.getKey(), entry.getValue(), total, success, daysToHold, profitPercent);
        }

        log.info("Success {}", success.get());
        log.info("Total {}", total.get());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float percent = ((float) success.get() / total.get()) * 100;
        log.info("Success Percentage {}", decimalFormat.format(percent));

        return new LinkedHashMap<>() {{
            put("Strategy", strategyName);
            put("Success", success.get());
            put("total", total.get());
            put("Success Percentage", Float.parseFloat(decimalFormat.format(percent)));
        }};
    }

}
