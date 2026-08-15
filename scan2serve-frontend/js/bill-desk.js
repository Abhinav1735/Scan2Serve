// =========================================================
// SCAN2SERVE - BILL DESK
// =========================================================

const API_BASE_URL = "http://localhost:8080";

// =========================================================
// DOM
// =========================================================

const activeBillsTab = document.getElementById("activeBillsTab");

const oldBillsTab = document.getElementById("oldBillsTab");

const activeBillsSection = document.getElementById("activeBillsSection");

const oldBillsSection = document.getElementById("oldBillsSection");

const activeBillsContainer = document.getElementById("activeBillsContainer");

const noActiveBills = document.getElementById("noActiveBills");

const activeBillCount = document.getElementById("activeBillCount");

const refreshActiveButton = document.getElementById("refreshActiveButton");

const orderIdInput = document.getElementById("orderIdInput");

const tableNumberInput = document.getElementById("tableNumberInput");

const dateInput = document.getElementById("dateInput");

const searchButton = document.getElementById("searchButton");

const clearButton = document.getElementById("clearButton");

const refreshRecentButton = document.getElementById("refreshRecentButton");

const searchMessage = document.getElementById("searchMessage");

const recentBillsContainer = document.getElementById("recentBillsContainer");

const currentDate = document.getElementById("currentDate");

// =========================================================
// MODAL
// =========================================================

const billModal = document.getElementById("billModal");

const closeModalButton = document.getElementById("closeModalButton");

const modalCloseBottomButton = document.getElementById(
  "modalCloseBottomButton",
);

const modalLoading = document.getElementById("modalLoading");

const modalContent = document.getElementById("modalContent");

const modalError = document.getElementById("modalError");

const modalOrderInfo = document.getElementById("modalOrderInfo");

const modalOrderId = document.getElementById("modalOrderId");

const modalTableNumber = document.getElementById("modalTableNumber");

const modalBillItems = document.getElementById("modalBillItems");

const modalSubtotal = document.getElementById("modalSubtotal");

const modalGst = document.getElementById("modalGst");

const modalGrandTotal = document.getElementById("modalGrandTotal");

const paymentSection = document.getElementById("paymentSection");

const paymentMethod = document.getElementById("paymentMethod");

const payButton = document.getElementById("payButton");

const paidMessage = document.getElementById("paidMessage");

const refreshBillButton = document.getElementById("refreshBillButton");

const printButton = document.getElementById("printButton");

// =========================================================
// STATE
// =========================================================

let currentOrderId = null;

let currentBill = null;

let currentBillIsOldBill = false;

// =========================================================
// TAB STATE
// =========================================================

const BILL_DESK_TAB_KEY = "scan2serve_bill_desk_selected_tab";

// =========================================================
// AUTO REFRESH
// =========================================================

const AUTO_REFRESH_INTERVAL = 60 * 1000;

// =========================================================
// PAGE LOAD
// =========================================================

document.addEventListener("DOMContentLoaded", () => {
  updateDateTime();

  const selectedTab = getSelectedTab();

  // Restore selected tab first.
  restoreSelectedTab();

  // Active bills data is always loaded.
  loadActiveBills();

  // If user was on Old Bills before
  // refreshing the page, load Old Bills.
  if (selectedTab === "old") {
    if (hasActiveSearch()) {
      searchOldBills();
    } else {
      loadRecentBills();
    }
  }
});

// =========================================================
// AUTO REFRESH - EVERY 1 MINUTE
// =========================================================

setInterval(() => {
  console.log("Auto refreshing Bill Desk...");

  updateDateTime();

  // -----------------------------------------------------
  // ACTIVE BILLS
  // -----------------------------------------------------

  loadActiveBills();

  // -----------------------------------------------------
  // OLD BILLS
  // -----------------------------------------------------

  const selectedTab = getSelectedTab();

  if (selectedTab === "old") {
    if (hasActiveSearch()) {
      searchOldBills();
    } else {
      loadRecentBills();
    }
  }

  // -----------------------------------------------------
  // OPEN BILL DETAILS
  // -----------------------------------------------------

  if (currentOrderId !== null) {
    openBill(currentOrderId, currentBillIsOldBill);
  }
}, AUTO_REFRESH_INTERVAL);

// =========================================================
// DATE / TIME
// =========================================================

function updateDateTime() {
  const now = new Date();

  const options = {
    day: "2-digit",

    month: "short",

    year: "numeric",

    hour: "2-digit",

    minute: "2-digit",
  };

  if (currentDate) {
    currentDate.textContent = now.toLocaleString("en-IN", options);
  }
}

// =========================================================
// GET SELECTED TAB
// =========================================================
//
// IMPORTANT:
// localStorage is used so the selected tab
// survives hard refresh.
// =========================================================

function getSelectedTab() {
  const storedTab = localStorage.getItem(BILL_DESK_TAB_KEY);

  if (storedTab === "old") {
    return "old";
  }

  return "active";
}

// =========================================================
// SAVE SELECTED TAB
// =========================================================

function saveSelectedTab(tab) {
  localStorage.setItem(BILL_DESK_TAB_KEY, tab);
}

// =========================================================
// RESTORE SELECTED TAB
// =========================================================

function restoreSelectedTab() {
  const selectedTab = getSelectedTab();

  if (selectedTab === "old") {
    showOldBillsTab(false);
  } else {
    showActiveBillsTab(false);
  }
}

// =========================================================
// SHOW ACTIVE BILLS TAB
// =========================================================

function showActiveBillsTab(saveState = true) {
  activeBillsTab.classList.add("active");

  oldBillsTab.classList.remove("active");

  activeBillsSection.classList.remove("hidden");

  oldBillsSection.classList.add("hidden");

  if (saveState) {
    saveSelectedTab("active");
  }
}

// =========================================================
// SHOW OLD BILLS TAB
// =========================================================

function showOldBillsTab(saveState = true) {
  oldBillsTab.classList.add("active");

  activeBillsTab.classList.remove("active");

  oldBillsSection.classList.remove("hidden");

  activeBillsSection.classList.add("hidden");

  if (saveState) {
    saveSelectedTab("old");
  }
}

// =========================================================
// TAB EVENTS
// =========================================================

activeBillsTab.addEventListener("click", () => {
  showActiveBillsTab(true);

  loadActiveBills();
});

oldBillsTab.addEventListener("click", () => {
  showOldBillsTab(true);

  if (hasActiveSearch()) {
    searchOldBills();
  } else {
    loadRecentBills();
  }
});

// =========================================================
// LOAD ACTIVE BILLS
// =========================================================

async function loadActiveBills() {
  try {
    activeBillsContainer.innerHTML = `
      <div class="loading">
        Loading active bills...
      </div>
    `;

    noActiveBills.classList.add("hidden");

    const response = await fetch(`${API_BASE_URL}/bill-desk/orders`);

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load active bills.");
    }

    const orders = Array.isArray(result.data) ? result.data : [];

    activeBillCount.textContent = `${orders.length} Active Bill${
      orders.length === 1 ? "" : "s"
    }`;

    if (orders.length === 0) {
      activeBillsContainer.innerHTML = "";

      noActiveBills.classList.remove("hidden");

      return;
    }

    noActiveBills.classList.add("hidden");

    displayActiveBills(orders);
  } catch (error) {
    console.error("Active bill error:", error);

    activeBillsContainer.innerHTML = `
      <div class="loading">
        Unable to load active bills.<br>
        ${escapeHtml(error.message)}
      </div>
    `;
  }
}

// =========================================================
// DISPLAY ACTIVE BILLS
// =========================================================

function displayActiveBills(orders) {
  activeBillsContainer.innerHTML = "";

  orders.forEach((order) => {
    const card = document.createElement("div");

    card.className = "bill-card";

    card.innerHTML = `

        <div class="bill-card-top">

          <div class="bill-icon">
            🛎
          </div>

          <div>

            <h3>
              Order #${order.id}
            </h3>

            <div class="table-number">
              Table ${order.tableNumber ?? "-"}
            </div>

          </div>

        </div>

        <div class="bill-amount">

          ₹${formatMoney(getOrderAmount(order))}

        </div>

        <button
          class="view-bill-button"
          onclick="openBill(${order.id}, false)"
        >

          ◉ View Bill

        </button>

      `;

    activeBillsContainer.appendChild(card);
  });
}

// =========================================================
// ACTIVE BILLS MANUAL REFRESH
// =========================================================
//
// This is separate from the 1-minute
// automatic refresh.
// =========================================================

if (refreshActiveButton) {
  refreshActiveButton.addEventListener("click", async () => {
    const originalText = refreshActiveButton.innerHTML;

    refreshActiveButton.disabled = true;

    refreshActiveButton.innerHTML = "↻ Refreshing...";

    try {
      await loadActiveBills();
    } catch (error) {
      console.error("Manual active bill refresh error:", error);
    } finally {
      refreshActiveButton.disabled = false;

      refreshActiveButton.innerHTML = originalText;
    }
  });
}

// =========================================================
// LOAD OLD BILLS
// =========================================================

async function loadRecentBills() {
  try {
    recentBillsContainer.innerHTML = `
      <div class="loading">
        Loading old bills...
      </div>
    `;

    const response = await fetch(`${API_BASE_URL}/bill-desk/old-bills`);

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load old bills.");
    }

    let bills = Array.isArray(result.data) ? result.data : [];

    bills.sort((a, b) => {
      const dateA = new Date(a.orderTime || 0);

      const dateB = new Date(b.orderTime || 0);

      return dateB - dateA;
    });

    bills = bills.slice(0, 10);

    displayRecentBills(bills);
  } catch (error) {
    console.error("Old bills error:", error);

    recentBillsContainer.innerHTML = `
      <div class="loading">
        Unable to load old bills.<br>
        ${escapeHtml(error.message)}
      </div>
    `;
  }
}

// =========================================================
// DISPLAY OLD BILLS
// =========================================================

function displayRecentBills(bills) {
  if (bills.length === 0) {
    recentBillsContainer.innerHTML = `
      <div class="loading">
        No paid bills found.
      </div>
    `;

    return;
  }

  const table = document.createElement("table");

  table.className = "recent-table";

  table.innerHTML = `

    <thead>

      <tr>

        <th>
          Order ID
        </th>

        <th>
          Table No.
        </th>

        <th>
          Date & Time
        </th>

        <th>
          Total Amount
        </th>

        <th>
          Payment Status
        </th>

        <th>
          Actions
        </th>

      </tr>

    </thead>

    <tbody></tbody>

  `;

  const tbody = table.querySelector("tbody");

  bills.forEach((order) => {
    const row = document.createElement("tr");

    row.innerHTML = `

        <td>
          Order #${order.id}
        </td>

        <td>
          Table ${order.tableNumber ?? "-"}
        </td>

        <td>
          ${formatDateTime(order.orderTime)}
        </td>

        <td>
          ₹${formatMoney(getOrderAmount(order))}
        </td>

        <td>

          <span class="paid-badge">
            PAID
          </span>

        </td>

        <td>

          <button
            class="table-action view-action"
            title="View Bill"
            onclick="openBill(${order.id}, true)"
          >
            ◉
          </button>

          <button
            class="table-action print-action"
            title="Print Bill"
            onclick="printBill(${order.id})"
          >
            🖨
          </button>

        </td>

      `;

    tbody.appendChild(row);
  });

  recentBillsContainer.innerHTML = "";

  recentBillsContainer.appendChild(table);
}

// =========================================================
// RECENT BILLS MANUAL REFRESH
// =========================================================

if (refreshRecentButton) {
  refreshRecentButton.addEventListener("click", async () => {
    // Make sure refresh never
    // changes the selected section.
    showOldBillsTab(true);

    const originalText = refreshRecentButton.innerHTML;

    refreshRecentButton.disabled = true;

    refreshRecentButton.innerHTML = "↻ Refreshing...";

    try {
      if (hasActiveSearch()) {
        await searchOldBills();
      } else {
        await loadRecentBills();
      }
    } catch (error) {
      console.error("Manual recent bill refresh error:", error);
    } finally {
      refreshRecentButton.disabled = false;

      refreshRecentButton.innerHTML = originalText;
    }
  });
}

// =========================================================
// SEARCH BUTTON
// =========================================================

searchButton.addEventListener("click", searchOldBills);

// =========================================================
// ENTER KEY SEARCH
// =========================================================

orderIdInput.addEventListener("keydown", handleSearchEnter);

tableNumberInput.addEventListener("keydown", handleSearchEnter);

dateInput.addEventListener("keydown", handleSearchEnter);

function handleSearchEnter(event) {
  if (event.key === "Enter") {
    event.preventDefault();

    searchOldBills();
  }
}

// =========================================================
// CHECK ACTIVE SEARCH
// =========================================================

function hasActiveSearch() {
  const orderId = orderIdInput.value.trim();

  const tableNumber = tableNumberInput.value.trim();

  const date = dateInput.value;

  return Boolean(orderId || tableNumber || date);
}

// =========================================================
// SEARCH OLD BILLS
// =========================================================

async function searchOldBills() {
  const orderId = orderIdInput.value.trim();

  const tableNumber = tableNumberInput.value.trim();

  const date = dateInput.value;

  if (!orderId && !tableNumber && !date) {
    showSearchMessage("Enter at least one search value.", false, true);

    return;
  }

  try {
    searchButton.disabled = true;

    searchButton.textContent = "Searching...";

    recentBillsContainer.innerHTML = `
      <div class="loading">
        Searching old bills...
      </div>
    `;

    const params = new URLSearchParams();

    if (orderId) {
      params.append("orderId", orderId);
    }

    if (tableNumber) {
      params.append("tableNumber", tableNumber);
    }

    if (date) {
      params.append("date", date);
    }

    const response = await fetch(
      `${API_BASE_URL}/bill-desk/old-bills?${params.toString()}`,
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to search old bills.");
    }

    const bills = Array.isArray(result.data) ? result.data : [];

    if (bills.length === 0) {
      showSearchMessage(result.message || "No paid bills found.", false, false);

      recentBillsContainer.innerHTML = `
        <div class="loading">
          No bills found for the selected search.
        </div>
      `;

      return;
    }

    showSearchMessage(
      `${bills.length} paid bill${bills.length === 1 ? "" : "s"} found.`,
      true,
      false,
    );

    displayRecentBills(bills);
  } catch (error) {
    console.error("Search error:", error);

    showSearchMessage(error.message || "Unable to search bills.", false, true);
  } finally {
    searchButton.disabled = false;

    searchButton.textContent = "🔍 Search";
  }
}

// =========================================================
// CLEAR SEARCH
// =========================================================

clearButton.addEventListener("click", () => {
  orderIdInput.value = "";

  tableNumberInput.value = "";

  dateInput.value = "";

  searchMessage.classList.add("hidden");

  // Remain on Old Bills.
  showOldBillsTab(true);

  loadRecentBills();
});

// =========================================================
// SEARCH MESSAGE
// =========================================================

function showSearchMessage(message, success, error) {
  searchMessage.textContent = message;

  searchMessage.className = "search-message";

  if (success) {
    searchMessage.classList.add("success");
  }

  if (error) {
    searchMessage.classList.add("error");
  }

  searchMessage.classList.remove("hidden");
}

// =========================================================
// OPEN BILL
// =========================================================

async function openBill(orderId, isOldBill = false) {
  currentOrderId = orderId;

  currentBillIsOldBill = isOldBill;

  billModal.classList.remove("hidden");

  modalLoading.classList.remove("hidden");

  modalContent.classList.add("hidden");

  modalError.classList.add("hidden");

  paidMessage.classList.add("hidden");

  modalOrderInfo.textContent = `Order #${orderId}`;

  try {
    const response = await fetch(`${API_BASE_URL}/bill-desk/orders/${orderId}`);

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load bill.");
    }

    currentBill = result.data;

    displayBill(currentBill, isOldBill);
  } catch (error) {
    console.error("Bill error:", error);

    modalLoading.classList.add("hidden");

    modalError.textContent = error.message || "Unable to load bill.";

    modalError.classList.remove("hidden");
  }
}

// =========================================================
// DISPLAY BILL
// =========================================================
//
// SCREEN:
// ALL ITEMS + STATUS
//
// PRINT:
// READY + SERVED ONLY
// =========================================================

function displayBill(bill, isOldBill) {
  modalLoading.classList.add("hidden");

  modalContent.classList.remove("hidden");

  modalOrderId.textContent = bill.orderId ?? currentOrderId;

  modalTableNumber.textContent = bill.tableNumber ?? "-";

  modalOrderInfo.textContent = `Order #${bill.orderId ?? currentOrderId}`;

  modalBillItems.innerHTML = "";

  const items = Array.isArray(bill.items) ? bill.items : [];

  items.forEach((item) => {
    const row = document.createElement("tr");

    const status = String(item.status || "ORDER_PLACED").toUpperCase();

    const statusClass = status.toLowerCase();

    row.innerHTML = `

        <td>
          ${escapeHtml(item.itemName || "-")}
        </td>

        <td>
          ${item.quantity ?? 0}
        </td>

        <td>
          ₹${formatMoney(item.unitPrice ?? item.price ?? 0)}
        </td>

        <td>
          ₹${formatMoney(
            item.totalPrice ??
              Number(item.price || 0) * Number(item.quantity || 0),
          )}
        </td>

        <td>

          <span
            class="status ${statusClass}"
          >
            ${formatStatus(status)}
          </span>

        </td>

      `;

    modalBillItems.appendChild(row);
  });

  modalSubtotal.textContent = `₹${formatMoney(bill.subtotal)}`;

  modalGst.textContent = `₹${formatMoney(bill.gst)}`;

  modalGrandTotal.textContent = `₹${formatMoney(bill.grandTotal)}`;

  // Old Bill
  if (isOldBill) {
    paymentSection.classList.add("hidden");

    paidMessage.classList.remove("hidden");
  }

  // Active Bill
  else {
    paymentSection.classList.remove("hidden");

    paidMessage.classList.add("hidden");
  }
}

// =========================================================
// BILL DETAILS MANUAL REFRESH
// =========================================================
//
// IMPORTANT:
// currentOrderId and currentBillIsOldBill
// are preserved.
// =========================================================

if (refreshBillButton) {
  refreshBillButton.addEventListener("click", async () => {
    if (currentOrderId === null) {
      return;
    }

    const originalText = refreshBillButton.innerHTML;

    refreshBillButton.disabled = true;

    refreshBillButton.innerHTML = "↻ Refreshing...";

    try {
      await openBill(currentOrderId, currentBillIsOldBill);
    } catch (error) {
      console.error("Manual bill refresh error:", error);
    } finally {
      refreshBillButton.disabled = false;

      refreshBillButton.innerHTML = originalText;
    }
  });
}

// =========================================================
// PAYMENT
// =========================================================

payButton.addEventListener("click", processPayment);

async function processPayment() {
  if (!currentOrderId) {
    return;
  }

  const method = paymentMethod.value;

  const confirmed = confirm(
    `Mark Order #${currentOrderId} as PAID using ${method}?`,
  );

  if (!confirmed) {
    return;
  }

  try {
    payButton.disabled = true;

    payButton.textContent = "Processing...";

    const response = await fetch(
      `${API_BASE_URL}/bill-desk/orders/${currentOrderId}/payment`,
      {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          paymentMethod: method,
        }),
      },
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Payment failed.");
    }

    paymentSection.classList.add("hidden");

    paidMessage.classList.remove("hidden");

    alert("Payment completed successfully.");

    // Refresh active bills.
    loadActiveBills();

    // If Old Bills is selected,
    // refresh it without switching tabs.
    if (getSelectedTab() === "old") {
      if (hasActiveSearch()) {
        searchOldBills();
      } else {
        loadRecentBills();
      }
    }
  } catch (error) {
    console.error("Payment error:", error);

    alert(error.message || "Unable to complete payment.");
  } finally {
    payButton.disabled = false;

    payButton.textContent = "✓ Mark as Paid";
  }
}

// =========================================================
// PRINT BILL
// =========================================================
//
// ONLY READY + SERVED ITEMS
//
// STATUS COLUMN IS NOT PRINTED
// =========================================================

printButton.addEventListener("click", () => {
  if (currentOrderId) {
    printBill(currentOrderId);
  }
});

async function printBill(orderId) {
  try {
    const response = await fetch(`${API_BASE_URL}/bill-desk/orders/${orderId}`);

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load bill.");
    }

    const bill = result.data;

    const allItems = Array.isArray(bill.items) ? bill.items : [];

    // -----------------------------------------------------
    // READY + SERVED ONLY
    // -----------------------------------------------------

    const printableItems = allItems.filter((item) => {
      const status = String(item.status || "").toUpperCase();

      return status === "READY" || status === "SERVED";
    });

    const rows = printableItems
      .map((item) => {
        const total =
          item.totalPrice ??
          Number(item.price || 0) * Number(item.quantity || 0);

        return `

              <tr>

                <td>
                  ${escapeHtml(item.itemName || "-")}
                </td>

                <td>
                  ${item.quantity ?? 0}
                </td>

                <td>
                  ₹${formatMoney(item.unitPrice ?? item.price ?? 0)}
                </td>

                <td>
                  ₹${formatMoney(total)}
                </td>

              </tr>

            `;
      })
      .join("");

    const printWindow = window.open("", "_blank");

    if (!printWindow) {
      throw new Error(
        "Unable to open print window. Please allow pop-ups for this site.",
      );
    }

    printWindow.document.write(
      `

      <!DOCTYPE html>

      <html>

      <head>

        <meta charset="UTF-8">

        <title>
          Scan2Serve Bill #${bill.orderId}
        </title>

        <style>

          body {

            font-family:
              Arial,
              sans-serif;

            padding:
              30px;

            color:
              #111;

          }

          h1 {

            text-align:
              center;

            margin-bottom:
              5px;

          }

          .subtitle {

            text-align:
              center;

            margin-bottom:
              20px;

          }

          .info {

            display:
              flex;

            justify-content:
              space-between;

            margin:
              25px 0;

          }

          table {

            width:
              100%;

            border-collapse:
              collapse;

          }

          th,
          td {

            border:
              1px solid #ccc;

            padding:
              9px;

            text-align:
              left;

          }

          th {

            background:
              #eee;

          }

          .note {

            margin-top:
              20px;

            font-size:
              13px;

          }

          .totals {

            width:
              300px;

            margin-left:
              auto;

            margin-top:
              25px;

          }

          .total-row {

            display:
              flex;

            justify-content:
              space-between;

            padding:
              6px 0;

          }

          .grand {

            border-top:
              2px solid #111;

            font-size:
              18px;

            font-weight:
              bold;

          }

          .thank-you {

            text-align:
              center;

            margin-top:
              35px;

            padding-top:
              15px;

            border-top:
              1px solid #ddd;

            font-size:
              14px;

            color:
              #555;

          }

          .thank-you-main {

            font-size:
              15px;

            font-weight:
              bold;

            margin-bottom:
              5px;

          }

          .thank-you-sub {

            font-size:
              12px;

          }

        </style>

      </head>

      <body>

        <h1>
          SCAN2SERVE
        </h1>

        <div class="subtitle">
          Restaurant Bill
        </div>

        <div class="info">

          <strong>
            Order #${bill.orderId}
          </strong>

          <strong>
            Table ${bill.tableNumber}
          </strong>

        </div>

        <table>

          <thead>

            <tr>

              <th>
                Item
              </th>

              <th>
                Qty
              </th>

              <th>
                Unit Price
              </th>

              <th>
                Total
              </th>

            </tr>

          </thead>

          <tbody>

            ${
              rows ||
              `

                <tr>

                  <td colspan="4">

                    No READY or SERVED
                    items available.

                  </td>

                </tr>

              `
            }

          </tbody>

        </table>

        <div class="note">

          Only READY and SERVED items
          are included in the payable bill.

        </div>

        <div class="totals">

          <div class="total-row">

            <span>
              Subtotal
            </span>

            <strong>
              ₹${formatMoney(bill.subtotal)}
            </strong>

          </div>

          <div class="total-row">

            <span>
              GST (5%)
            </span>

            <strong>
              ₹${formatMoney(bill.gst)}
            </strong>

          </div>

          <div class="total-row grand">

            <span>
              Grand Total
            </span>

            <strong>
              ₹${formatMoney(bill.grandTotal)}
            </strong>

          </div>

        </div>

        <div class="thank-you">

          <div class="thank-you-main">

            Thank you for dining with us!

          </div>

          <div class="thank-you-sub">

            We hope to serve you again.

          </div>

        </div>

        <script>

          window.onload =
            function() {

              window.print();

            };

        <\/script>

      </body>

      </html>

      `,
    );

    printWindow.document.close();
  } catch (error) {
    console.error("Print error:", error);

    alert(error.message || "Unable to print bill.");
  }
}

// =========================================================
// CLOSE MODAL
// =========================================================

closeModalButton.addEventListener("click", closeModal);

modalCloseBottomButton.addEventListener("click", closeModal);

const modalOverlay = document.querySelector(".modal-overlay");

if (modalOverlay) {
  modalOverlay.addEventListener("click", closeModal);
}

function closeModal() {
  billModal.classList.add("hidden");

  currentOrderId = null;

  currentBill = null;

  currentBillIsOldBill = false;
}

// =========================================================
// HELPERS
// =========================================================

function getOrderAmount(order) {
  return Number(order.totalAmount ?? order.grandTotal ?? 0);
}

// =========================================================
// FORMAT MONEY
// =========================================================

function formatMoney(value) {
  return Number(value || 0).toFixed(2);
}

// =========================================================
// FORMAT DATE/TIME
// =========================================================

function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString("en-IN", {
    day: "2-digit",

    month: "short",

    year: "numeric",

    hour: "2-digit",

    minute: "2-digit",
  });
}

// =========================================================
// FORMAT STATUS
// =========================================================

function formatStatus(status) {
  return String(status || "-")
    .replaceAll("_", " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

// =========================================================
// ESCAPE HTML
// =========================================================

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// =========================================================
// GLOBAL FUNCTIONS
// =========================================================

window.openBill = openBill;

window.printBill = printBill;
