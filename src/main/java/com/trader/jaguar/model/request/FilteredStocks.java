package com.trader.jaguar.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilteredStocks {

    private String name;

    private String symbol;

    private double change;

    private double price;

    private double volume;

}
