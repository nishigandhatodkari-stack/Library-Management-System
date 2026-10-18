package com.library.entity.enums;

public enum Role {
    ROLE_ADMIN("Administrator"),
    ROLE_LIBRARIAN("Librarian");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
