package com.example.WigellRepairService.repositories;

import com.example.WigellRepairService.entities.RepairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairServiceRepository extends JpaRepository<RepairService, Long> {

    List<RepairService> findAllByActiveTrueOrderByRepairServiceNameAsc();

    Optional<RepairService> findByIdAndActiveTrue(Long id);

}
