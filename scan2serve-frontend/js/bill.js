// =========================================================
// API CONFIGURATION
// =========================================================

const API_URL = "http://127.0.0.1:8080";

// =========================================================
// URL PARAMETERS
// =========================================================

const params = new URLSearchParams(window.location.search);

// =========================================================
// GET ORDER ID
// =========================================================

function getOrderId() {
  return params.get("orderId");
}

// =========================================================
// GET TABLE NUMBER
// =========================================================

function getTableNumber() {
  return params.get("table");
}

// =========================================================
// GET STATUS TEXT
// =========================================================

function getStatusText(status) {
  switch (status) {
    case "ORDER_PLACED":
      return "Order Placed";

    case "PREPARING":
      return "Preparing";

    case "READY":
      return "Ready";

    case "SERVED":
      return "Served";

    default:
      return "Order Placed";
  }
}

// =========================================================
// LOAD BILL
// =========================================================

async function loadBill() {
  const orderId = getOrderId();

  console.log("Loading bill for Order ID:", orderId);

  // =====================================================
  // CHECK ORDER ID
  // =====================================================

  if (!orderId) {
    showError("Order ID is missing.");

    return;
  }

  try {
    // =================================================
    // API URL
    // =================================================

    const url = `${API_URL}/customer/bill/${orderId}`;

    console.log("Request URL:", url);

    // =================================================
    // API REQUEST
    // =================================================

    const response = await fetch(url);

    console.log("Response Status:", response.status);

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`);
    }

    // =================================================
    // JSON RESPONSE
    // =================================================

    const result = await response.json();

    console.log("Bill API Response:", result);

    // =================================================
    // CHECK RESPONSE
    // =================================================

    if (!result.success) {
      throw new Error(result.message || "Unable to generate bill.");
    }

    const bill = result.data;

    // =================================================
    // ORDER INFORMATION
    // =================================================

    document.getElementById("orderId").textContent = bill.orderId;

    document.getElementById("tableNumber").textContent = bill.tableNumber;

    // =================================================
    // BILL ITEMS
    // =================================================

    const billItems = document.getElementById("billItems");

    billItems.innerHTML = "";

    bill.items.forEach((item) => {
      const row = document.createElement("tr");

      // =========================================
      // STATUS
      // =========================================

      const status = item.status || "ORDER_PLACED";

      const statusText = getStatusText(status);

      // =========================================
      // ITEM ROW
      // =========================================

      row.innerHTML = `

                    <td>
                        ${item.itemName}
                    </td>

                    <td>
                        ${item.quantity}
                    </td>

                    <td>
                        ₹${Number(item.unitPrice).toFixed(2)}
                    </td>

                    <td>
                        ₹${Number(item.totalPrice).toFixed(2)}
                    </td>

                    <td>

                        <span
                            class="status-badge status-${status}"
                        >
                            ${statusText}
                        </span>

                    </td>

                `;

      billItems.appendChild(row);
    });

    // =================================================
    // TOTALS
    // =================================================

    document.getElementById("subtotal").textContent = Number(
      bill.subtotal,
    ).toFixed(2);

    document.getElementById("gst").textContent = Number(bill.gst).toFixed(2);

    document.getElementById("grandTotal").textContent = Number(
      bill.grandTotal,
    ).toFixed(2);

    // =================================================
    // SHOW BILL
    // =================================================

    document.getElementById("loading").style.display = "none";

    document.getElementById("billContent").style.display = "block";
  } catch (error) {
    console.error("Bill Error:", error);

    // Don't hide an already-loaded bill
    // because of a temporary polling error.

    const billContent = document.getElementById("billContent");

    if (billContent.style.display !== "block") {
      showError("Unable to load bill. Please try again.");
    }
  }
}

// =========================================================
// SHOW ERROR
// =========================================================

function showError(message) {
  document.getElementById("loading").style.display = "none";

  const error = document.getElementById("error");

  error.textContent = message;

  error.style.display = "block";
}

// =========================================================
// BACK TO MENU
// =========================================================

function goToMenu() {
  const tableNumber = getTableNumber();

  if (tableNumber) {
    window.location.href = "menu.html?table=" + encodeURIComponent(tableNumber);
  } else {
    window.location.href = "menu.html";
  }
}

// =========================================================
// AUTOMATIC BILL REFRESH
// =========================================================
//
// Refresh bill every 3 seconds so the customer can see
// kitchen status changes automatically.
//

setInterval(loadBill, 3000);

// =========================================================
// FIRST LOAD
// =========================================================

window.onload = loadBill;
