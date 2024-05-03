package com.trader.jaguar.service.impl;

import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.BackTestData;
import com.trader.jaguar.model.data.Stock;
import com.trader.jaguar.service.BackTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackTestingImpl implements BackTesting {

    static String URL = "http://localhost:8090/getData?symbol=%s&fromDate=%s&toDate=%s";
    private static ExecutorService pool = Executors.newFixedThreadPool(50);
    private RestTemplate restTemplate = new RestTemplate();

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
}
