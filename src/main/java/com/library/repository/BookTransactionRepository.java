package com.library.repository;

import com.library.entity.BookTransaction;
import com.library.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookTransactionRepository extends JpaRepository<BookTransaction, Long> {

    Optional<BookTransaction> findByTransactionCode(String transactionCode);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COUNT(t) FROM BookTransaction t WHERE t.status = 'ISSUED' AND t.dueDate < :currentDate")
    long countOverdueTransactions(@Param("currentDate") LocalDate currentDate);

    long countByMemberIdAndStatus(Long memberId, TransactionStatus status);

    long countByBookIdAndStatus(Long bookId, TransactionStatus status);

    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, TransactionStatus status);

    List<BookTransaction> findTop10ByOrderByIssueDateDescIdDesc();

    List<BookTransaction> findByStatusOrderByDueDateAsc(TransactionStatus status);

    @Query("SELECT t FROM BookTransaction t WHERE t.status = 'ISSUED' AND t.dueDate < :currentDate ORDER BY t.dueDate ASC")
    List<BookTransaction> findActiveOverdueTransactions(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT t FROM BookTransaction t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:startDate IS NULL OR t.issueDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.issueDate <= :endDate) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(t.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.book.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.book.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.member.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.member.memberId) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.issueDate DESC, t.id DESC")
    List<BookTransaction> searchTransactions(@Param("keyword") String keyword,
                                            @Param("status") TransactionStatus status,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
