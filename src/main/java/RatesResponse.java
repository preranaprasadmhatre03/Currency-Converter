package com.prerana.currency_converter;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RatesResponse(
		String result,
        @JsonProperty("base_code") String baseCode,
        @JsonProperty("time_last_update_utc") String lastUpdated,
        Map<String, BigDecimal> rates ) {

}
