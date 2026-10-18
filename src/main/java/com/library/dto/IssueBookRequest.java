package com.library.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class IssueBookRequest {

    @NotNull(message = "Please select a book to issue")
    private Long bookId;

    @NotNull(message = "Please select a library member")
    private Long memberId;

    @NotNull(message = "Issue date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate = LocalDate.now();

    @NotNull(message = "Due date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate = LocalDate.now().plusDays(14);

    private String remarks;

    public IssueBookRequest() {
        this.issueDate = LocalDate.now();
        this.dueDate = LocalDate.now().plusDays(14);
    }

    public IssueBookRequest(Long bookId, Long memberId, LocalDate issueDate, LocalDate dueDate, String remarks) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = (issueDate != null) ? issueDate : LocalDate.now();
        this.dueDate = (dueDate != null) ? dueDate : LocalDate.now().plusDays(14);
        this.remarks = remarks;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
