package com.trader.jaguar.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.service.EtfService;
import com.trader.jaguar.service.RupeezyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RupeezyServiceImpl implements RupeezyService {

    private static final String url = "https://cms.rupeezy.in/cms/fetch-master-data/MTF?format=json&name=&page=";

    private static final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private EtfService etfService;

    @Override
    public void downloadMtfDataRupeezy() {
        boolean flag = true;
        int i = 1;
        List<String[]> list = new ArrayList<>();
        while (flag) {
            String page = url + i;
            String response = restTemplate.getForObject(page, String.class);
            JsonObject object = new Gson().fromJson(response, JsonObject.class);
            JsonObject data = object.getAsJsonObject("data");
            if (data == null) {
                break;
            }
            JsonArray results = data.getAsJsonArray("results");

            if (results.isEmpty()) {
                flag = false;
                break;
            }

            for (JsonElement margin : results) {
                JsonObject m = margin.getAsJsonObject();
                list.add(new String[]{m.get("symbol").getAsString(), m.get("mtf_margin").getAsString()});
            }
            i++;
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/resources/rupeezymtf.csv"))) {
            writer.writeAll(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Map<String, Float> getEtf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<String, Float> etf = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            csvReader.skip(1);
            List<String[]> allData = csvReader.readAll();
            for (String[] record : allData) {
                Float margin = Constants.rupeezy.get(record[0].trim());
                if (margin != null) {
                    etf.put(record[0].trim(), margin);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }

        List<String[]> list = new ArrayList<>();
        etf.keySet().parallelStream().forEach(s -> list.add(new String[]{s}));

        if (false) {
            try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/resources/python_scripts/ETF.csv")
                    , ',', ' ', ' ', "\n")) {
                writer.writeAll(list);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return etf;
    }

    @Override
    public List<Map<String, Object>> getBacktestEtfRupeezy(int numberOfDays, float percent) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("src/main/resources/python_scripts/ETF.csv"))) {
            csvReader.skip(1);
            List<String[]> allData = csvReader.readAll();
            for (String[] record : allData) {
                try {
                    String name = record[0].trim();
                    Map<String, String> result = etfService.backTestEtf(numberOfDays, name, percent);
                    Map<String, Object> objectMap = new LinkedHashMap<>();
                    objectMap.put("name", name);
                    objectMap.put("margin", Constants.rupeezy.get(name));
                    objectMap.put("success", result.get("Success"));
                    list.add(objectMap);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }

        list = list.stream()
                .filter(map -> Integer.parseInt(map.get("success").toString()) >= 10)
                .collect(Collectors.toList());

        sortByBoth(list);

        return list;
    }

    private void sortByMargin(List<Map<String, Object>> list) {
        list.sort((o1, o2) -> {
            // Extract age values from the maps
            float m1 = Float.parseFloat(o1.get("margin").toString());
            float m2 = Float.parseFloat(o2.get("margin").toString());

            // Compare age values
            return Float.compare(m2, m1);
        });
    }

    private void sortBySuccess(List<Map<String, Object>> list) {
        list.sort((o1, o2) -> {
            // Extract age values from the maps
            int success1 = Integer.parseInt(o1.get("success").toString());
            int success2 = Integer.parseInt(o2.get("success").toString());

            // Compare age values
            return Integer.compare(success2, success1);
        });
    }

    private void sortByBoth(List<Map<String, Object>> list) {
        list.sort((o1, o2) -> {
            float m1 = Float.parseFloat(o1.get("margin").toString());
            float m2 = Float.parseFloat(o2.get("margin").toString());

            // Compare age values
            int margin = Float.compare(m2, m1);

            if (margin != 0) {
                return margin;
            } else {
                int success1 = Integer.parseInt(o1.get("success").toString());
                int success2 = Integer.parseInt(o2.get("success").toString());
                return success2 - success1;
            }
        });
    }

}
