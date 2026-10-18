package com.library.dto;

import com.library.entity.BookTransaction;
import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDto {

    private long totalBooks;
    private long totalBookCopies;
    private long availableBookCopies;
    private long totalMembers;
    private long currentlyIssuedBooks;
    private long overdueBooks;
    private List<BookTransaction> recentTransactions = new ArrayList<>();

    public DashboardStatsDto() {
    }

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getTotalBookCopies() {
        return totalBookCopies;
    }

    public void setTotalBookCopies(long totalBookCopies) {
        this.totalBookCopies = totalBookCopies;
    }

    public long getAvailableBookCopies() {
        return availableBookCopies;
    }

    public void setAvailableBookCopies(long availableBookCopies) {
        this.availableBookCopies = availableBookCopies;
    }

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getCurrentlyIssuedBooks() {
        return currentlyIssuedBooks;
    }

    public void setCurrentlyIssuedBooks(long currentlyIssuedBooks) {
        this.currentlyIssuedBooks = currentlyIssuedBooks;
    }

    public long getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(long overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public List<BookTransaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<BookTransaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
