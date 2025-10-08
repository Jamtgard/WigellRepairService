package com.example.WigellRepairService.repositories;

import com.example.WigellRepairService.entities.RepairTechnician;
import com.example.WigellRepairService.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairTechniciansRepository extends JpaRepository<RepairTechnician, Long> {

    List<RepairTechnician> findByRepairTechniciansSpecialityAndActiveTrue(ServiceType speciality);

}
