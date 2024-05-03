package com.trader.jaguar.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.BackTestData;
import com.trader.jaguar.model.data.Stock;
import com.trader.jaguar.service.BackTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackTestingImpl implements BackTesting {

    static String URL = "http://localhost:8090/getData?symbol=%s&fromDate=%s&toDate=%s";
    private static ExecutorService pool = Executors.newFixedThreadPool(50);
    private RestTemplate restTemplate = new RestTemplate();

    public static void readAllDataAtOnce(String name, Map<String, String> map, AtomicLong total, AtomicLong success) {
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
            LocalDate tradeDate = LocalDate.parse(key, formatter);
            tradeDate = tradeDate.plusDays(1);
            total.getAndIncrement();
            float buyPrice = 0;
            for (int i = 0; i < 5; i++) {
                String[] prices = datePrice.get(tradeDate.format(formatter));
                if (prices == null) {
                    tradeDate = tradeDate.plusDays(1);
                    continue;
                }
                /*if (Float.parseFloat(prices[1]) >= 1.015 * Float.parseFloat(prices[0]))
                    success.getAndIncrement();*/
                buyPrice = Float.parseFloat(prices[0]);
                break;
            }

            for (int i = 0; i < 10; i++) {
                String[] prices = datePrice.get(tradeDate.format(formatter));
                if (prices == null) {
                    tradeDate = tradeDate.plusDays(1);
                    continue;
                }
                if (Float.parseFloat(prices[1]) >= 1.02 * buyPrice) {
                    success.getAndIncrement();
                    break;
                }
                tradeDate = tradeDate.plusDays(1);
            }

        });
    }

    private static String formatDate(String date) {
        String[] split = date.split("-");
        return String.format("%s-%s-%s", split[2], split[1], split[0]);
    }

    @Override
    public List<Stock> csvToStock(MultipartFile file, String date, int days) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }
        List<Stock> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;


            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                dataList.add(Stock.builder().symbol(fields[1]).date(fields[0]).build());
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


        LocalDate initialRecord = LocalDate.parse(date, formatter);

        LocalDate lastRecord = initialRecord.plusDays(days);


        List<Stock> finalList = dataList.parallelStream().filter(s -> Constants.mstock.containsKey(s.getSymbol()))
                .filter(s -> {
                    LocalDate sdate = LocalDate.parse(s.getDate(), formatter);
                    return sdate.isAfter(initialRecord.minusDays(1)) && sdate.isBefore(lastRecord);
                })
                .collect(Collectors.toList());


        log.info("{}", dataList.size());
        log.info("{}", finalList.size());

        pool.submit(() -> {
            AtomicInteger total = new AtomicInteger();
            AtomicInteger success = new AtomicInteger();
            finalList.parallelStream().filter(s -> !Objects.equals(s.getSymbol(), "symbol"))
                    .forEach(stock -> {
                        LocalDate sdate = LocalDate.parse(stock.getDate(), formatter);

                        LocalDate newDate = sdate.plusDays(6);

                        String endDate = newDate.format(formatter);
                        String encodedSymbol = URLEncoder.encode(stock.getSymbol(), StandardCharsets.UTF_8);

                        String url = String.format(URL, encodedSymbol, stock.getDate(), endDate);
                        try {
                            BackTestData data = restTemplate.getForObject(url, BackTestData.class);
                            if (Float.valueOf(data.getHighPrice().get("1").replace(",", "")) >=
                                    Float.valueOf(data.getOpenPrice().get("1").replace(",", "")) * 1.01) {
                                success.getAndIncrement();
                            }
                            total.getAndIncrement();
                        } catch (Exception e) {
                            log.error("{}", e.getMessage());
                            log.info("{}", url);
                        }
                    });
            log.info("{}", success.get());
            log.info("{}", total.get());
        });


        return dataList;
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

      /*  Set<String[]> downloadList = finalList.parallelStream().map(stock -> new String[]{stock.getSymbol()}).
                collect(Collectors.toSet());


        try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/resources/csvs/download.csv"))) {
            writer.writeAll(downloadList,false);
        }*/

        try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/resources/csvs/trades.csv"))) {
            writer.writeAll(trades, false);
        }

    }

    @Override
    public void backtestTrades() {
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
            readAllDataAtOnce(entry.getKey(), entry.getValue(), total, success);
        }

        log.info("Success {}", success.get());
        log.info("Total {}", total.get());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        float percent = ((float) success.get() / total.get()) * 100;
        log.info("Success Percentage {}", decimalFormat.format(percent));
    }

}
