package com.prerana.currency_converter;

import java.math.BigDecimal;

public record ConversionResult(
		String from,
        String to,
        BigDecimal amount,
        BigDecimal rate,
        BigDecimal result,
        String lastUpdated ) {

}
