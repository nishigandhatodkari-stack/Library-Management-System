package com.library.service.impl;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookTransactionRepository transactionRepository;

    public BookServiceImpl(BookRepository bookRepository, BookTransactionRepository transactionRepository) {
        this.bookRepository = bookRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchAndFilterBooks(String keyword, String category, String availability) {
        return bookRepository.searchAndFilterBooks(keyword, category, availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return bookRepository.findDistinctCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
    }

    @Override
    public Book saveBook(BookDto bookDto) {
        String cleanIsbn = bookDto.getIsbn().trim();
        if (bookRepository.existsByIsbn(cleanIsbn)) {
            throw new BusinessValidationException("A book with ISBN '" + cleanIsbn + "' already exists.");
        }

        if (bookDto.getTotalCopies() == null || bookDto.getTotalCopies() < 1) {
            throw new BusinessValidationException("Total copies must be at least 1.");
        }

        // When creating a new book, available copies default to total copies if not specified or invalid
        int total = bookDto.getTotalCopies();
        int available = (bookDto.getAvailableCopies() != null) ? bookDto.getAvailableCopies() : total;
        if (available > total) {
            throw new BusinessValidationException("Available copies (" + available + ") cannot exceed total copies (" + total + ").");
        }
        if (available < 0) {
            throw new BusinessValidationException("Available copies cannot be negative.");
        }

        Book book = new Book();
        book.setIsbn(cleanIsbn);
        book.setTitle(bookDto.getTitle().trim());
        book.setAuthor(bookDto.getAuthor().trim());
        book.setCategory(bookDto.getCategory().trim());
        book.setPublisher(bookDto.getPublisher() != null ? bookDto.getPublisher().trim() : null);
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setTotalCopies(total);
        book.setAvailableCopies(available);

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long id, BookDto bookDto) {
        Book existingBook = getBookById(id);

        String cleanIsbn = bookDto.getIsbn().trim();
        if (bookRepository.existsByIsbnAndIdNot(cleanIsbn, id)) {
            throw new BusinessValidationException("Another book already exists with ISBN '" + cleanIsbn + "'.");
        }

        int newTotal = bookDto.getTotalCopies();
        int currentlyIssued = existingBook.getTotalCopies() - existingBook.getAvailableCopies();
        if (newTotal < currentlyIssued) {
            throw new BusinessValidationException("Cannot reduce total copies to " + newTotal +
                    ". Currently " + currentlyIssued + " copies are issued out to members.");
        }

        int newAvailable = (bookDto.getAvailableCopies() != null) ? bookDto.getAvailableCopies() : (newTotal - currentlyIssued);
        if (newAvailable > newTotal) {
            throw new BusinessValidationException("Available copies (" + newAvailable + ") cannot exceed total copies (" + newTotal + ").");
        }
        if (newAvailable < 0) {
            throw new BusinessValidationException("Available copies cannot be negative.");
        }

        existingBook.setIsbn(cleanIsbn);
        existingBook.setTitle(bookDto.getTitle().trim());
        existingBook.setAuthor(bookDto.getAuthor().trim());
        existingBook.setCategory(bookDto.getCategory().trim());
        existingBook.setPublisher(bookDto.getPublisher() != null ? bookDto.getPublisher().trim() : null);
        existingBook.setPublicationYear(bookDto.getPublicationYear());
        existingBook.setTotalCopies(newTotal);
        existingBook.setAvailableCopies(newAvailable);

        return bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = getBookById(id);
        long activeIssues = transactionRepository.countByBookIdAndStatus(id, TransactionStatus.ISSUED);
        if (activeIssues > 0) {
            throw new BusinessValidationException("Cannot delete book '" + book.getTitle() +
                    "' because there are " + activeIssues + " active issue(s) linked to it.");
        }
        bookRepository.delete(book);
    }

    @Override
    public BookDto convertToDto(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setCategory(book.getCategory());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        return dto;
    }
}
