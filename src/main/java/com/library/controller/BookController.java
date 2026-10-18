package com.library.controller;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.exception.BusinessValidationException;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String listBooks(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "category", required = false) String category,
                            @RequestParam(value = "availability", required = false) String availability,
                            Model model) {
        List<Book> books = bookService.searchAndFilterBooks(keyword, category, availability);
        List<String> categories = bookService.getAllCategories();

        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedAvailability", availability);
        model.addAttribute("activePage", "books");

        return "books/list";
    }

    @GetMapping("/new")
    public String showAddBookForm(Model model) {
        if (!model.containsAttribute("bookDto")) {
            BookDto dto = new BookDto();
            dto.setTotalCopies(1);
            dto.setAvailableCopies(1);
            model.addAttribute("bookDto", dto);
        }
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("activePage", "books");
        return "books/form";
    }

    @PostMapping("/save")
    public String saveBook(@Valid @ModelAttribute("bookDto") BookDto bookDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("categories", bookService.getAllCategories());
            model.addAttribute("activePage", "books");
            return "books/form";
        }

        try {
            bookService.saveBook(bookDto);
            redirectAttributes.addFlashAttribute("successMessage", "Book '" + bookDto.getTitle() + "' was successfully added!");
            return "redirect:/books";
        } catch (BusinessValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("categories", bookService.getAllCategories());
            model.addAttribute("activePage", "books");
            return "books/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (!model.containsAttribute("bookDto")) {
            model.addAttribute("bookDto", bookService.convertToDto(book));
        }
        model.addAttribute("bookId", id);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("activePage", "books");
        return "books/form";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("bookDto") BookDto bookDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", bookService.getAllCategories());
            model.addAttribute("activePage", "books");
            return "books/form";
        }

        try {
            bookService.updateBook(id, bookDto);
            redirectAttributes.addFlashAttribute("successMessage", "Book '" + bookDto.getTitle() + "' was successfully updated!");
            return "redirect:/books";
        } catch (BusinessValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bookId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", bookService.getAllCategories());
            model.addAttribute("activePage", "books");
            return "books/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id);
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book '" + book.getTitle() + "' was successfully deleted.");
        } catch (BusinessValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the book.");
        }
        return "redirect:/books";
    }
}
