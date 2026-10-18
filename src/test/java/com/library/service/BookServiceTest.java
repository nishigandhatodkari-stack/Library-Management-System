package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookTransactionRepository transactionRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private BookDto sampleBookDto;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBookDto = new BookDto();
        sampleBookDto.setIsbn("978-0134685991");
        sampleBookDto.setTitle("Effective Java");
        sampleBookDto.setAuthor("Joshua Bloch");
        sampleBookDto.setCategory("Computer Science");
        sampleBookDto.setTotalCopies(5);
        sampleBookDto.setAvailableCopies(5);

        sampleBook = new Book(
                "978-0134685991",
                "Effective Java",
                "Joshua Bloch",
                "Computer Science",
                "Addison-Wesley",
                2018,
                5,
                5
        );
        sampleBook.setId(1L);
    }

    @Test
    @DisplayName("Should successfully save new book when ISBN is unique")
    void testSaveBook_Success() {
        when(bookRepository.existsByIsbn("978-0134685991")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        Book savedBook = bookService.saveBook(sampleBookDto);

        assertNotNull(savedBook);
        assertEquals("Effective Java", savedBook.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when saving book with duplicate ISBN")
    void testSaveBook_DuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn("978-0134685991")).thenReturn(true);

        BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> bookService.saveBook(sampleBookDto)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BusinessValidationException when available copies exceed total copies")
    void testSaveBook_AvailableExceedsTotal_ThrowsException() {
        sampleBookDto.setTotalCopies(3);
        sampleBookDto.setAvailableCopies(5);
        when(bookRepository.existsByIsbn("978-0134685991")).thenReturn(false);

        assertThrows(
                BusinessValidationException.class,
                () -> bookService.saveBook(sampleBookDto)
        );
    }

    @Test
    @DisplayName("Should prevent deleting book when active loans exist")
    void testDeleteBook_WithActiveLoans_ThrowsException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(transactionRepository.countByBookIdAndStatus(1L, TransactionStatus.ISSUED)).thenReturn(2L);

        BusinessValidationException exception = assertThrows(
                BusinessValidationException.class,
                () -> bookService.deleteBook(1L)
        );

        assertTrue(exception.getMessage().contains("active issue(s)"));
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    @DisplayName("Should delete book when no active loans exist")
    void testDeleteBook_NoActiveLoans_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(transactionRepository.countByBookIdAndStatus(1L, TransactionStatus.ISSUED)).thenReturn(0L);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(sampleBook);
    }
}
