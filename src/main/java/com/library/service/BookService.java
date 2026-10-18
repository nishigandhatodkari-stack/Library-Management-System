package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Book;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks();
    List<Book> searchAndFilterBooks(String keyword, String category, String availability);
    List<String> getAllCategories();
    Book getBookById(Long id);
    Book getBookByIsbn(String isbn);
    Book saveBook(BookDto bookDto);
    Book updateBook(Long id, BookDto bookDto);
    void deleteBook(Long id);
    BookDto convertToDto(Book book);
}
