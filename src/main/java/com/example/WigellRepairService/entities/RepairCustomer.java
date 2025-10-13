package com.example.WigellRepairService.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_customer",
        uniqueConstraints = @UniqueConstraint(name = "uk_repair_customer_name", columnNames = "name")
)
public class RepairCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<RepairBooking> bookings = new ArrayList<>();

    public RepairCustomer() {}
    public RepairCustomer(String name, List<RepairBooking> bookings) {
        this.name = name;
        this.bookings = bookings;
    }


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() { return name; }
    public void setName(String name) {}
    public List<RepairBooking> getBookings() { return bookings; }
    public void setBookings(List<RepairBooking> bookings) {}
}
