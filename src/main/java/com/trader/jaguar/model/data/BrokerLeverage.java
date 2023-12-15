package com.trader.jaguar.model.data;

import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class BrokerLeverage {
    private Map<String, Leverage> mtf;
}
