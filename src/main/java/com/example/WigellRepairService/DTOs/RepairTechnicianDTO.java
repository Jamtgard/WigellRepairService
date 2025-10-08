package com.example.WigellRepairService.DTOs;

import com.example.WigellRepairService.enums.ServiceType;
import jakarta.validation.constraints.NotNull;

public final class RepairTechnicianDTO {

    public record Create(
            @NotNull String name,
            @NotNull ServiceType speciality,
            boolean active
    ) {}

    public record Response(
            Long id,
            String name,
            ServiceType speciality,
            boolean active
    ) {}


}
