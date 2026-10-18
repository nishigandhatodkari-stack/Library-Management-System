package com.library.entity;

import com.library.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entity representing an issue and return transaction between a member and a book.
 */
@Entity
@Table(name = "book_transactions", indexes = {
        @Index(name = "idx_txn_code", columnList = "transaction_code", unique = true),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_due_date", columnList = "due_date")
})
public class BookTransaction extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 40)
    private String transactionCode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.ISSUED;

    @Column(name = "fine_amount")
    private Double fineAmount = 0.0;

    @Column(length = 255)
    private String remarks;

    public BookTransaction() {
    }

    public BookTransaction(String transactionCode, Book book, Member member, LocalDate issueDate, LocalDate dueDate) {
        this.transactionCode = transactionCode;
        this.book = book;
        this.member = member;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = TransactionStatus.ISSUED;
        this.fineAmount = 0.0;
    }

    /**
     * Checks if this transaction is overdue compared to the given reference date (usually today).
     */
    public boolean isCurrentlyOverdue(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }
        if (this.returnDate == null) {
            return referenceDate.isAfter(this.dueDate);
        }
        return this.returnDate.isAfter(this.dueDate);
    }

    /**
     * Calculates the overdue days based on today's date (if unreturned) or the actual return date.
     */
    public long calculateOverdueDays(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }
        LocalDate compareDate = (this.returnDate != null) ? this.returnDate : referenceDate;
        if (compareDate.isAfter(this.dueDate)) {
            return ChronoUnit.DAYS.between(this.dueDate, compareDate);
        }
        return 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
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

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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
