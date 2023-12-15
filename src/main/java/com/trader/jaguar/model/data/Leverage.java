package com.trader.jaguar.model.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Leverage {
    private String name;
    private String symbol;
    private double percent;
}
