package com.library.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReturnBookRequest {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotNull(message = "Return date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate = LocalDate.now();

    private Double fineAmount = 0.0;

    private String remarks;

    public ReturnBookRequest() {
        this.returnDate = LocalDate.now();
    }

    public ReturnBookRequest(Long transactionId, LocalDate returnDate, Double fineAmount, String remarks) {
        this.transactionId = transactionId;
        this.returnDate = (returnDate != null) ? returnDate : LocalDate.now();
        this.fineAmount = (fineAmount != null) ? fineAmount : 0.0;
        this.remarks = remarks;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
