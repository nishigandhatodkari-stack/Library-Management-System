package com.library.service;

import com.library.dto.IssueBookRequest;
import com.library.dto.ReturnBookRequest;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.impl.BookTransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookTransactionServiceTest {

    @Mock
    private BookTransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BookTransactionServiceImpl transactionService;

    private Book testBook;
    private Member testMember;
    private BookTransaction testTransaction;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "dailyFineRate", 2.0);

        testBook = new Book("978-0134685991", "Effective Java", "Joshua Bloch", "CS", "Addison", 2018, 5, 5);
        testBook.setId(10L);

        testMember = new Member("MEM-1001", "Aarav Sharma", "aarav@example.com", "+91 9876543210", "Delhi", LocalDate.now());
        testMember.setId(20L);

        testTransaction = new BookTransaction("TXN-20261018-TEST", testBook, testMember, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));
        testTransaction.setId(100L);
    }

    @Test
    @DisplayName("Should successfully issue book, decrement stock, and create transaction")
    void testIssueBook_Success() {
        IssueBookRequest request = new IssueBookRequest(10L, 20L, LocalDate.now(), LocalDate.now().plusDays(14), "Test issue");

        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(transactionRepository.existsByBookIdAndMemberIdAndStatus(10L, 20L, TransactionStatus.ISSUED)).thenReturn(false);
        when(transactionRepository.save(any(BookTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookTransaction txn = transactionService.issueBook(request);

        assertNotNull(txn);
        assertEquals(4, testBook.getAvailableCopies());
        assertEquals(TransactionStatus.ISSUED, txn.getStatus());
        verify(bookRepository, times(1)).save(testBook);
        verify(transactionRepository, times(1)).save(any(BookTransaction.class));
    }

    @Test
    @DisplayName("Should reject issuing book when stock is zero")
    void testIssueBook_OutOfStock_ThrowsException() {
        testBook.setAvailableCopies(0);
        IssueBookRequest request = new IssueBookRequest(10L, 20L, LocalDate.now(), LocalDate.now().plusDays(14), "Out of stock");

        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> transactionService.issueBook(request)
        );

        assertTrue(ex.getMessage().contains("out of stock"));
        verify(transactionRepository, never()).save(any(BookTransaction.class));
    }

    @Test
    @DisplayName("Should prevent member from borrowing duplicate copy of same book simultaneously")
    void testIssueBook_DuplicateLoan_ThrowsException() {
        IssueBookRequest request = new IssueBookRequest(10L, 20L, LocalDate.now(), LocalDate.now().plusDays(14), "Duplicate loan");

        when(bookRepository.findById(10L)).thenReturn(Optional.of(testBook));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(transactionRepository.existsByBookIdAndMemberIdAndStatus(10L, 20L, TransactionStatus.ISSUED)).thenReturn(true);

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> transactionService.issueBook(request)
        );

        assertTrue(ex.getMessage().contains("already has an active copy"));
        verify(transactionRepository, never()).save(any(BookTransaction.class));
    }

    @Test
    @DisplayName("Should process return, increment stock, and mark status as RETURNED")
    void testReturnBook_Success() {
        testBook.setAvailableCopies(4);
        ReturnBookRequest request = new ReturnBookRequest(100L, LocalDate.now(), 0.0, "Returned in person");

        when(transactionRepository.findById(100L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(BookTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookTransaction returnedTxn = transactionService.returnBook(request);

        assertNotNull(returnedTxn);
        assertEquals(TransactionStatus.RETURNED, returnedTxn.getStatus());
        assertEquals(5, testBook.getAvailableCopies());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("Should prevent returning a book twice")
    void testReturnBook_DoubleReturn_ThrowsException() {
        testTransaction.setStatus(TransactionStatus.RETURNED);
        testTransaction.setReturnDate(LocalDate.now().minusDays(1));
        ReturnBookRequest request = new ReturnBookRequest(100L, LocalDate.now(), 0.0, "Try returning again");

        when(transactionRepository.findById(100L)).thenReturn(Optional.of(testTransaction));

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> transactionService.returnBook(request)
        );

        assertTrue(ex.getMessage().contains("already been returned"));
    }

    @Test
    @DisplayName("Should calculate fine properly when book is returned past due date")
    void testReturnBook_OverdueWithFine() {
        // Due 5 days ago, return today -> 5 overdue days * $2.00 = $10.00
        testTransaction.setDueDate(LocalDate.now().minusDays(5));
        testBook.setAvailableCopies(4);
        ReturnBookRequest request = new ReturnBookRequest(100L, LocalDate.now(), null, "Late return");

        when(transactionRepository.findById(100L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(BookTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookTransaction returnedTxn = transactionService.returnBook(request);

        assertEquals(10.0, returnedTxn.getFineAmount());
        assertEquals(TransactionStatus.RETURNED, returnedTxn.getStatus());
    }
}
