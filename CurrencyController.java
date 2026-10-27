package com.prerana.currency_converter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class CurrencyController {
	
    private final CurrencyService service;

    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    @GetMapping("/currencies")
    public Set<String> currencies() {
        return service.currencies();
    }

    @GetMapping("/convert")
    public ConversionResult convert(@RequestParam String from,
                                    @RequestParam String to,
                                    @RequestParam BigDecimal amount) {
        return service.convert(from, to, amount);
    }

}
