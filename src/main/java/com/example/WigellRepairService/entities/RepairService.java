package com.example.WigellRepairService.entities;

import com.example.WigellRepairService.enums.ServiceType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "repair_service")
public class RepairService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String repairServiceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_service_type", nullable = false)
    private ServiceType repairServiceType;

    @Column(name = "price_sek", nullable = false)
    private BigDecimal repairServicePriceSek;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private RepairTechnician repairServiceTechnician;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public RepairService() {}
    public RepairService(String repairServiceName,
                         ServiceType repairServiceType,
                         BigDecimal repairServicePriceSek,
                         RepairTechnician repairServiceTechnician,
                         Boolean active) {
        this.repairServiceName = repairServiceName;
        this.repairServiceType = repairServiceType;
        this.repairServicePriceSek = repairServicePriceSek;
        this.repairServiceTechnician = repairServiceTechnician;
        this.active = active;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getRepairServiceName() {return repairServiceName;}
    public void setRepairServiceName(String repairServiceName) {this.repairServiceName = repairServiceName;}

    public ServiceType getRepairServiceType() {return repairServiceType;}
    public void setRepairServiceType(ServiceType repairServiceType) {this.repairServiceType = repairServiceType;}

    public BigDecimal getRepairServicePriceSek() {return repairServicePriceSek;}
    public void setRepairServicePriceSek(BigDecimal repairServicePriceSek) {this.repairServicePriceSek = repairServicePriceSek;}

    public RepairTechnician getRepairServiceTechnician() {return repairServiceTechnician;}
    public void setRepairServiceTechnician(RepairTechnician repairServiceTechnician) {this.repairServiceTechnician = repairServiceTechnician;}

    public boolean isActive() {return active;}
    public void setActive(boolean active) {this.active = active;}
}
