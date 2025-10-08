package com.example.WigellRepairService.services;


import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.DTOs.RepairTechnicianDTO;

import java.util.List;

public interface RepairAdminService {

    List<RepairBookingDTO.Response> listCanceledBookings();
    List<RepairBookingDTO.Response> listUpcomingBookings();
    List<RepairBookingDTO.Response> listPastBookings();

    RepairServiceDTO.Summary addService(RepairServiceDTO.Create request);
    RepairServiceDTO.Summary updateService(RepairServiceDTO.Update request);
    String removeService(Long id);

    RepairTechnicianDTO.Response addTechnician(RepairTechnicianDTO.Create request);
    List<RepairTechnicianDTO.Response> listTechnicians();

}
