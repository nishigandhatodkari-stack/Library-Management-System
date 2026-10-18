package com.library.service;

import com.library.dto.ReportFilterDto;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;

import java.io.Writer;
import java.util.List;

public interface ReportService {
    List<Book> getBooksReport(ReportFilterDto filter);
    List<Member> getMembersReport(ReportFilterDto filter);
    List<BookTransaction> getTransactionsReport(ReportFilterDto filter);
    List<BookTransaction> getActiveIssuedReport();
    List<BookTransaction> getOverdueReport();

    void exportBooksToCsv(Writer writer, ReportFilterDto filter);
    void exportMembersToCsv(Writer writer, ReportFilterDto filter);
    void exportTransactionsToCsv(Writer writer, ReportFilterDto filter);
}
