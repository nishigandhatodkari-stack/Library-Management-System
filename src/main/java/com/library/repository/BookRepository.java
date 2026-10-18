package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category ASC")
    List<String> findDistinctCategories();

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR b.category = :category) AND " +
           "(:availability IS NULL OR :availability = '' OR " +
           "(:availability = 'AVAILABLE' AND b.availableCopies > 0) OR " +
           "(:availability = 'OUT_OF_STOCK' AND b.availableCopies = 0)) " +
           "ORDER BY b.title ASC")
    List<Book> searchAndFilterBooks(@Param("keyword") String keyword,
                                   @Param("category") String category,
                                   @Param("availability") String availability);

    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM Book b")
    long countTotalBookCopies();

    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM Book b")
    long countTotalAvailableCopies();
}
