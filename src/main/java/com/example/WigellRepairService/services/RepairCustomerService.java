package com.example.WigellRepairService.services;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;

import java.security.Principal;
import java.util.List;

public interface RepairCustomerService {

    List<RepairServiceDTO.Summary> listServices();

    RepairBookingDTO.Response bookService(RepairBookingDTO.CreateRequest request, Principal principal);

    RepairBookingDTO.Response cancelService(RepairBookingDTO.CancelRequest request, Principal principal);

    List<RepairBookingDTO.Response> myBookings(Principal principal);


}
