/**
 * Library Management System - Main Interactive JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {
    // Mobile Sidebar Toggle
    const sidebarToggleBtn = document.getElementById('sidebarToggleBtn');
    const appSidebar = document.getElementById('appSidebar');

    if (sidebarToggleBtn && appSidebar) {
        sidebarToggleBtn.addEventListener('click', function () {
            appSidebar.classList.toggle('show');
        });
    }

    // Auto-dismiss alert notifications after 5 seconds
    const autoDismissAlerts = document.querySelectorAll('.alert-dismissible');
    autoDismissAlerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });

    // Populate Return Book Modal
    const returnModal = document.getElementById('returnBookModal');
    if (returnModal) {
        returnModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            if (!button) return;

            const txnId = button.getAttribute('data-txn-id');
            const txnCode = button.getAttribute('data-txn-code');
            const bookTitle = button.getAttribute('data-book-title');
            const memberName = button.getAttribute('data-member-name');
            const dueDate = button.getAttribute('data-due-date');
            const overdueDays = parseInt(button.getAttribute('data-overdue-days') || '0', 10);
            const fineRate = parseFloat(button.getAttribute('data-fine-rate') || '2.0');

            document.getElementById('modalTransactionId').value = txnId;
            document.getElementById('modalTxnCodeDisplay').textContent = txnCode;
            document.getElementById('modalBookTitleDisplay').textContent = bookTitle;
            document.getElementById('modalMemberNameDisplay').textContent = memberName;
            document.getElementById('modalDueDateDisplay').textContent = dueDate;

            const fineAmountInput = document.getElementById('modalFineAmount');
            const overdueWarningBox = document.getElementById('modalOverdueWarning');
            const overdueDaysDisplay = document.getElementById('modalOverdueDaysDisplay');

            if (overdueDays > 0) {
                const calculatedFine = (overdueDays * fineRate).toFixed(2);
                if (overdueWarningBox) overdueWarningBox.classList.remove('d-none');
                if (overdueDaysDisplay) overdueDaysDisplay.textContent = overdueDays;
                if (fineAmountInput) fineAmountInput.value = calculatedFine;
            } else {
                if (overdueWarningBox) overdueWarningBox.classList.add('d-none');
                if (fineAmountInput) fineAmountInput.value = '0.00';
            }
        });
    }

    // Dynamic Due Date updater on Issue Book page
    const issueDateInput = document.getElementById('issueDate');
    const dueDateInput = document.getElementById('dueDate');
    if (issueDateInput && dueDateInput) {
        issueDateInput.addEventListener('change', function () {
            if (this.value) {
                const issueDate = new Date(this.value);
                issueDate.setDate(issueDate.getDate() + 14);
                dueDateInput.value = issueDate.toISOString().split('T')[0];
            }
        });
    }
});

/**
 * Standard confirmation helper for destructive actions (e.g. deletion)
 */
function confirmAction(message) {
    return confirm(message || 'Are you sure you want to proceed with this action?');
}
