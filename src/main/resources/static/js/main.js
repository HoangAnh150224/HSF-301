/**
 * Digital Market - Main JavaScript
 */

// Toast notification helper
function showToast(message, type = "success") {
  const bgClass =
    type === "success"
      ? "bg-success"
      : type === "error"
        ? "bg-danger"
        : "bg-warning";
  const icon =
    type === "success"
      ? "check-circle"
      : type === "error"
        ? "x-circle"
        : "exclamation-circle";

  const toastHtml = `
        <div class="toast align-items-center text-white ${bgClass} border-0 fade-in" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-${icon} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

  $("#toastContainer").append(toastHtml);
  const toastEl = $("#toastContainer .toast").last()[0];
  const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
  toast.show();

  // Remove toast element after it's hidden
  toastEl.addEventListener("hidden.bs.toast", function () {
    toastEl.remove();
  });
}

// Format currency (VND)
function formatVND(amount) {
  if (amount === null || amount === undefined) return "0";
  return new Intl.NumberFormat("vi-VN").format(amount);
}

// Format date
function formatDate(dateString) {
  if (!dateString) return "--";
  return new Date(dateString).toLocaleDateString("vi-VN");
}

// Document ready
$(document).ready(function () {
  // Load wallet balance for navbar (if authenticated)
  loadNavBalance();
});

// Load navbar balance
function loadNavBalance() {
  $.get("/api/wallet/balance")
    .done(function (wallet) {
      const balance = typeof wallet.balance === 'object' ? parseFloat(wallet.balance) : parseFloat(wallet.balance);
      $("#navBalance").text(formatVND(balance));
    })
    .fail(function () {
      // User not authenticated or error
      $("#navBalance").text("0");
    });
}
