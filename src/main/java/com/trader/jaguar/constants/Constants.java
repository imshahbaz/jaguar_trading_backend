package com.trader.jaguar.constants;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.service.MtfSorting;
import com.trader.jaguar.utils.CommonUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Constants {

    public static Map<String, Leverage> mstock = new ConcurrentHashMap<>();

    public static Map<String, Float> rupeezy = new ConcurrentHashMap<>();

    @Autowired
    private Gson gson;

    @Autowired
    private MtfSorting mtfSorting;

    @PostConstruct
    public void loadMstock() throws IOException {
        File file = new File("src/main/resources/convertcsv.csv");
        List<Leverage> list = mtfSorting.csvToMtf(CommonUtils.convertFile(file), (short) 60);
        list.parallelStream().forEach(leverage -> mstock.put(leverage.getSymbol(), leverage));
        log.info("mstock : {}", mstock);
        loadRupeezy();
        log.info("rupeezy : {}", rupeezy);
    }

    private void loadRupeezy() {
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/rupeezymtf.csv"))) {
            List<String[]> data = reader.readAll();
            data.parallelStream().filter(stock ->
                    Float.parseFloat(stock[1]) >= 3
            ).forEach(stock -> rupeezy.put(stock[0], Float.parseFloat(stock[1])));
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

}
