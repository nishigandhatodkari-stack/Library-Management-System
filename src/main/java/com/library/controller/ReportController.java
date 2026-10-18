package com.library.controller;

import com.library.dto.ReportFilterDto;
import com.library.service.BookService;
import com.library.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final BookService bookService;

    public ReportController(ReportService reportService, BookService bookService) {
        this.reportService = reportService;
        this.bookService = bookService;
    }

    @GetMapping
    public String viewReports(@RequestParam(value = "tab", defaultValue = "books") String tab,
                              @ModelAttribute("filter") ReportFilterDto filter,
                              Model model) {
        model.addAttribute("activeTab", tab);
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "reports");

        switch (tab) {
            case "issued":
                model.addAttribute("reportData", reportService.getActiveIssuedReport());
                break;
            case "overdue":
                model.addAttribute("reportData", reportService.getOverdueReport());
                break;
            case "members":
                model.addAttribute("reportData", reportService.getMembersReport(filter));
                break;
            case "history":
                model.addAttribute("reportData", reportService.getTransactionsReport(filter));
                break;
            case "books":
            default:
                model.addAttribute("reportData", reportService.getBooksReport(filter));
                break;
        }

        return "reports/index";
    }

    @GetMapping("/export/books")
    public void exportBooksCsv(@ModelAttribute ReportFilterDto filter, HttpServletResponse response) throws IOException {
        String filename = "books_report_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        reportService.exportBooksToCsv(response.getWriter(), filter);
    }

    @GetMapping("/export/members")
    public void exportMembersCsv(@ModelAttribute ReportFilterDto filter, HttpServletResponse response) throws IOException {
        String filename = "members_report_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        reportService.exportMembersToCsv(response.getWriter(), filter);
    }

    @GetMapping("/export/transactions")
    public void exportTransactionsCsv(@ModelAttribute ReportFilterDto filter, HttpServletResponse response) throws IOException {
        String filename = "transactions_report_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        reportService.exportTransactionsToCsv(response.getWriter(), filter);
    }
}
