package com.example.WigellRepairService.services;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.DTOs.RepairTechnicianDTO;
import com.example.WigellRepairService.entities.RepairBooking;
import com.example.WigellRepairService.entities.RepairService;
import com.example.WigellRepairService.entities.RepairTechnician;
import com.example.WigellRepairService.enums.BookingStatus;
import com.example.WigellRepairService.enums.ServiceType;
import com.example.WigellRepairService.exceptions.ResourceNotFoundException;
import com.example.WigellRepairService.repositories.RepairBookingRepository;
import com.example.WigellRepairService.repositories.RepairServiceRepository;
import com.example.WigellRepairService.repositories.RepairTechnicianRepository;
import static com.example.WigellRepairService.utilities.LogMethods.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class RepairAdminServiceImpl implements RepairAdminService {

    private final RepairBookingRepository bookingRepository;
    private final RepairServiceRepository serviceRepository;
    private final RepairTechnicianRepository techniciansRepository;

    private final CurrencyService currencyService;

    private static final Logger ACTION_LOGGER = LogManager.getLogger("actionlog");

    public RepairAdminServiceImpl(
            RepairBookingRepository bookingRepository,
            RepairServiceRepository serviceRepository,
            RepairTechnicianRepository techniciansRepository,
            CurrencyService currencyService) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.techniciansRepository = techniciansRepository;
        this.currencyService = currencyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairBookingDTO.Response> listCanceledBookings() {
        List<RepairBooking> bookings = bookingRepository.findByStatusOrderByDateDesc(BookingStatus.CANCELED);
        return bookings.stream().map(this::toBookingResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairBookingDTO.Response> listUpcomingBookings() {
        List<RepairBooking> bookings = bookingRepository.findUpcoming(LocalDate.now());
        return bookings.stream().map(this::toBookingResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairBookingDTO.Response> listPastBookings() {
        List<RepairBooking> bookings = bookingRepository.findPast(LocalDate.now());
        return bookings.stream().map(this::toBookingResponse).toList();
    }

    @Override
    public RepairServiceDTO.Summary addService(RepairServiceDTO.Create request) {

        RepairTechnician technician = getTechnicianByType(request.type());

        RepairService entity = new RepairService(
                request.name(),
                request.type(),
                request.priceSek(),
                technician,
                request.active() != null ? request.active() : true
        );

        RepairService saved = serviceRepository.save(entity);

        ACTION_LOGGER.info(
                "CREATE RepairService{}",
                logBuilder(
                        saved,
                        "id",
                        "repairServiceName",
                        "repairServiceType",
                        "repairServicePriceSek",
                        "active"
                )
        );

        return toSummary(saved);
    }

    @Override
    public RepairServiceDTO.Summary updateService(RepairServiceDTO.Update request) {

        RepairService entity = serviceRepository.findById(request.id()).orElseThrow(()->
                new ResourceNotFoundException("Service", "id", request.id()));

        RepairService beforeUpdate = new RepairService();
        beforeUpdate.setRepairServiceName(entity.getRepairServiceName());
        beforeUpdate.setRepairServiceType(entity.getRepairServiceType());
        beforeUpdate.setRepairServicePriceSek(entity.getRepairServicePriceSek());
        beforeUpdate.setActive(entity.isActive());


        if(request.name() != null && !request.name().isBlank()) {entity.setRepairServiceName(request.name());}
        if(request.priceSek() != null){entity.setRepairServicePriceSek(request.priceSek());}
        if(request.active() != null){entity.setActive(request.active());}

        if (request.type() != null) {
            ServiceType currentType = entity.getRepairServiceType();
            ServiceType newType = request.type();
            if (currentType != newType) {
                entity.setRepairServiceType(newType);
            }
        }

        RepairTechnician currentTech = entity.getRepairServiceTechnician();
        if (currentTech == null
                || !currentTech.isActive()
                || currentTech.getRepairTechniciansSpeciality() != entity.getRepairServiceType()) {
            entity.setRepairServiceTechnician(getTechnicianByType(entity.getRepairServiceType()));
        }

        RepairService saved = serviceRepository.save(entity);

        String delta = logUpdateBuilder(
                beforeUpdate,
                entity,
                "repairServiceName",
                "repairServiceType",
                "repairServicePriceSek",
                "active"
        );

        ACTION_LOGGER.info("UPDATE RepairService id={}{}", entity.getId(), delta);

        return toSummary(saved);
    }

    @Override
    public String removeService(Long id) {
        RepairService entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        if (entity.isActive()) {
            entity.setActive(false);
            serviceRepository.save(entity);

            ACTION_LOGGER.info(
                    "DELETE RepairService{}",
                    logBuilder(
                            entity,
                            "id",
                            "repairServiceName",
                            "repairServiceType",
                            "repairServicePriceSek",
                            "active"
                    )
            );
            return "Service has been deactivated";
        }

        return "Service already deactivated";
    }

    @Override
    public RepairTechnicianDTO.Response addTechnician(RepairTechnicianDTO.Create request) {

        RepairTechnician technician = new RepairTechnician(
                request.name().trim(),
                request.speciality(),
                request.active()
        );

        RepairTechnician saved = techniciansRepository.save(technician);

        ACTION_LOGGER.info(
                "CREATE RepairTechnician{}",
                logBuilder(
                        saved,
                        "id",
                        "repairTechniciansName",
                        "repairTechniciansSpeciality",
                        "active"
                )
        );

        return new RepairTechnicianDTO.Response(
                saved.getId(),
                saved.getRepairTechniciansName(),
                saved.getRepairTechniciansSpeciality(),
                saved.isActive()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairTechnicianDTO.Response> listTechnicians() {
        return techniciansRepository.findAll()
                .stream()
                .map(t -> new RepairTechnicianDTO.Response(
                        t.getId(),
                        t.getRepairTechniciansName(),
                        t.getRepairTechniciansSpeciality(),
                        t.isActive()
                ))
                .toList();
    }

// ----- FUNCTIONAL ----------------------------------------------------------------------------------------------------

    private RepairBookingDTO.Response toBookingResponse(RepairBooking b) {
        BigDecimal priceSek = b.getTotalPriceSek();
        BigDecimal priceEur = currencyService.convertToEuro(priceSek);
        if (priceEur != null) {
            priceEur = priceEur.setScale(2, RoundingMode.HALF_UP);
        }

        return new RepairBookingDTO.Response(
                b.getId(),
                b.getCustomer().getId(),
                b.getService().getId(),
                b.getService().getRepairServiceName(),
                b.getDate(),
                b.getStatus(),
                priceSek,
                priceEur
        );
    }

    private RepairServiceDTO.Summary toSummary(RepairService s) {
        return new RepairServiceDTO.Summary(
                s.getId(),
                s.getRepairServiceName(),
                s.getRepairServiceType(),
                s.getRepairServicePriceSek(),
                s.getRepairServiceTechnician().getRepairTechniciansName()
        );
    }

    @Transactional(readOnly = true)
    protected RepairTechnician getTechnicianByType(ServiceType serviceType) {

        var alternatives = techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(serviceType);

        if (alternatives.isEmpty()) {
            throw new ResourceNotFoundException("Technician", "Speciality", serviceType.name());
        }

        int idx = ThreadLocalRandom.current().nextInt(alternatives.size());
        return alternatives.get(idx);
    }


}
