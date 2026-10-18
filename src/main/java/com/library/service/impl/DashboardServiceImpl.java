package com.library.service.impl;

import com.library.dto.DashboardStatsDto;
import com.library.entity.enums.TransactionStatus;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookTransactionRepository transactionRepository;

    public DashboardServiceImpl(BookRepository bookRepository,
                                MemberRepository memberRepository,
                                BookTransactionRepository transactionRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalBooks(bookRepository.count());
        stats.setTotalBookCopies(bookRepository.countTotalBookCopies());
        stats.setAvailableBookCopies(bookRepository.countTotalAvailableCopies());
        stats.setTotalMembers(memberRepository.count());
        stats.setCurrentlyIssuedBooks(transactionRepository.countByStatus(TransactionStatus.ISSUED));
        stats.setOverdueBooks(transactionRepository.countOverdueTransactions(LocalDate.now()));
        stats.setRecentTransactions(transactionRepository.findTop10ByOrderByIssueDateDescIdDesc());

        return stats;
    }
}
