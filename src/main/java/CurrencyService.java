package com.prerana.currency_converter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyService {
	
	 private record Cached(RatesResponse data, Instant fetchedAt) {}

	    private final Map<String, Cached> cache = new ConcurrentHashMap<>();
	    private final RestClient client = RestClient.create();
	    private final String apiUrl;
	    private final Duration ttl;

	    public CurrencyService(@Value("${rates.api.url}") String apiUrl,
	                           @Value("${rates.cache.minutes}") long cacheMinutes) {
	        this.apiUrl = apiUrl;
	        this.ttl = Duration.ofMinutes(cacheMinutes);
	    }

	    public TreeSet<String> currencies() {
	        return new TreeSet<>(getRates("USD").rates().keySet());
	    }

	    public ConversionResult convert(String from, String to, BigDecimal amount) {
	        from = from.toUpperCase();
	        to = to.toUpperCase();

	        if (amount == null || amount.signum() < 0) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be zero or positive");
	        }

	        RatesResponse data = getRates(from);
	        BigDecimal rate = data.rates().get(to);
	        if (rate == null) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + to);
	        }

	        BigDecimal result = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
	        return new ConversionResult(from, to, amount, rate, result, data.lastUpdated());
	    }

	    private RatesResponse getRates(String base) {
	        Cached cached = cache.get(base);
	        if (cached != null && Instant.now().isBefore(cached.fetchedAt().plus(ttl))) {
	            return cached.data();
	        }

	        RatesResponse fresh;
	        try {
	            fresh = client.get().uri(apiUrl + base).retrieve().body(RatesResponse.class);
	        } catch (Exception e) {
	            if (cached != null) return cached.data(); // serve stale rates if API is down
	            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach rates provider");
	        }

	        if (fresh == null || !"success".equals(fresh.result()) || fresh.rates() == null) {
	            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported base currency: " + base);
	        }
	        cache.put(base, new Cached(fresh, Instant.now()));
	        return fresh;
	    }

}
