package com.example.WigellRepairService.DTOs;


import com.example.WigellRepairService.enums.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RepairBookingDTO {

    public record CreateRequest(
            @NotNull Long serviceId,
            @NotNull @FutureOrPresent LocalDate date
            ) {}

    public record CancelRequest(
            @NotNull Long bookingId
    ) {}

    public static class Response {
        private Long bookingId;
        private Long customerId;
        private Long serviceId;
        private String serviceName;
        private LocalDate date;
        private BookingStatus status;
        private BigDecimal priceSek;
        private BigDecimal priceEur;

        public Response(Long bookingId, Long customerId, Long serviceId, String serviceName,
                        LocalDate date, BookingStatus status, BigDecimal priceSek) {
            this.bookingId = bookingId;
            this.customerId = customerId;
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.date = date;
            this.status = status;
            this.priceSek = priceSek;
            this.priceEur = null;
        }

        public Response(Long bookingId, Long customerId, Long serviceId, String serviceName,
                        LocalDate date, BookingStatus status, BigDecimal priceSek, BigDecimal priceEur) {
            this.bookingId = bookingId;
            this.customerId = customerId;
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.date = date;
            this.status = status;
            this.priceSek = priceSek;
            this.priceEur = priceEur;
        }

        public Long getBookingId() {return bookingId;}
        public Long getCustomerId() {return customerId;}
        public Long getServiceId() {return serviceId;}
        public String getServiceName() {return serviceName;}
        public LocalDate getDate() {return date;}
        public BookingStatus getStatus() {return status;}
        public BigDecimal getPriceSek() {return priceSek;}
        public BigDecimal getPriceEur() {return priceEur;}

        public void setPriceEur(BigDecimal priceEur) {this.priceEur = priceEur;}
    }
}
