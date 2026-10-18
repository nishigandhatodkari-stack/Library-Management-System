package com.library;

import com.library.dto.BookDto;
import com.library.dto.IssueBookRequest;
import com.library.dto.MemberDto;
import com.library.dto.ReturnBookRequest;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.service.BookService;
import com.library.service.BookTransactionService;
import com.library.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LibraryIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookTransactionService transactionService;

    @Test
    @DisplayName("End-to-End Workflow: Add book -> Register member -> Issue book -> Return book")
    void testEndToEndLibraryCycle() {
        // 1. Add Book
        BookDto bookDto = new BookDto();
        bookDto.setIsbn("978-9999999999");
        bookDto.setTitle("Spring Boot in Practice");
        bookDto.setAuthor("Somnath Musib");
        bookDto.setCategory("Programming");
        bookDto.setTotalCopies(3);
        bookDto.setAvailableCopies(3);
        Book book = bookService.saveBook(bookDto);

        assertNotNull(book.getId());
        assertEquals(3, book.getAvailableCopies());

        // 2. Register Member
        MemberDto memberDto = new MemberDto();
        memberDto.setMemberId("TEST-M999");
        memberDto.setFullName("Test Student");
        memberDto.setEmail("student@university.edu");
        memberDto.setPhone("+91 9123456780");
        memberDto.setRegistrationDate(LocalDate.now());
        Member member = memberService.saveMember(memberDto);

        assertNotNull(member.getId());

        // 3. Issue Book
        IssueBookRequest issueReq = new IssueBookRequest(
                book.getId(),
                member.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                "Lab reference"
        );
        BookTransaction txn = transactionService.issueBook(issueReq);

        assertNotNull(txn.getId());
        assertEquals(TransactionStatus.ISSUED, txn.getStatus());

        // Check book stock decreased
        Book updatedBook = bookService.getBookById(book.getId());
        assertEquals(2, updatedBook.getAvailableCopies());

        // 4. Return Book
        ReturnBookRequest returnReq = new ReturnBookRequest(
                txn.getId(),
                LocalDate.now(),
                0.0,
                "Returned in good shape"
        );
        BookTransaction returnedTxn = transactionService.returnBook(returnReq);

        assertEquals(TransactionStatus.RETURNED, returnedTxn.getStatus());

        // Check book stock increased back to initial
        Book returnedBook = bookService.getBookById(book.getId());
        assertEquals(3, returnedBook.getAvailableCopies());
    }
}
