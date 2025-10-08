package com.example.WigellRepairService.entities;


import com.example.WigellRepairService.enums.ServiceType;
import jakarta.persistence.*;


@Entity
@Table(name = "repair_technicians")
public class RepairTechnician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_technicians_name", nullable = false)
    private String repairTechniciansName;

    @Enumerated(EnumType.STRING)
    @Column(name = "repair_technicians_speciality", nullable = false)
    private ServiceType repairTechniciansSpeciality;

    @Column(name = "active", nullable = false)
    private boolean active;

    public RepairTechnician() {}
    public RepairTechnician(String repairTechniciansName, ServiceType repairTechniciansSpeciality, boolean active) {
        this.repairTechniciansName = repairTechniciansName;
        this.repairTechniciansSpeciality = repairTechniciansSpeciality;
        this.active = true;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getRepairTechniciansName() {return repairTechniciansName;}
    public void setRepairTechniciansName(String repairTechniciansName) {this.repairTechniciansName = repairTechniciansName;}

    public ServiceType getRepairTechniciansSpeciality() {return repairTechniciansSpeciality;}
    public void setRepairTechniciansSpeciality(ServiceType repairTechniciansSpeciality) {this.repairTechniciansSpeciality = repairTechniciansSpeciality;}

    public boolean isActive() {return active;}
    public void setActive(boolean active) {this.active = active;}
}
