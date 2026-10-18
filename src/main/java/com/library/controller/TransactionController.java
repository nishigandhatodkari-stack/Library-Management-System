package com.library.controller;

import com.library.dto.IssueBookRequest;
import com.library.dto.ReturnBookRequest;
import com.library.entity.Book;
import com.library.entity.BookTransaction;
import com.library.entity.Member;
import com.library.entity.enums.TransactionStatus;
import com.library.exception.BusinessValidationException;
import com.library.service.BookService;
import com.library.service.BookTransactionService;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final BookTransactionService transactionService;
    private final BookService bookService;
    private final MemberService memberService;

    public TransactionController(BookTransactionService transactionService,
                                 BookService bookService,
                                 MemberService memberService) {
        this.transactionService = transactionService;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "status", required = false) TransactionStatus status,
                                   @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   Model model) {
        List<BookTransaction> transactions = transactionService.searchTransactions(keyword, status, startDate, endDate);

        model.addAttribute("transactions", transactions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("statuses", TransactionStatus.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "history");

        return "transactions/history";
    }

    @GetMapping("/issue")
    public String showIssueBookForm(@RequestParam(value = "bookId", required = false) Long preselectedBookId,
                                    @RequestParam(value = "memberId", required = false) Long preselectedMemberId,
                                    Model model) {
        if (!model.containsAttribute("issueRequest")) {
            IssueBookRequest request = new IssueBookRequest();
            if (preselectedBookId != null) {
                request.setBookId(preselectedBookId);
            }
            if (preselectedMemberId != null) {
                request.setMemberId(preselectedMemberId);
            }
            model.addAttribute("issueRequest", request);
        }

        populateIssueFormData(model);
        model.addAttribute("activePage", "issue");
        return "transactions/issue";
    }

    @PostMapping("/issue")
    public String processIssueBook(@Valid @ModelAttribute("issueRequest") IssueBookRequest request,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateIssueFormData(model);
            model.addAttribute("activePage", "issue");
            return "transactions/issue";
        }

        try {
            BookTransaction txn = transactionService.issueBook(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + txn.getBook().getTitle() + "' was successfully issued to " +
                    txn.getMember().getFullName() + " (Due Date: " + txn.getDueDate() + "). Txn Code: " + txn.getTransactionCode());
            return "redirect:/transactions/returns";
        } catch (BusinessValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateIssueFormData(model);
            model.addAttribute("activePage", "issue");
            return "transactions/issue";
        }
    }

    @GetMapping("/returns")
    public String listActiveReturns(Model model) {
        List<BookTransaction> activeTransactions = transactionService.getActiveIssuedTransactions();
        model.addAttribute("activeTransactions", activeTransactions);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "returns");
        return "transactions/return-list";
    }

    @PostMapping("/return")
    public String processBookReturn(@ModelAttribute ReturnBookRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            BookTransaction txn = transactionService.returnBook(request);
            String fineMsg = (txn.getFineAmount() != null && txn.getFineAmount() > 0)
                    ? " Overdue fine: $" + String.format("%.2f", txn.getFineAmount()) + "."
                    : " Returned on time.";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + txn.getBook().getTitle() + "' successfully returned by " +
                    txn.getMember().getFullName() + "!" + fineMsg);
        } catch (BusinessValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing book return: " + e.getMessage());
        }
        return "redirect:/transactions/returns";
    }

    @GetMapping("/overdue")
    public String listOverdueBooks(Model model) {
        List<BookTransaction> overdueTransactions = transactionService.getOverdueTransactions();
        model.addAttribute("overdueTransactions", overdueTransactions);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "overdue");
        return "transactions/overdue-list";
    }

    private void populateIssueFormData(Model model) {
        List<Book> availableBooks = bookService.getAllBooks().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());

        List<Member> activeMembers = memberService.getAllMembers().stream()
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("books", availableBooks);
        model.addAttribute("members", activeMembers);
    }
}
