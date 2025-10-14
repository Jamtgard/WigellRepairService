package com.example.WigellRepairService.repositories;

import com.example.WigellRepairService.entities.RepairBooking;
import com.example.WigellRepairService.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairBookingRepository extends JpaRepository<RepairBooking, Long> {

    List<RepairBooking> findByCustomerNameIgnoreCaseOrderByDateDesc(String name);

    List<RepairBooking> findByStatusOrderByDateDesc(BookingStatus status);

    @Query("""
      select b from RepairBooking b
      where b.date >= :today and b.status in (com.example.WigellRepairService.enums.BookingStatus.BOOKED,
                                              com.example.WigellRepairService.enums.BookingStatus.ON_GOING)
      order by b.date asc
    """)
    List<RepairBooking> findUpcoming(@Param("today") LocalDate today);

    @Query("""
      select b from RepairBooking b
      where (b.date < :today) or (b.status = com.example.WigellRepairService.enums.BookingStatus.COMPLETED)
      order by b.date desc
    """)
    List<RepairBooking> findPast(@Param("today") LocalDate today);

    Optional<RepairBooking> findByIdAndCustomer_NameIgnoreCase(Long id, String name);

}
