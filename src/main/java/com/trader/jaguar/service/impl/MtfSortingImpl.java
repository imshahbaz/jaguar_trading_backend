package com.trader.jaguar.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trader.jaguar.constants.Constants;
import com.trader.jaguar.model.data.Leverage;
import com.trader.jaguar.service.MtfSorting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MtfSortingImpl implements MtfSorting {

    private Gson gson;

    @Autowired
    public MtfSortingImpl(Gson gson) {
        this.gson = gson;
    }


    @Override
    public List<Leverage> csvToMtf(MultipartFile file, short percent) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            List<Leverage> dataList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                double leverage = Double.parseDouble(fields[2]);
                if (leverage >= percent)
                    dataList.add(Leverage.builder().name(fields[0]).symbol(fields[1]).percent(leverage).build());
            }
            log.info("size of list : {}", dataList.size());
            return dataList;
        }
    }

    @Override
    public List<Map<String, Object>> getEtfMargin(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Collections.emptyList();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            List<Map<String, Object>> dataList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (Constants.mstock.containsKey(fields[0])) {
                    String json = gson.toJson(Constants.mstock.get(fields[0]));
                    Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    map.put("volume", Integer.valueOf(fields[9].trim()));
                    dataList.add(map);
                }
            }
            return dataList;
        }
    }

}

