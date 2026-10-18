package com.library.service.impl;

import com.library.dto.ReportFilterDto;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.repository.BookRepository;
import com.library.repository.BookTransactionRepository;
import com.library.repository.MemberRepository;
import com.library.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookTransactionRepository transactionRepository;

    public ReportServiceImpl(BookRepository bookRepository,
                             MemberRepository memberRepository,
                             BookTransactionRepository transactionRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Book> getBooksReport(ReportFilterDto filter) {
        if (filter == null) {
            return bookRepository.findAll();
        }
        return bookRepository.searchAndFilterBooks(
                filter.getKeyword(),
                filter.getCategory(),
                filter.getAvailability()
        );
    }

    @Override
    public List<Member> getMembersReport(ReportFilterDto filter) {
        if (filter == null || filter.getKeyword() == null || filter.getKeyword().isBlank()) {
            return memberRepository.findAll();
        }
        return memberRepository.searchMembers(filter.getKeyword());
    }

    @Override
    public List<BookTransaction> getTransactionsReport(ReportFilterDto filter) {
        if (filter == null) {
            return transactionRepository.findAll();
        }
        return transactionRepository.searchTransactions(
                filter.getKeyword(),
                filter.getStatus(),
                filter.getStartDate(),
                filter.getEndDate()
        );
    }

    @Override
    public List<BookTransaction> getActiveIssuedReport() {
        return transactionRepository.findByStatusOrderByDueDateAsc(TransactionStatus.ISSUED);
    }

    @Override
    public List<BookTransaction> getOverdueReport() {
        return transactionRepository.findActiveOverdueTransactions(LocalDate.now());
    }

    @Override
    public void exportBooksToCsv(Writer writer, ReportFilterDto filter) {
        try {
            writer.write("ID,ISBN,Title,Author,Category,Publisher,Year,Total Copies,Available Copies,Issued Copies\n");
            List<Book> books = getBooksReport(filter);
            for (Book b : books) {
                int issued = b.getTotalCopies() - b.getAvailableCopies();
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%d,%d,%d\n",
                        b.getId(),
                        escapeCsv(b.getIsbn()),
                        escapeCsv(b.getTitle()),
                        escapeCsv(b.getAuthor()),
                        escapeCsv(b.getCategory()),
                        escapeCsv(b.getPublisher() != null ? b.getPublisher() : "-"),
                        b.getPublicationYear() != null ? b.getPublicationYear().toString() : "-",
                        b.getTotalCopies(),
                        b.getAvailableCopies(),
                        issued
                ));
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Books CSV export", e);
        }
    }

    @Override
    public void exportMembersToCsv(Writer writer, ReportFilterDto filter) {
        try {
            writer.write("ID,Member ID,Full Name,Email,Phone,Address,Registration Date,Status\n");
            List<Member> members = getMembersReport(filter);
            for (Member m : members) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        m.getId(),
                        escapeCsv(m.getMemberId()),
                        escapeCsv(m.getFullName()),
                        escapeCsv(m.getEmail()),
                        escapeCsv(m.getPhone()),
                        escapeCsv(m.getAddress() != null ? m.getAddress() : "-"),
                        m.getRegistrationDate(),
                        m.getStatus()
                ));
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Members CSV export", e);
        }
    }

    @Override
    public void exportTransactionsToCsv(Writer writer, ReportFilterDto filter) {
        try {
            writer.write("ID,Txn Code,Book Title,ISBN,Member Name,Member ID,Issue Date,Due Date,Return Date,Status,Overdue Days,Fine\n");
            List<BookTransaction> transactions = getTransactionsReport(filter);
            LocalDate today = LocalDate.now();
            for (BookTransaction t : transactions) {
                long overdueDays = t.calculateOverdueDays(today);
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.2f\n",
                        t.getId(),
                        escapeCsv(t.getTransactionCode()),
                        escapeCsv(t.getBook().getTitle()),
                        escapeCsv(t.getBook().getIsbn()),
                        escapeCsv(t.getMember().getFullName()),
                        escapeCsv(t.getMember().getMemberId()),
                        t.getIssueDate(),
                        t.getDueDate(),
                        t.getReturnDate() != null ? t.getReturnDate().toString() : "Not Returned",
                        t.getStatus().name(),
                        overdueDays,
                        t.getFineAmount() != null ? t.getFineAmount() : 0.0
                ));
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error generating Transactions CSV export", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String clean = value.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }
}
