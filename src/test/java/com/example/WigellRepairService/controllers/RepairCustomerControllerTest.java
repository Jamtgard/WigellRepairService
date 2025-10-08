package com.example.WigellRepairService.controllers;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.entities.RepairBooking;
import com.example.WigellRepairService.entities.RepairCustomer;
import com.example.WigellRepairService.entities.RepairService;
import com.example.WigellRepairService.entities.RepairTechnician;
import com.example.WigellRepairService.enums.BookingStatus;
import com.example.WigellRepairService.enums.ServiceType;
import com.example.WigellRepairService.repositories.RepairBookingRepository;
import com.example.WigellRepairService.repositories.RepairCustomerRepository;
import com.example.WigellRepairService.repositories.RepairServiceRepository;
import com.example.WigellRepairService.services.CurrencyServiceImpl;
import com.example.WigellRepairService.services.RepairCustomerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // <CHANGED>
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // <CHANGED>

@WebMvcTest(controllers = RepairCustomerController.class)
@Import(RepairCustomerServiceImpl.class)
class RepairCustomerControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RepairCustomerRepository repairCustomerRepository;
    @MockitoBean RepairBookingRepository repairBookingRepository;
    @MockitoBean RepairServiceRepository repairServiceRepository;
    @MockitoBean CurrencyServiceImpl currencyService;

    private ServiceType TYPE_A;

    @BeforeEach
    void setup() {
        TYPE_A = ServiceType.values()[0];
    }

    private RepairCustomer mkCustomer(long id, String name) {
        RepairCustomer c = new RepairCustomer(name, new ArrayList<>());
        c.setId(id);
        return c;
    }

    private RepairTechnician mkTech(long id, String name, ServiceType speciality, boolean active) {
        RepairTechnician t = new RepairTechnician(name, speciality, active);
        t.setId(id);
        t.setRepairTechniciansName(name);
        t.setRepairTechniciansSpeciality(speciality);
        t.setActive(active);
        return t;
    }

    private RepairService mkService(long id, String name, ServiceType type, BigDecimal price, RepairTechnician tech, boolean active) {
        RepairService s = new RepairService(name, type, price, tech, active);
        s.setId(id);
        return s;
    }

    // --- GET /services -------------------------------------------------------------------------

    @Test
    void services_shouldReturnActiveServiceSummaries() throws Exception {
        RepairTechnician tech = mkTech(10L, "Tina Tech", TYPE_A, true);
        RepairService s1 = mkService(1L, "Alignment", TYPE_A, new BigDecimal("500"), tech, true);
        RepairService s2 = mkService(2L, "Brakes",    TYPE_A, new BigDecimal("900"), tech, true);

        when(repairServiceRepository.findAllByActiveTrueOrderByRepairServiceNameAsc())
                .thenReturn(List.of(s1, s2));

        mockMvc.perform(
                        get("/api/wigellrepairs/services")
                                .with(user("alice"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Alignment")))
                .andExpect(jsonPath("$[0].type", is(TYPE_A.name())))
                .andExpect(jsonPath("$[0].priceSek", is(500)))
                .andExpect(jsonPath("$[0].technicianName", is("Tina Tech")))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    // --- POST /bookservice ---------------------------------------------------------------------

    @Test
    void bookService_shouldCreateBookingAndReturnResponse() throws Exception {
        var date = LocalDate.now().plusDays(2);
        var req = new RepairBookingDTO.CreateRequest(5L, date);

        when(repairCustomerRepository.findIdByNameIgnoreCase("alice")).thenReturn(Optional.of(100L));
        when(repairCustomerRepository.findById(100L)).thenReturn(Optional.of(mkCustomer(100L, "alice")));

        RepairTechnician tech = mkTech(10L, "Tina Tech", TYPE_A, true);
        RepairService service = mkService(5L, "Alignment", TYPE_A, new BigDecimal("799"), tech, true);
        when(repairServiceRepository.findById(5L)).thenReturn(Optional.of(service));

        when(currencyService.convertToEuro(new BigDecimal("799"))).thenReturn(new BigDecimal("71.818"));

        when(repairBookingRepository.save(argThat(new ArgumentMatcher<RepairBooking>() {
            @Override public boolean matches(RepairBooking b) {
                return b.getCustomer().getId().equals(100L)
                        && b.getService().getId().equals(5L)
                        && b.getDate().equals(date)
                        && b.getStatus() == BookingStatus.BOOKED
                        && b.getTotalPriceSek().equals(new BigDecimal("799"));
            }
        }))).thenAnswer(inv -> {
            RepairBooking b = inv.getArgument(0);
            b.setId(999L);
            return b;
        });

        mockMvc.perform(
                        post("/api/wigellrepairs/bookservice")
                                .with(user("alice"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId", is(999)))
                .andExpect(jsonPath("$.customerId", is(100)))
                .andExpect(jsonPath("$.serviceId", is(5)))
                .andExpect(jsonPath("$.serviceName", is("Alignment")))
                .andExpect(jsonPath("$.status", is("BOOKED")))
                .andExpect(jsonPath("$.priceSek", is(799)))
                .andExpect(jsonPath("$.priceEur", is(71.82)));
    }

    @Test
    void bookService_shouldFailValidationForPastDate() throws Exception {
        var pastReq = new RepairBookingDTO.CreateRequest(5L, LocalDate.now().minusDays(1));

        mockMvc.perform(
                        post("/api/wigellrepairs/bookservice")
                                .with(user("alice"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(pastReq))
                )
                .andExpect(status().isBadRequest());
    }

    // --- GET /mybookings -----------------------------------------------------------------------

    @Test
    void myBookings_shouldReturnUserBookings() throws Exception {
        RepairTechnician tech = mkTech(10L, "Tina Tech", TYPE_A, true);
        RepairService s  = mkService(5L, "Alignment", TYPE_A, new BigDecimal("799"), tech, true);
        RepairCustomer c = mkCustomer(100L, "alice");

        RepairBooking b1 = new RepairBooking(c, s, LocalDate.now().plusDays(4), new BigDecimal("799"));
        b1.setId(1L);
        b1.setStatus(BookingStatus.BOOKED);

        when(repairBookingRepository.findByCustomerNameIgnoreCaseOrderByDateDesc("alice"))
                .thenReturn(List.of(b1));

        mockMvc.perform(
                        get("/api/wigellrepairs/mybookings")
                                .with(user("alice"))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingId", is(1)))
                .andExpect(jsonPath("$[0].customerId", is(100)))
                .andExpect(jsonPath("$[0].serviceId", is(5)))
                .andExpect(jsonPath("$[0].serviceName", is("Alignment")))
                .andExpect(jsonPath("$[0].status", is("BOOKED")));
    }

    // --- PUT /cancelbooking --------------------------------------------------------------------

    @Nested
    class CancelBooking {

        @Test
        void cancelBooking_success() throws Exception {
            RepairTechnician tech = mkTech(10L, "Tina Tech", TYPE_A, true);
            RepairService s  = mkService(5L, "Alignment", TYPE_A, new BigDecimal("799"), tech, true);
            RepairCustomer c = mkCustomer(100L, "alice");

            RepairBooking booking = new RepairBooking(c, s, LocalDate.now().plusDays(3), new BigDecimal("799"));
            booking.setId(55L);
            booking.setStatus(BookingStatus.BOOKED);

            when(repairBookingRepository.findByIdAndCustomer_NameIgnoreCase(55L, "alice"))
                    .thenReturn(Optional.of(booking));

            when(repairBookingRepository.save(any())).thenAnswer(inv -> {
                RepairBooking b = inv.getArgument(0);
                b.setStatus(BookingStatus.CANCELED);
                return b;
            });

            var req = new RepairBookingDTO.CancelRequest(55L);

            mockMvc.perform(
                            put("/api/wigellrepairs/cancelbooking")
                                    .with(user("alice")) // <CHANGED> authenticate
                                    .with(csrf())        // <CHANGED> CSRF for PUT
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.bookingId", is(55)))
                    .andExpect(jsonPath("$.status", is("CANCELED")));
        }

        @Test
        void cancelBooking_tooLate_conflict409() throws Exception {
            RepairTechnician tech = mkTech(10L, "Tina Tech", TYPE_A, true);
            RepairService s  = mkService(5L, "Alignment", TYPE_A, new BigDecimal("799"), tech, true);
            RepairCustomer c = mkCustomer(100L, "alice");

            RepairBooking booking = new RepairBooking(c, s, LocalDate.now(), new BigDecimal("799"));
            booking.setId(56L);
            booking.setStatus(BookingStatus.BOOKED);

            when(repairBookingRepository.findByIdAndCustomer_NameIgnoreCase(56L, "alice"))
                    .thenReturn(Optional.of(booking));

            var req = new RepairBookingDTO.CancelRequest(56L);

            mockMvc.perform(
                            put("/api/wigellrepairs/cancelbooking")
                                    .with(user("alice"))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isConflict());
        }

        @Test
        void cancelBooking_wrongUser_forbidden403() throws Exception {
            when(repairBookingRepository.findByIdAndCustomer_NameIgnoreCase(999L, "alice"))
                    .thenReturn(Optional.empty());

            var req = new RepairBookingDTO.CancelRequest(999L);

            mockMvc.perform(
                            put("/api/wigellrepairs/cancelbooking")
                                    .with(user("alice"))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isForbidden());
        }
    }
}
