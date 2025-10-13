package com.example.WigellRepairService.services;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.entities.RepairBooking;
import com.example.WigellRepairService.entities.RepairCustomer;
import com.example.WigellRepairService.entities.RepairService;
import com.example.WigellRepairService.enums.BookingStatus;
import com.example.WigellRepairService.exceptions.ForbiddenActionException;
import com.example.WigellRepairService.exceptions.InactiveResourceException;
import com.example.WigellRepairService.exceptions.RequestConflictException;
import com.example.WigellRepairService.exceptions.ResourceNotFoundException;
import com.example.WigellRepairService.repositories.RepairBookingRepository;
import com.example.WigellRepairService.repositories.RepairCustomerRepository;
import com.example.WigellRepairService.repositories.RepairServiceRepository;
import com.example.WigellRepairService.utilities.LogMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;


@Service
@Transactional
public class RepairCustomerServiceImpl implements RepairCustomerService {

    private final RepairCustomerRepository repairCustomerRepository;
    private final RepairBookingRepository repairBookingRepository;
    private final RepairServiceRepository repairServiceRepository;

    private final CurrencyService currencyService;

    private static final Logger ACTION_LOGGER = LogManager.getLogger("actionlog");

    public RepairCustomerServiceImpl(
            RepairCustomerRepository repairCustomerRepository,
            RepairBookingRepository repairBookingRepository,
            RepairServiceRepository repairServiceRepository,
            CurrencyService currencyService) {
        this.repairCustomerRepository = repairCustomerRepository;
        this.repairBookingRepository = repairBookingRepository;
        this.repairServiceRepository = repairServiceRepository;
        this.currencyService = currencyService;
    }


    @Override
    @Transactional(readOnly = true)
    public List<RepairServiceDTO.Summary> listServices() {
        return repairServiceRepository.findAllByActiveTrueOrderByRepairServiceNameAsc()
                .stream()
                .map(s -> new RepairServiceDTO.Summary(
                        s.getId(),
                        s.getRepairServiceName(),
                        s.getRepairServiceType(),
                        s.getRepairServicePriceSek(),
                        s.getRepairServiceTechnician().getRepairTechniciansName()
                ))
                .toList();
    }

    @Override
    public RepairBookingDTO.Response bookService(RepairBookingDTO.CreateRequest request, Principal principal) {

        String username = principal.getName();
        Long customerId = repairCustomerRepository.findIdByNameIgnoreCase(username).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "username", username)
        );
        RepairCustomer customer = findCustomerById(customerId);

        RepairService service = findServiceById(request.serviceId());
        if (!service.isActive()){throw new InactiveResourceException("Service", "Inactive");}

        BigDecimal priceSek = service.getRepairServicePriceSek();
        BigDecimal priceEur = currencyService.convertToEuro(priceSek);
        if (priceEur != null) {
            priceEur = priceEur.setScale(2, RoundingMode.HALF_UP);
        }

        RepairBooking booking = new RepairBooking(customer, service, request.date(), priceSek);
        booking.setStatus(BookingStatus.BOOKED);
        booking = repairBookingRepository.save(booking);

        ACTION_LOGGER.info("CREATE RepairBooking by User '{}' :{}",
                username,
                LogMethods.logBuilder(booking, "id", "date", "status", "totalPriceSek")
        );


        return toResponse(booking, priceEur);
    }

    @Override
    public RepairBookingDTO.Response cancelService(RepairBookingDTO.CancelRequest request, Principal principal) {

        String username = principal.getName();
        RepairBooking booking = repairBookingRepository
                .findByIdAndCustomer_NameIgnoreCase(request.bookingId(), username)
                .orElseThrow(()-> new ForbiddenActionException("Cancellation", "invalid user"));

        LocalDate today = LocalDate.now();
        if (!today.isBefore(booking.getDate())) {
            throw new RequestConflictException("Booking", "Cancel", "Late cancellation - Service has already started.");
        }
        if (booking.getStatus() == BookingStatus.CANCELED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RequestConflictException("Booking", "Cancel", "Booking is already finalized or canceled.");
        }

        BookingStatus oldStatus = booking.getStatus();

        booking.setStatus(BookingStatus.CANCELED);
        booking = repairBookingRepository.save(booking);

        ACTION_LOGGER.info("CANCELLED RepairBooking by user '{}' : id={} \n\tstatus: '{}' -> '{}'",
                username,
                booking.getId(),
                oldStatus,
                booking.getStatus()
        );

        BigDecimal priceSek = booking.getTotalPriceSek();
        BigDecimal priceEur = currencyService.convertToEuro(priceSek);
        if (priceEur != null) {
            priceEur = priceEur.setScale(2, RoundingMode.HALF_UP);
        }

        return new RepairBookingDTO.Response(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getService().getId(),
                booking.getService().getRepairServiceName(),
                booking.getDate(),
                booking.getStatus(),
                priceSek,
                priceEur
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairBookingDTO.Response> myBookings(Principal principal) {

        String username = principal.getName();
        List<RepairBooking> bookings = repairBookingRepository.findByCustomerNameIgnoreCaseOrderByDateDesc(username);

        return bookings.stream()
                .map(b -> {
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
                })
                .toList();
    }

//----- Functional -----------------------------------------------------------------------------------------------------

    private RepairCustomer findCustomerById(Long id) {
        return repairCustomerRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Customer", "id", id));
    }

    private RepairService findServiceById(Long id) {
        return repairServiceRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Service", "id", id));
    }

    private RepairBooking findBookingById(Long id) {
        return repairBookingRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException("Booking", "id", id));
    }

    private RepairBookingDTO.Response toResponse(RepairBooking booking) {
        return new RepairBookingDTO.Response(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getService().getId(),
                booking.getService().getRepairServiceName(),
                booking.getDate(),
                booking.getStatus(),
                booking.getTotalPriceSek()
        );
    }

    private RepairBookingDTO.Response toResponse(RepairBooking booking, BigDecimal priceEur) {
        return new RepairBookingDTO.Response(
                booking.getId(),
                booking.getCustomer().getId(),
                booking.getService().getId(),
                booking.getService().getRepairServiceName(),
                booking.getDate(),
                booking.getStatus(),
                booking.getTotalPriceSek(),
                priceEur
        );
    }
}
