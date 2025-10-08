package com.example.WigellRepairService.DTOs;

import com.example.WigellRepairService.enums.ServiceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.math.BigDecimal;

public final class RepairServiceDTO {

    public record Summary(
            Long id,
            String name,
            ServiceType type,
            BigDecimal priceSek,
            String technicianName
    ) {}

    public record Create(
            @NotNull String name,
            @NotNull ServiceType type,
            @NotNull BigDecimal priceSek,
            @Null Long technicianId,
            @NotNull Boolean active
    ) {}

    public record Update(
            @NotNull Long id,
            String name,
            ServiceType type,
            BigDecimal priceSek,
            Boolean active
    ) {}

}
