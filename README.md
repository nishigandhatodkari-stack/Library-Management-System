# 📚 LibMaster Pro - Comprehensive Library Management System

A production-grade, full-stack **Library Management System** built with **Java Spring Boot**, **Thymeleaf**, **Spring Data JPA / Hibernate**, **Spring Security**, **Bootstrap 5**, and **MySQL**. Designed for academic projects, resumes, and technical interviews following standard layered enterprise MVC architecture.

---

## 🌟 Key Features

### 1. 🔐 Role-Based Authentication & Security
- Secure login portal using **Spring Security** and **BCryptPasswordEncoder**.
- Session-based authentication with automatic CSRF token protection.
- Role-based separation for **Administrator** (`ROLE_ADMIN`) and **Librarian** (`ROLE_LIBRARIAN`).
- Secure session termination and logout redirection.

### 2. 📊 Real-Time Operations Dashboard
- Dynamic statistic cards:
  - **Total Book Titles** & **Total Physical Copies**
  - **Available Copies for Issue** (real-time shelf stock)
  - **Currently Issued Books** (active borrower loans)
  - **Overdue Books** (loans exceeding permitted due date)
  - **Registered Members**
- Recent issue and return transactions table with live status badges.
- Quick circulation desk navigation shortcuts.

### 3. 📖 Book Catalog Management (Full CRUD)
- **Add New Book**: Store ISBN (unique), Title, Author, Category, Publisher, Publication Year, Total Copies, and Available Copies.
- **Validation**: Enforces ISBN uniqueness, prevents negative stock, and prevents setting available copies greater than total copies.
- **Search & Filter**: Search books by title, author, or ISBN; filter by category and stock availability.
- **Safe Deletion**: Deletion is protected; books with active outstanding loans cannot be deleted.

### 4. 👥 Member Management (Full CRUD)
- **Member Registration**: Store Member ID (unique institutional code), Full Name, Email (unique), Phone, Address, and Registration Date.
- **Validation**: Strict validation prevents duplicate Member IDs and emails.
- **Search**: Real-time filtering by name, email, member ID, or phone.
- **Delete Protection**: Members with active unreturned books cannot be deleted.

### 5. 📤 Issue Book (Circulation Desk)
- Select from active registered members and currently available in-stock books.
- Enforces stock check (`availableCopies > 0`).
- Automatically decrements book stock upon issue.
- Prevents duplicate active borrowing of the same title by the same member.
- Configurable default loan duration (14 days) with automatic due date computation.

### 6. 📥 Return Book Processing
- Dedicated return desk showing all active borrower loans.
- Modal-based check-in workflow capturing actual return date, condition remarks, and calculated overdue fines.
- Automatically increments book stock back to the shelf upon check-in.
- Prevents double-returning books.
- Full audit history preserved.

### 7. ⏰ Overdue Book Tracking & Fine Calculation
- Dynamic overdue calculation based on the current system date.
- Shows borrower name, phone, email, book title, due date, days overdue, and accrued overdue fine ($2.00/day configurable).
- One-click fine settlement and return completion.

### 8. 📑 Reports & CSV Export Center
- Filterable reports for:
  - Inventory Valuation & Stock Balances
  - Active Borrower Loans
  - Delinquent Overdue Loans
  - Registered Member Directory
  - Full Circulation Audit Trail
- One-click **CSV Data Export** with standard formatting.

---

## 🏗️ Architecture & OOP Design

The project strictly follows layered enterprise MVC design:

```
src/main/java/com/library/
├── config/              # SecurityConfig, DataInitializer
├── controller/          # MVC Controllers (Auth, Dashboard, Book, Member, Transaction, Report)
├── dto/                 # Form DTOs with Jakarta validation (BookDto, MemberDto, IssueBookRequest, etc.)
├── entity/              # JPA Entities (Book, Member, BookTransaction, User)
│   └── enums/           # Role, TransactionStatus
├── exception/           # BusinessValidationException, ResourceNotFoundException, GlobalExceptionHandler
├── repository/          # Spring Data JPA Repositories
└── service/             # Service interfaces (BookService, MemberService, BookTransactionService, etc.)
    └── impl/            # Transactional business logic implementations
```

### Applied OOP Principles
- **Encapsulation**: Entity and DTO fields are private with explicit accessors and domain methods (e.g. `book.decreaseAvailableCopies()`, `txn.calculateOverdueDays()`).
- **Inheritance**: Common auditing attributes (`createdAt`, `updatedAt`) encapsulated in `BaseAuditableEntity` (`@MappedSuperclass`).
- **Abstraction & Polymorphism**: All business operations are defined as clean interfaces (`BookService`, `MemberService`, `BookTransactionService`, `ReportService`, `UserService`) and implemented in transactional service classes.
- **Database Consistency**: Critical multi-table updates (e.g. issuing/returning a book and modifying stock quantities) run within `@Transactional` boundaries.

---

## 🛠️ Technology Stack

- **Language:** Java 17+ (Java 17, 21, and 24 compatible)
- **Framework:** Spring Boot 3.3.4
- **Security:** Spring Security 6 with BCrypt
- **ORM / Persistence:** Spring Data JPA / Hibernate 6
- **Template Engine:** Thymeleaf with Spring Security 6 extras
- **Frontend UI:** HTML5, CSS3, JavaScript, Bootstrap 5.3.3, Bootstrap Icons 1.11.3
- **Database:** MySQL 8.0+ (with H2 in-memory profile for automated tests)
- **Build Tool:** Maven 3.9+

---

## 🚀 Getting Started

### Prerequisites
1. **Java JDK 17 or higher** installed.
2. **MySQL Server 8.0+** running locally on port `3306`.
3. **IntelliJ IDEA** (Community or Ultimate) or any standard Java IDE.

---

### Step 1: MySQL Database Configuration

Create the database in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Check `src/main/resources/application.properties` for database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=Password26
```

> **Note:** You can also pass credentials using environment variables without modifying source code:
> - `DB_URL`
> - `DB_USERNAME`
> - `DB_PASSWORD`

*(Optional)* If you wish to manually initialize tables and sample data via SQL scripts, run:
- [database/schema.sql](database/schema.sql)
- [database/sample_data.sql](database/sample_data.sql)

*(Otherwise, the application will automatically create all tables and bootstrap default data on first startup via Hibernate and `DataInitializer`.)*

---

### Step 2: Open and Run in IntelliJ IDEA

1. Launch **IntelliJ IDEA**.
2. Select **File -> Open...** and choose this project folder: `c:\Users\nishi\Downloads\Libraray Project`.
3. Allow IntelliJ to import the Maven project and download dependencies.
4. Locate `src/main/java/com/library/LibraryManagementApplication.java`.
5. Right-click and choose **Run 'LibraryManagementApplication'** (or press `Shift + F10`).

---

### Step 3: Run from Command Line / Terminal

Open PowerShell or CMD in the project folder and run:

```powershell
# Using the bundled Maven helper script
.\mvn.cmd spring-boot:run

# Or using standard maven if installed on PATH
mvn spring-boot:run
```

The application will start at:
👉 **http://localhost:8080**

---

## 🔑 Default Login Credentials

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `admin123` | Full administrative & circulation privileges |
| **Campus Librarian** | `librarian` | `librarian123` | Full circulation, book & member management |

*(Note: These credentials are encrypted using BCrypt in the database.)*

---

## 🧪 Running Automated Tests

Run the full automated test suite (including service unit tests and end-to-end integration tests using the isolated H2 test profile):

```powershell
.\mvn.cmd test
```

### Test Coverage Highlights
- `BookServiceTest`: Tests book creation, duplicate ISBN detection, stock validation, and loan deletion safety.
- `MemberServiceTest`: Tests member registration, duplicate ID & email detection, and active loan delete protection.
- `BookTransactionServiceTest`: Tests loan issuance, out-of-stock prevention, duplicate loan prevention, book returns, and overdue fine computation.
- `LibraryIntegrationTest`: Full end-to-end integration test exercising the complete book catalog, member registration, loan issuing, and return lifecycle.

---

## 📁 Project Structure

```
Libraray Project/
├── .gitignore
├── mvn.cmd                          # Local Maven runner script
├── pom.xml                          # Maven build & dependencies definition
├── README.md                        # Documentation & setup guide
├── database/
│   ├── schema.sql                   # MySQL DDL schema
│   └── sample_data.sql              # Sample data SQL
└── src/
    ├── main/
    │   ├── java/com/library/
    │   │   ├── LibraryManagementApplication.java
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── BookController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── MemberController.java
    │   │   │   ├── ReportController.java
    │   │   │   └── TransactionController.java
    │   │   ├── dto/
    │   │   │   ├── BookDto.java
    │   │   │   ├── DashboardStatsDto.java
    │   │   │   ├── IssueBookRequest.java
    │   │   │   ├── MemberDto.java
    │   │   │   ├── ReportFilterDto.java
    │   │   │   └── ReturnBookRequest.java
    │   │   ├── entity/
    │   │   │   ├── BaseAuditableEntity.java
    │   │   │   ├── Book.java
    │   │   │   ├── BookTransaction.java
    │   │   │   ├── Member.java
    │   │   │   ├── User.java
    │   │   │   └── enums/
    │   │   │       ├── Role.java
    │   │   │       └── TransactionStatus.java
    │   │   ├── exception/
    │   │   │   ├── BusinessValidationException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── repository/
    │   │   │   ├── BookRepository.java
    │   │   │   ├── BookTransactionRepository.java
    │   │   │   ├── MemberRepository.java
    │   │   │   └── UserRepository.java
    │   │   └── service/
    │   │       ├── BookService.java
    │   │       ├── BookTransactionService.java
    │   │       ├── CustomUserDetailsService.java
    │   │       ├── DashboardService.java
    │   │       ├── MemberService.java
    │   │       ├── ReportService.java
    │   │       ├── UserService.java
    │   │       └── impl/
    │   │           ├── BookServiceImpl.java
    │   │           ├── BookTransactionServiceImpl.java
    │   │           ├── DashboardServiceImpl.java
    │   │           ├── MemberServiceImpl.java
    │   │           ├── ReportServiceImpl.java
    │   │           └── UserServiceImpl.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       │   ├── css/styles.css
    │       │   └── js/main.js
    │       └── templates/
    │           ├── auth/login.html
    │           ├── books/
    │           │   ├── form.html
    │           │   └── list.html
    │           ├── dashboard/index.html
    │           ├── error/
    │           │   ├── 404.html
    │           │   └── 500.html
    │           ├── fragments/
    │           │   ├── alerts.html
    │           │   ├── navbar.html
    │           │   └── sidebar.html
    │           ├── members/
    │           │   ├── form.html
    │           │   └── list.html
    │           ├── reports/index.html
    │           └── transactions/
    │               ├── history.html
    │               ├── issue.html
    │               ├── overdue-list.html
    │               └── return-list.html
    └── test/
        ├── java/com/library/
        │   ├── LibraryIntegrationTest.java
        │   └── service/
        │       ├── BookServiceTest.java
        │       ├── BookTransactionServiceTest.java
        │       └── MemberServiceTest.java
        └── resources/
            └── application-test.properties
```

---

## 🎓 Academic Viva & Interview Highlights

- **Database Normalization & Integrity**: Tables (`books`, `members`, `book_transactions`, `users`) maintain 3NF with foreign keys, index optimization, and stock quantity integrity constraints.
- **ACID Transaction Management**: Multi-step operations (e.g. decrementing stock when issuing a book, incrementing stock when returning) are annotated with `@Transactional` to guarantee atomic database commits or rollbacks on failure.
- **Security Best Practices**: Password credentials are never stored in plaintext (BCrypt hashing with 10 salt rounds); CSRF protection is active on all POST/PUT/DELETE forms.
- **Clean Architecture**: Separation of concerns between presentation (`controller`), business rules (`service`), data access (`repository`), and view layers (`templates`).
