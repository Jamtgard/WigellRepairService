package com.example.WigellRepairService.services;

import com.example.WigellRepairService.exceptions.CurrencyConversionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final RestClient client;
    private final String apiKey;
    private final boolean failToOpen;

    private static final String PROVIDER = "apiplugin";
    private static final String ENDPOINT = "/v1/currency/{api-key}/convert";

    public CurrencyServiceImpl(
            @Qualifier("currencyConverterRestClient") RestClient client,
            @Value("${wigell.currency.apiplugin.api-key}") String apiKey,
            @Value("${wigell.currency.failOpen:false}") boolean failToOpen
    ) {
        this.client = client;
        this.apiKey = apiKey;
        this.failToOpen = failToOpen;
    }

    @Override
    public BigDecimal convertToEuro(BigDecimal amountSek) {

        if (amountSek == null) {return null;}
        if (amountSek.signum() == 0) {return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);}

        try {
            ResponseEntity<Map> entity = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ENDPOINT)
                            .queryParam("from", "SEK")
                            .queryParam("to", "EUR")
                            .queryParam("amount", amountSek)
                            .build(apiKey))
                    .retrieve()
                    .toEntity(Map.class);

            if (entity.getStatusCode().isError()) {
                String body = entity.hasBody() ? String.valueOf(entity.getBody()) : null;
                throw new CurrencyConversionException(PROVIDER, ENDPOINT, "Empty response");
            }

            Map<String, Object> json = entity.getBody();
            if (json == null) {throw new CurrencyConversionException(PROVIDER, ENDPOINT, "Empty response");}

            BigDecimal amountEur = readEurAmount(json, amountSek);
            return amountEur.setScale(2, RoundingMode.HALF_UP);
        } catch (RestClientResponseException e) {
            if (failToOpen) {return null;}
            throw new CurrencyConversionException(PROVIDER, ENDPOINT, e.getRawStatusCode(),
                    e.getResponseBodyAsString(), "Currency API error");
        } catch (CurrencyConversionException ex) {
            if (failToOpen) return null;
            throw ex;
        } catch (Exception ex) {
            if (failToOpen) return null;
            throw new CurrencyConversionException(PROVIDER, ENDPOINT, ex.getMessage());
        }
    }

    private static BigDecimal readEurAmount(Map<String, Object> json, BigDecimal amountSek) {
        BigDecimal eur = asBig(json.get("result"));
        if (eur != null) return eur;

        Object dataObj = json.get("data");
        if (dataObj instanceof Map<?, ?> data) {
            BigDecimal amount = asBig(data.get("amount"));
            if (amount != null) return amount;

            BigDecimal rate = asBig(data.get("rate"));
            if (rate != null) return amountSek.multiply(rate);
        }

        BigDecimal value = asBig(json.get("value"));
        if (value != null) return value;

        BigDecimal topRate = asBig(json.get("rate"));
        if (topRate != null) return amountSek.multiply(topRate);

        throw new CurrencyConversionException(PROVIDER, ENDPOINT,
                "Unexpected currency API response shape: " + json);
    }

    private static BigDecimal asBig(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal b) return b;
        if (value instanceof Number n) {
            if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
                return BigDecimal.valueOf(n.longValue());
            }
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }
}
