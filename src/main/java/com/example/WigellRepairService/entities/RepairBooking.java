package com.example.WigellRepairService.entities;

import com.example.WigellRepairService.enums.BookingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "repair_booking")
public class RepairBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private RepairCustomer customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private RepairService service;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private BigDecimal totalPriceSek;

    public RepairBooking() {}
    public RepairBooking(RepairCustomer customer, RepairService service, LocalDate date, BigDecimal totalPriceSek) {
        this.customer = customer;
        this.service = service;
        this.date = date;
        this.totalPriceSek = totalPriceSek;
        this.status = BookingStatus.BOOKED;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public RepairCustomer getCustomer() {return customer;}
    public void setCustomer(RepairCustomer customer) {this.customer = customer;}

    public RepairService getService() {return service;}
    public void setService(RepairService service) {this.service = service;}

    public LocalDate getDate() {return date;}
    public void setDate(LocalDate date) {this.date = date;}

    public BookingStatus getStatus() {return status;}
    public void setStatus(BookingStatus status) {this.status = status;}

    public BigDecimal getTotalPriceSek() {return totalPriceSek;}
    public void setTotalPriceSek(BigDecimal totalPriceSek) {this.totalPriceSek = totalPriceSek;}

}
