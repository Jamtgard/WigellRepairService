package com.example.WigellRepairService.controllers;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.services.RepairCustomerService;
import com.example.WigellRepairService.services.RepairCustomerServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wigellrepairs")
public class RepairCustomerController {

    private final RepairCustomerServiceImpl repairCustomerService;

    @Autowired
    public RepairCustomerController(RepairCustomerServiceImpl repairCustomerService) {
        this.repairCustomerService = repairCustomerService;
    }

    @GetMapping("/services")
    public ResponseEntity<List<RepairServiceDTO.Summary>> services() {
        return ResponseEntity.ok(repairCustomerService.listServices());
    }

    @PostMapping("/bookservice")
    public ResponseEntity<RepairBookingDTO.Response> BookService(
            @Valid @RequestBody RepairBookingDTO.CreateRequest request,
            Principal principal) {
        return ResponseEntity.ok(repairCustomerService.bookService(request, principal));
    }


    @PutMapping("/cancelbooking")
    public ResponseEntity<RepairBookingDTO.Response> cancelBooking(@Valid @RequestBody RepairBookingDTO.CancelRequest request, Principal principal) {
        return ResponseEntity.ok(repairCustomerService.cancelService(request, principal));
    }

    @GetMapping("/mybookings")
    public ResponseEntity<List<RepairBookingDTO.Response>> MyBookings(Principal principal) {
        return ResponseEntity.ok(repairCustomerService.myBookings(principal));
    }

}
