package com.example.WigellRepairService.controllers;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.DTOs.RepairTechnicianDTO;
import com.example.WigellRepairService.services.RepairAdminServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wigellrepairs")
public class RepairAdminController {

    private final RepairAdminServiceImpl repairAdminService;

    @Autowired
    public RepairAdminController(RepairAdminServiceImpl repairAdminService) {
        this.repairAdminService = repairAdminService;
    }

//---- CRUD - Bookings ------------------------------------------------------------------------------------


    @GetMapping("/listcanceled")
    public ResponseEntity<List<RepairBookingDTO.Response>> listcanceled() {
        return ResponseEntity.ok(repairAdminService.listCanceledBookings());
    }

    @GetMapping("/listupcoming")
    public ResponseEntity<List<RepairBookingDTO.Response>> listUpcoming() {
        return ResponseEntity.ok(repairAdminService.listUpcomingBookings());
    }

    @GetMapping("/listpast")
    public ResponseEntity<List<RepairBookingDTO.Response>> listPast() {
        return ResponseEntity.ok(repairAdminService.listPastBookings());
    }

    @PostMapping("/addservice")
    public ResponseEntity<RepairServiceDTO.Summary> addService(
            @Valid @RequestBody RepairServiceDTO.Create request) {
        return ResponseEntity.ok(repairAdminService.addService(request));
    }

    @PutMapping("/updateservice")
    public ResponseEntity<RepairServiceDTO.Summary> updateService(
            @Valid @RequestBody RepairServiceDTO.Update request) {
        return ResponseEntity.ok(repairAdminService.updateService(request));
    }

    @PutMapping("/removeservice/{id}")
    public ResponseEntity<String> removeService(@PathVariable Long id) {
        return ResponseEntity.ok(repairAdminService.removeService(id));
    }


//---- CRUD - Technicians ---------------------------------------------------------------------------------

    @PostMapping("/addtechnician")
    public ResponseEntity<RepairTechnicianDTO.Response> addTechnician(
            @Valid @RequestBody RepairTechnicianDTO.Create request) {
        return ResponseEntity.ok(repairAdminService.addTechnician(request));
    }

    @GetMapping("/technicians")
    public ResponseEntity<List<RepairTechnicianDTO.Response>> technicians() {
        return ResponseEntity.ok(repairAdminService.listTechnicians());
    }

}
