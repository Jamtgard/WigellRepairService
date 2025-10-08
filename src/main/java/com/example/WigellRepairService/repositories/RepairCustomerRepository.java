package com.example.WigellRepairService.repositories;

import com.example.WigellRepairService.entities.RepairCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairCustomerRepository extends JpaRepository<RepairCustomer, Long> {


    @Query("select c.id from RepairCustomer c where lower(c.name) = lower(:name)")
    Optional<Long> findIdByNameIgnoreCase(@Param("name") String name);


}
