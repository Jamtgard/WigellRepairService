package com.example.WigellRepairService.services;

import java.math.BigDecimal;

public interface CurrencyService {
    BigDecimal convertToEuro(BigDecimal amountSek);
}
