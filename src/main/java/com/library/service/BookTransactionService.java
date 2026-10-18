package com.library.service;

import com.library.dto.IssueBookRequest;
import com.library.dto.ReturnBookRequest;
import com.library.entity.BookTransaction;
import com.library.entity.enums.TransactionStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookTransactionService {
    BookTransaction issueBook(IssueBookRequest request);
    BookTransaction returnBook(ReturnBookRequest request);
    BookTransaction returnBookById(Long transactionId, String remarks);
    BookTransaction getTransactionById(Long id);
    List<BookTransaction> getAllTransactions();
    List<BookTransaction> getActiveIssuedTransactions();
    List<BookTransaction> getOverdueTransactions();
    List<BookTransaction> getRecentTransactions(int limit);
    List<BookTransaction> searchTransactions(String keyword, TransactionStatus status, LocalDate startDate, LocalDate endDate);
    long countOverdueTransactions();
}
