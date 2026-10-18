package com.library.service.impl;

import com.library.dto.IssueBookRequest;
import com.library.dto.ReturnBookRequest;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.BookTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BookTransactionServiceImpl implements BookTransactionService {

    private final BookTransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Value("${app.library.daily-fine-rate:2.0}")
    private double dailyFineRate;

    public BookTransactionServiceImpl(BookTransactionRepository transactionRepository,
                                     BookRepository bookRepository,
                                     MemberRepository memberRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public BookTransaction issueBook(IssueBookRequest request) {
        if (request.getBookId() == null) {
            throw new BusinessValidationException("Please select a book to issue.");
        }
        if (request.getMemberId() == null) {
            throw new BusinessValidationException("Please select a member.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + request.getBookId()));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + request.getMemberId()));

        if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
            throw new BusinessValidationException("Cannot issue book: Member '" + member.getFullName() + "' is currently inactive.");
        }

        if (!book.isAvailable()) {
            throw new BusinessValidationException("Book '" + book.getTitle() + "' is currently out of stock (Available: 0).");
        }

        // Prevent duplicate active issue of the same book to the same member
        boolean alreadyIssued = transactionRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), member.getId(), TransactionStatus.ISSUED);
        if (alreadyIssued) {
            throw new BusinessValidationException("Member '" + member.getFullName() +
                    "' already has an active copy of '" + book.getTitle() + "' issued. Please return it before issuing another copy.");
        }

        LocalDate issueDate = (request.getIssueDate() != null) ? request.getIssueDate() : LocalDate.now();
        LocalDate dueDate = (request.getDueDate() != null) ? request.getDueDate() : issueDate.plusDays(14);

        if (dueDate.isBefore(issueDate)) {
            throw new BusinessValidationException("Due date cannot be earlier than issue date.");
        }

        // Decrement available copies
        book.decreaseAvailableCopies();
        bookRepository.save(book);

        // Generate clean transaction code: TXN-YYYYMMDD-XXXX
        String datePrefix = issueDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String transactionCode = "TXN-" + datePrefix + "-" + randomSuffix;

        BookTransaction transaction = new BookTransaction();
        transaction.setTransactionCode(transactionCode);
        transaction.setBook(book);
        transaction.setMember(member);
        transaction.setIssueDate(issueDate);
        transaction.setDueDate(dueDate);
        transaction.setStatus(TransactionStatus.ISSUED);
        transaction.setFineAmount(0.0);
        transaction.setRemarks(request.getRemarks());

        return transactionRepository.save(transaction);
    }

    @Override
    public BookTransaction returnBook(ReturnBookRequest request) {
        if (request.getTransactionId() == null) {
            throw new BusinessValidationException("Transaction ID is required to process return.");
        }

        BookTransaction transaction = getTransactionById(request.getTransactionId());

        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            throw new BusinessValidationException("This book has already been returned on " + transaction.getReturnDate() + ".");
        }

        LocalDate returnDate = (request.getReturnDate() != null) ? request.getReturnDate() : LocalDate.now();
        if (returnDate.isBefore(transaction.getIssueDate())) {
            throw new BusinessValidationException("Return date cannot be earlier than issue date (" + transaction.getIssueDate() + ").");
        }

        transaction.setReturnDate(returnDate);

        // Calculate overdue days and fine
        long overdueDays = transaction.calculateOverdueDays(returnDate);
        double calculatedFine = (request.getFineAmount() != null && request.getFineAmount() > 0)
                ? request.getFineAmount()
                : (overdueDays * dailyFineRate);

        transaction.setFineAmount(calculatedFine);
        transaction.setStatus(TransactionStatus.RETURNED);

        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            String updatedRemarks = (transaction.getRemarks() != null && !transaction.getRemarks().isBlank())
                    ? transaction.getRemarks() + " | Return: " + request.getRemarks()
                    : request.getRemarks();
            transaction.setRemarks(updatedRemarks);
        }

        // Increase available copies on book
        Book book = transaction.getBook();
        book.increaseAvailableCopies();
        bookRepository.save(book);

        return transactionRepository.save(transaction);
    }

    @Override
    public BookTransaction returnBookById(Long transactionId, String remarks) {
        ReturnBookRequest request = new ReturnBookRequest();
        request.setTransactionId(transactionId);
        request.setReturnDate(LocalDate.now());
        request.setRemarks(remarks);
        return returnBook(request);
    }

    @Override
    @Transactional(readOnly = true)
    public BookTransaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookTransaction> getActiveIssuedTransactions() {
        return transactionRepository.findByStatusOrderByDueDateAsc(TransactionStatus.ISSUED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookTransaction> getOverdueTransactions() {
        return transactionRepository.findActiveOverdueTransactions(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookTransaction> getRecentTransactions(int limit) {
        return transactionRepository.findTop10ByOrderByIssueDateDescIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookTransaction> searchTransactions(String keyword, TransactionStatus status, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.searchTransactions(keyword, status, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOverdueTransactions() {
        return transactionRepository.countOverdueTransactions(LocalDate.now());
    }
}
