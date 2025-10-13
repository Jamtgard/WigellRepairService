package com.example.WigellRepairService.services;

import com.example.WigellRepairService.DTOs.RepairBookingDTO;
import com.example.WigellRepairService.DTOs.RepairServiceDTO;
import com.example.WigellRepairService.DTOs.RepairTechnicianDTO;
import com.example.WigellRepairService.entities.RepairBooking;
import com.example.WigellRepairService.entities.RepairCustomer;
import com.example.WigellRepairService.entities.RepairService;
import com.example.WigellRepairService.entities.RepairTechnician;
import com.example.WigellRepairService.enums.BookingStatus;
import com.example.WigellRepairService.enums.ServiceType;
import com.example.WigellRepairService.exceptions.ResourceNotFoundException;
import com.example.WigellRepairService.repositories.RepairBookingRepository;
import com.example.WigellRepairService.repositories.RepairServiceRepository;
import com.example.WigellRepairService.repositories.RepairTechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairAdminServiceImplTest {

    @Mock RepairBookingRepository bookingRepository;
    @Mock RepairServiceRepository serviceRepository;
    @Mock RepairTechnicianRepository techniciansRepository;

    @Mock CurrencyService currencyService;

    @InjectMocks RepairAdminServiceImpl service;

    private ServiceType typeA;
    private ServiceType typeB;

    @BeforeEach
    void setup() {
        ServiceType[] all = ServiceType.values();
        assertTrue(all.length >= 1);
        typeA = all[0];
        typeB = all.length > 1 ? all[1] : all[0];
    }

    @Test
    void listCanceledBookings_mapsEntities() {
        RepairCustomer customer = mkCustomer(10L, "Alice");
        RepairTechnician tech = mkTech(20L, "Tech1", typeA, true);
        RepairService service = mkService(30L, "Srv", typeA, new BigDecimal("999"), tech, true);
        RepairBooking booking = mkBooking(1L, customer, service, LocalDate.now(), new BigDecimal("999"), BookingStatus.CANCELED);

        when(bookingRepository.findByStatusOrderByDateDesc(BookingStatus.CANCELED))
                .thenReturn(List.of(booking));

        when(currencyService.convertToEuro(any(BigDecimal.class)))
                .thenReturn(new BigDecimal("12.3"));

        List<RepairBookingDTO.Response> out = this.service.listCanceledBookings();

        assertEquals(1, out.size());
        RepairBookingDTO.Response r = out.getFirst();
        assertEquals(1L, r.getBookingId());
        assertEquals(10L, r.getCustomerId());
        assertEquals(30L, r.getServiceId());
        assertEquals("Srv", r.getServiceName());
        assertEquals(BookingStatus.CANCELED, r.getStatus());
        assertEquals(new BigDecimal("999"), r.getPriceSek());

        assertNotNull(r.getPriceEur());
        verify(currencyService, atLeastOnce()).convertToEuro(any(BigDecimal.class));
    }

    @Test
    void listUpcomingBookings_mapsEntities() {
        RepairCustomer customer = mkCustomer(11L, "Bob");
        RepairTechnician tech = mkTech(21L, "Tech2", typeA, true);
        RepairService service = mkService(31L, "ServiceUp", typeA, new BigDecimal("100"), tech, true);
        RepairBooking booking = mkBooking(2L, customer, service, LocalDate.now().plusDays(3), new BigDecimal("100"), BookingStatus.BOOKED);

        when(bookingRepository.findUpcoming(any(LocalDate.class))).thenReturn(List.of(booking));

        List<RepairBookingDTO.Response> out = this.service.listUpcomingBookings();
        assertEquals(1, out.size());
        assertEquals(2L, out.getFirst().getBookingId());

        verify(currencyService, atLeastOnce()).convertToEuro(any(BigDecimal.class));
    }

    @Test
    void listPastBookings_mapsEntities() {
        RepairCustomer customer = mkCustomer(12L, "Carol");
        RepairTechnician tech = mkTech(22L, "Tech3", typeA, true);
        RepairService service = mkService(32L, "ServicePast", typeA, new BigDecimal("200"), tech, true);
        RepairBooking b = mkBooking(3L, customer, service, LocalDate.now().minusDays(5), new BigDecimal("200"), BookingStatus.BOOKED);

        when(bookingRepository.findPast(any(LocalDate.class))).thenReturn(List.of(b));

        List<RepairBookingDTO.Response> out = this.service.listPastBookings();
        assertEquals(1, out.size());
        assertEquals(3L, out.getFirst().getBookingId());

        verify(currencyService, atLeastOnce()).convertToEuro(any(BigDecimal.class));
    }

    @Test
    void addService_saves_andReturnsSummary() {
        RepairTechnician tech = mkTech(100L, "ChosenTech", typeA, true);
        when(techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(typeA)).thenReturn(List.of(tech));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new RepairServiceDTO.Create("Brake Service", typeA, new BigDecimal("1499"), null, true);

        var out = service.addService(req);

        assertEquals("Brake Service", out.name());
        assertEquals(typeA, out.type());
        assertEquals(new BigDecimal("1499"), out.priceSek());
        assertEquals("ChosenTech", out.technicianName());
    }

    @Test
    void updateService_throwsIfNotFound() {
        when(serviceRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        var req = new RepairServiceDTO.Update(999L, null, null, null, null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateService(req));
    }

    @Test
    void updateService_updatesAndReassignsTechnicianWhenNeeded() {
        RepairTechnician techA = mkTech(200L, "OldTech", typeA, false);
        RepairService existing = mkService(321L, "OldName", typeA, new BigDecimal("1000"), techA, true);

        when(serviceRepository.findById(321L)).thenReturn(java.util.Optional.of(existing));

        RepairTechnician techB = mkTech(201L, "NewTech", typeB, true);
        when(techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(typeB)).thenReturn(List.of(techB));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new RepairServiceDTO.Update(321L, "NewName", typeB, new BigDecimal("1999"), false);

        var out = service.updateService(req);

        assertEquals("NewName", out.name());
        assertEquals(new BigDecimal("1999"), out.priceSek());
        assertEquals("NewTech", out.technicianName());
        assertNotNull(existing.getRepairServiceTechnician());
        assertFalse(existing.isActive());
    }

    @Test
    void updateService_keepsTypeWhenSame_andReassignsTechnicianOnMismatchSpeciality() {
        RepairTechnician wrongTech = mkTech(300L, "WrongTech", typeB, true);
        RepairService existing = mkService(654L, "Srv", typeA, new BigDecimal("500"), wrongTech, true);

        when(serviceRepository.findById(654L)).thenReturn(java.util.Optional.of(existing));
        RepairTechnician correctTech = mkTech(301L, "CorrectTech", typeA, true);
        when(techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(typeA)).thenReturn(List.of(correctTech));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new RepairServiceDTO.Update(654L, null, null, null, null);

        var out = service.updateService(req);

        assertEquals(typeA, out.type());
        assertEquals("CorrectTech", out.technicianName());
    }

    @Test
    void removeService_deactivatesActiveService_andReturnsMessage() {
        RepairTechnician tech = mkTech(1L, "T", typeA, true);
        RepairService entity = mkService(777L, "Srv", typeA, new BigDecimal("1"), tech, true);

        when(serviceRepository.findById(777L)).thenReturn(java.util.Optional.of(entity));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String msg = service.removeService(777L);

        assertEquals("Service has been deactivated", msg);
        assertFalse(entity.isActive());
        verify(serviceRepository).save(entity);
    }

    @Test
    void removeService_returnsAlreadyDeactivatedMessage_whenInactive() {
        RepairTechnician tech = mkTech(2L, "T", typeA, true);
        RepairService entity = mkService(778L, "Srv", typeA, new BigDecimal("1"), tech, false);

        when(serviceRepository.findById(778L)).thenReturn(java.util.Optional.of(entity));

        String msg = service.removeService(778L);

        assertEquals("Service already deactivated", msg);
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void removeService_throwsWhenNotFound() {
        when(serviceRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.removeService(1L));
    }

    @Test
    void addTechnician_saves_andReturnsResponse() {
        when(techniciansRepository.save(any())).thenAnswer(inv -> {
            RepairTechnician t = inv.getArgument(0);
            t.setId(999L);
            return t;
        });

        var req = new RepairTechnicianDTO.Create("Greta Gear", typeA, true);
        var out = service.addTechnician(req);

        assertEquals(999L, out.id());
        assertEquals("Greta Gear", out.name());
        assertEquals(typeA, out.speciality());
        assertTrue(out.active());
    }

    @Test
    void listTechnicians_mapsAll() {
        RepairTechnician t1 = mkTech(10L, "A", typeA, true);
        RepairTechnician t2 = mkTech(11L, "B", typeB, false);
        when(techniciansRepository.findAll()).thenReturn(List.of(t1, t2));

        var out = service.listTechnicians();

        assertEquals(2, out.size());
        assertEquals(10L, out.get(0).id());
        assertEquals(11L, out.get(1).id());
        assertEquals("A", out.get(0).name());
        assertEquals("B", out.get(1).name());
    }

    @Test
    void getTechnicianByType_returnsActiveWithSpeciality() {
        RepairTechnician t = mkTech(55L, "SpecTech", typeA, true);
        when(techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(typeA))
                .thenReturn(List.of(t));

        RepairTechnician picked = service.getTechnicianByType(typeA);

        assertEquals("SpecTech", picked.getRepairTechniciansName());
        assertTrue(picked.isActive());
        assertEquals(typeA, picked.getRepairTechniciansSpeciality());
    }

    @Test
    void getTechnicianByType_throwsWhenEmpty() {
        when(techniciansRepository.findByRepairTechniciansSpecialityAndActiveTrue(typeA))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> service.getTechnicianByType(typeA));
    }

// ----- FUNCTIONAL ----------------------------------------------------------------------------------------------------

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

    private RepairService mkService(Long id, String name, ServiceType type, BigDecimal price, RepairTechnician tech, boolean active) {
        RepairService s = new RepairService(name, type, price, tech, active);
        if (id != null) s.setId(id);
        return s;
    }

    private RepairBooking mkBooking(long id, RepairCustomer c, RepairService s, LocalDate date, BigDecimal price, BookingStatus status) {
        RepairBooking b = new RepairBooking(c, s, date, price);
        b.setId(id);
        b.setStatus(status);
        return b;
    }

}
