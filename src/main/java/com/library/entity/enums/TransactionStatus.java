package com.library.entity.enums;

public enum TransactionStatus {
    ISSUED("Issued", "badge bg-primary"),
    RETURNED("Returned", "badge bg-success"),
    OVERDUE("Overdue", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    TransactionStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
