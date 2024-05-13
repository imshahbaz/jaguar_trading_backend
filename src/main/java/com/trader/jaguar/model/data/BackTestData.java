package com.trader.jaguar.model.data;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BackTestData {
    @JsonAlias(value = "OpenPrice")
    private Map<String, String> openPrice;
    @JsonAlias(value = "HighPrice")
    private Map<String, String> highPrice;
}
