package com.library.config;

import com.library.dto.BookDto;
import com.library.dto.MemberDto;
import com.library.dto.IssueBookRequest;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.entity.User;
import com.library.entity.enums.Role;
import com.library.repository.UserRepository;
import com.library.service.BookService;
import com.library.service.BookTransactionService;
import com.library.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final BookTransactionService transactionService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BookService bookService,
                           MemberService memberService,
                           BookTransactionService transactionService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.memberService = memberService;
        this.transactionService = transactionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initBooksAndMembers();
    }

    private void initUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "System Administrator",
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            logger.info("Default admin created: admin / admin123");
        }

        if (!userRepository.existsByUsername("librarian")) {
            User librarian = new User(
                    "librarian",
                    passwordEncoder.encode("librarian123"),
                    "Main Campus Librarian",
                    Role.ROLE_LIBRARIAN
            );
            userRepository.save(librarian);
            logger.info("Default librarian created: librarian / librarian123");
        }
    }

    private void initBooksAndMembers() {
        if (bookService.getAllBooks().isEmpty()) {
            logger.info("Seeding initial book catalog...");

            createBook("978-0134685991", "Effective Java", "Joshua Bloch", "Computer Science", "Addison-Wesley", 2018, 5);
            createBook("978-0132350884", "Clean Code", "Robert C. Martin", "Computer Science", "Prentice Hall", 2008, 4);
            createBook("978-0201633610", "Design Patterns", "Erich Gamma et al.", "Computer Science", "Addison-Wesley", 1994, 3);
            createBook("978-0262033848", "Introduction to Algorithms", "Thomas H. Cormen", "Algorithms", "MIT Press", 2009, 4);
            createBook("978-0073529325", "Operating System Concepts", "Abraham Silberschatz", "Operating Systems", "Wiley", 2018, 3);
            createBook("978-0321125217", "Domain-Driven Design", "Eric Evans", "Software Architecture", "Addison-Wesley", 2003, 2);
        }

        if (memberService.getAllMembers().isEmpty()) {
            logger.info("Seeding initial library members...");

            createMember("MEM-1001", "Aarav Sharma", "aarav.sharma@example.com", "+91 9876543210", "A-12 Tech Enclave, New Delhi");
            createMember("MEM-1002", "Priya Patel", "priya.patel@example.com", "+91 9812345678", "Flat 4B, Green Avenue, Bengaluru");
            createMember("MEM-1003", "Rohan Mehta", "rohan.mehta@example.com", "+91 9923456789", "Sector 14, Gurugram, Haryana");
            createMember("MEM-1004", "Ananya Iyer", "ananya.iyer@example.com", "+91 9745612345", "T-6 Anna Nagar, Chennai");
        }

        if (transactionService.getAllTransactions().isEmpty()) {
            logger.info("Seeding initial issue transactions...");
            try {
                var books = bookService.getAllBooks();
                var members = memberService.getAllMembers();

                if (books.size() >= 2 && members.size() >= 2) {
                    // Transaction 1: Active normal loan
                    Book book1 = books.get(0);
                    Member member1 = members.get(0);
                    IssueBookRequest req1 = new IssueBookRequest(
                            book1.getId(),
                            member1.getId(),
                            LocalDate.now().minusDays(5),
                            LocalDate.now().plusDays(9),
                            "Standard 14-day study loan"
                    );
                    transactionService.issueBook(req1);

                    // Transaction 2: Overdue loan (issued 20 days ago, due 6 days ago)
                    Book book2 = books.get(1);
                    Member member2 = members.get(1);
                    IssueBookRequest req2 = new IssueBookRequest(
                            book2.getId(),
                            member2.getId(),
                            LocalDate.now().minusDays(20),
                            LocalDate.now().minusDays(6),
                            "Exam semester loan (overdue for demonstration)"
                    );
                    transactionService.issueBook(req2);
                }
            } catch (Exception e) {
                logger.warn("Sample transactions could not be seeded: {}", e.getMessage());
            }
        }
    }

    private void createBook(String isbn, String title, String author, String category,
                            String publisher, Integer year, int totalCopies) {
        BookDto dto = new BookDto();
        dto.setIsbn(isbn);
        dto.setTitle(title);
        dto.setAuthor(author);
        dto.setCategory(category);
        dto.setPublisher(publisher);
        dto.setPublicationYear(year);
        dto.setTotalCopies(totalCopies);
        dto.setAvailableCopies(totalCopies);
        bookService.saveBook(dto);
    }

    private void createMember(String memberId, String fullName, String email, String phone, String address) {
        MemberDto dto = new MemberDto();
        dto.setMemberId(memberId);
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setAddress(address);
        dto.setRegistrationDate(LocalDate.now().minusMonths(2));
        dto.setStatus("ACTIVE");
        memberService.saveMember(dto);
    }
}
