package com.trader.jaguar.constants;

import com.google.gson.Gson;
import com.trader.jaguar.model.data.BrokerLeverage;
import com.trader.jaguar.model.data.Leverage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class Constants {

    public static BrokerLeverage angelOne;
    @Autowired
    private Gson gson;

    @PostConstruct
    void init() throws IOException {
        angelOne = BrokerLeverage.builder()
                .mtf(getMap())
                .build();
        log.info("Angel One Mtf : {}", gson.toJson(angelOne));
    }

    private Map<String, Leverage> getMap() throws IOException {
        File file = new File("src/main/resources/angelonemtf.txt");
        Map<String, Map<String, Object>> rawData = gson.fromJson(FileUtils.readFileToString(file, StandardCharsets.UTF_8), Map.class);
        Map<String, Leverage> mtf = new ConcurrentHashMap<>(rawData.size());
        List<Leverage> le=new ArrayList<>();
        rawData.entrySet().parallelStream()
                .filter(entry -> Double.parseDouble(entry.getValue().get("FIELD3").toString()) >= 60)
                .forEach(entry -> {
                    Map<String, Object> value = entry.getValue();
                    mtf.put(value.get("FIELD2").toString(), Leverage.builder()
                            .name(entry.getKey())
                            .symbol(value.get("FIELD2").toString())
                            .percent(Double.parseDouble(entry.getValue().get("FIELD3").toString()))
                            .build());
                    le.add(Leverage.builder()
                            .name(entry.getKey())
                            .symbol(value.get("FIELD2").toString())
                            .percent(Double.parseDouble(entry.getValue().get("FIELD3").toString()))
                            .build());
                });
        log.info("{}",gson.toJson(le));
        return mtf;
    }
}
