// =========================================================
// SCAN2SERVE - KITCHEN DASHBOARD
// =========================================================

// =========================================================
// BACKEND URL
// =========================================================

const API_BASE_URL = "http://localhost:8080";

let activeKitchenOrders = [];

// =========================================================
// LOAD KITCHEN ORDERS
// =========================================================

async function loadKitchenOrders() {
  const errorElement = document.getElementById("kitchenError");

  const statusElement = document.getElementById("kitchenStatus");

  try {
    errorElement.textContent = "";

    statusElement.textContent = "Loading orders...";

    // =================================================
    // API REQUEST
    // =================================================

    const response = await fetch(`${API_BASE_URL}/kitchen/orders`);

    // =================================================
    // CHECK HTTP RESPONSE
    // =================================================

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`);
    }

    // =================================================
    // JSON
    // =================================================

    const result = await response.json();

    // =================================================
    // CHECK API RESPONSE
    // =================================================

    if (!result.success) {
      throw new Error(result.message || "Unable to load kitchen orders");
    }

    // =================================================
    // GET DATA
    // =================================================

    const orders = result.data || [];

    activeKitchenOrders = orders;

    // =================================================
    // PRESERVE CURRENT SEARCH
    // =================================================

    const searchInput = document.getElementById("kitchenSearch");

    const searchValue = searchInput
      ? searchInput.value.trim().toLowerCase()
      : "";

    // =================================================
    // DISPLAY ORDERS
    // =================================================

    if (searchValue) {
      const filteredOrders = activeKitchenOrders.filter((order) => {
        const orderId = String(order.orderId ?? "").toLowerCase();

        const tableNumber = String(order.tableNumber ?? "").toLowerCase();

        return (
          orderId.includes(searchValue) || tableNumber.includes(searchValue)
        );
      });

      displayKitchenOrders(filteredOrders);

      // ===============================================
      // UPDATE SEARCH RESULT COUNT
      // ===============================================

      statusElement.textContent = `${filteredOrders.length} active order(s)`;
    } else {
      // ===============================================
      // NO SEARCH
      // SHOW ALL ORDERS
      // ===============================================

      displayKitchenOrders(activeKitchenOrders);

      statusElement.textContent = `${activeKitchenOrders.length} active order(s)`;
    }

    // =================================================
    // UPDATE LAST UPDATED
    // =================================================

    document.getElementById("lastUpdated").textContent =
      `Last updated: ${new Date().toLocaleTimeString()}`;
  } catch (error) {
    console.error("Kitchen API Error:", error);

    errorElement.textContent =
      "Unable to load kitchen orders. Please check the backend.";

    statusElement.textContent = "Connection error";

    clearKitchenOrders();
  }
}

// =========================================================
// SEARCH KITCHEN ORDERS
// =========================================================

function searchKitchenOrders() {
  const searchInput = document.getElementById("kitchenSearch");

  const searchValue = searchInput.value.trim().toLowerCase();

  // =====================================================
  // SHOW ALL ORDERS WHEN SEARCH IS EMPTY
  // =====================================================

  if (!searchValue) {
    displayKitchenOrders(activeKitchenOrders);

    document.getElementById("kitchenStatus").textContent =
      `${activeKitchenOrders.length} active order(s)`;

    return;
  }

  // =====================================================
  // FILTER ORDERS
  // =====================================================

  const filteredOrders = activeKitchenOrders.filter((order) => {
    const orderId = String(order.orderId ?? "").toLowerCase();

    const tableNumber = String(order.tableNumber ?? "").toLowerCase();

    return orderId.includes(searchValue) || tableNumber.includes(searchValue);
  });

  // =====================================================
  // DISPLAY FILTERED ORDERS
  // =====================================================

  displayKitchenOrders(filteredOrders);

  // =====================================================
  // UPDATE RESULT COUNT
  // =====================================================

  document.getElementById("kitchenStatus").textContent =
    `${filteredOrders.length} active order(s)`;
}

// =========================================================
// DISPLAY KITCHEN ORDERS
// =========================================================

function displayKitchenOrders(orders) {
  const pendingOrders = document.getElementById("pendingOrders");

  const preparingOrders = document.getElementById("preparingOrders");

  const readyOrders = document.getElementById("readyOrders");

  // =====================================================
  // CLEAR OLD DATA
  // =====================================================

  pendingOrders.innerHTML = "";

  preparingOrders.innerHTML = "";

  readyOrders.innerHTML = "";

  // =====================================================
  // COUNTERS
  // =====================================================

  let pendingCount = 0;

  let preparingCount = 0;

  let readyCount = 0;

  // =====================================================
  // NO ORDERS
  // =====================================================

  if (orders.length === 0) {
    pendingOrders.innerHTML = `<p class="kitchen-empty">
        No new orders
      </p>`;

    preparingOrders.innerHTML = `<p class="kitchen-empty">
        No orders being prepared
      </p>`;

    readyOrders.innerHTML = `<p class="kitchen-empty">
        No ready orders
      </p>`;
  }

  // =====================================================
  // PROCESS ORDERS
  // =====================================================

  orders.forEach((order) => {
    const orderCard = createOrderCard(order);

    // =================================================
    // NEW / PENDING
    // =================================================

    if (order.orderStatus === "PENDING") {
      pendingOrders.appendChild(orderCard);

      pendingCount++;
    }

    // =================================================
    // PREPARING
    // =================================================
    else if (order.orderStatus === "PREPARING") {
      preparingOrders.appendChild(orderCard);

      preparingCount++;
    }

    // =================================================
    // READY
    // =================================================
    else if (order.orderStatus === "READY") {
      readyOrders.appendChild(orderCard);

      readyCount++;
    }
  });

  // =====================================================
  // UPDATE COUNTERS
  // =====================================================

  document.getElementById("pendingCount").textContent = pendingCount;

  document.getElementById("preparingCount").textContent = preparingCount;

  document.getElementById("readyCount").textContent = readyCount;
}

// =========================================================
// CREATE ORDER CARD
// =========================================================

function createOrderCard(order) {
  const card = document.createElement("div");

  card.className = "kitchen-order-card";

  // =====================================================
  // ORDER HEADER
  // =====================================================

  const header = document.createElement("div");

  header.className = "kitchen-order-header";

  header.innerHTML = `

    <div>

      <h3>
        Order #${order.orderId}
      </h3>

      <p>
        Table ${order.tableNumber}
      </p>

    </div>

    <span
      class="kitchen-order-status"
    >
      ${formatStatus(order.orderStatus)}
    </span>

  `;

  card.appendChild(header);

  // =====================================================
  // ORDER TIME
  // =====================================================

  if (order.orderTime) {
    const time = document.createElement("p");

    time.className = "kitchen-order-time";

    time.textContent = `Ordered: ${formatDateTime(order.orderTime)}`;

    card.appendChild(time);
  }

  // =====================================================
  // ITEMS
  // =====================================================

  const itemsContainer = document.createElement("div");

  itemsContainer.className = "kitchen-items";

  if (!order.items || order.items.length === 0) {
    itemsContainer.innerHTML = `<p class="kitchen-empty">
        No items found
      </p>`;
  } else {
    order.items.forEach((item) => {
      const itemElement = createOrderItem(item);

      itemsContainer.appendChild(itemElement);
    });
  }

  card.appendChild(itemsContainer);

  return card;
}

// =========================================================
// CREATE INDIVIDUAL ORDER ITEM
// =========================================================

function createOrderItem(item) {
  const itemCard = document.createElement("div");

  itemCard.className = "kitchen-item";

  // =====================================================
  // ITEM INFORMATION
  // =====================================================

  const information = document.createElement("div");

  information.className = "kitchen-item-information";

  information.innerHTML = `

    <h4>
      ${escapeHtml(item.itemName)}
    </h4>

    <p>
      Quantity:

      <strong>
        ${item.quantity}
      </strong>
    </p>

    <small>
      Item #${item.itemId}
    </small>

  `;

  itemCard.appendChild(information);

  // =====================================================
  // ITEM STATUS COLUMN
  // =====================================================

  const statusColumn = document.createElement("div");

  statusColumn.className = "kitchen-item-status-column";

  const statusLabel = document.createElement("span");

  statusLabel.className = "kitchen-item-status-label";

  statusLabel.textContent = "Status";

  const status = document.createElement("span");

  status.className = `kitchen-item-status kitchen-status-${String(
    item.status || "UNKNOWN",
  ).toLowerCase()}`;

  status.textContent = formatStatus(item.status);

  statusColumn.appendChild(statusLabel);

  statusColumn.appendChild(status);

  itemCard.appendChild(statusColumn);

  // =====================================================
  // BUTTONS
  // =====================================================

  const buttons = document.createElement("div");

  buttons.className = "kitchen-item-buttons";

  // =====================================================
  // ORDER PLACED
  // =====================================================

  if (item.status === "ORDER_PLACED") {
    buttons.appendChild(
      createStatusButton("Start Preparing", "PREPARING", item.itemId),
    );
  }

  // =====================================================
  // PREPARING
  // =====================================================
  else if (item.status === "PREPARING") {
    buttons.appendChild(createStatusButton("Mark Ready", "READY", item.itemId));
  }

  // =====================================================
  // READY
  // =====================================================
  else if (item.status === "READY") {
    buttons.appendChild(
      createStatusButton("Mark Served", "SERVED", item.itemId),
    );
  }

  // =====================================================
  // SERVED
  // =====================================================
  else if (item.status === "SERVED") {
    const served = document.createElement("span");

    served.className = "kitchen-served-label";

    served.textContent = "✓ Served";

    buttons.appendChild(served);
  }

  itemCard.appendChild(buttons);

  return itemCard;
}

// =========================================================
// CREATE STATUS BUTTON
// =========================================================

function createStatusButton(text, newStatus, itemId) {
  const button = document.createElement("button");

  button.type = "button";

  button.className = "kitchen-action-button";

  button.textContent = text;

  button.onclick = () => updateItemStatus(itemId, newStatus, button);

  return button;
}

// =========================================================
// UPDATE ITEM STATUS
// =========================================================

async function updateItemStatus(itemId, newStatus, button) {
  try {
    // =================================================
    // DISABLE BUTTON
    // =================================================

    button.disabled = true;

    button.textContent = "Updating...";

    // =================================================
    // API REQUEST
    // =================================================

    const response = await fetch(
      `${API_BASE_URL}/kitchen/order-items/${itemId}/status`,

      {
        method: "PUT",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          status: newStatus,
        }),
      },
    );

    // =================================================
    // CHECK HTTP RESPONSE
    // =================================================

    if (!response.ok) {
      throw new Error(`HTTP Error: ${response.status}`);
    }

    // =================================================
    // READ RESPONSE
    // =================================================

    const result = await response.json();

    // =================================================
    // CHECK API RESPONSE
    // =================================================

    if (!result.success) {
      throw new Error(result.message || "Unable to update item status");
    }

    // =================================================
    // RELOAD ORDERS
    // =================================================

    await loadKitchenOrders();
  } catch (error) {
    console.error("Status Update Error:", error);

    alert("Unable to update item status.");

    button.disabled = false;

    button.textContent = getButtonText(newStatus);
  }
}

// =========================================================
// BUTTON TEXT
// =========================================================

function getButtonText(status) {
  if (status === "PREPARING") {
    return "Start Preparing";
  }

  if (status === "READY") {
    return "Mark Ready";
  }

  if (status === "SERVED") {
    return "Mark Served";
  }

  return "Update";
}

// =========================================================
// FORMAT STATUS
// =========================================================

function formatStatus(status) {
  if (status === "ORDER_PLACED") {
    return "New";
  }

  if (status === "PREPARING") {
    return "Preparing";
  }

  if (status === "READY") {
    return "Ready";
  }

  if (status === "SERVED") {
    return "Served";
  }

  return status || "Unknown";
}

// =========================================================
// FORMAT DATE/TIME
// =========================================================

function formatDateTime(dateTime) {
  try {
    return new Date(dateTime).toLocaleString();
  } catch (error) {
    return dateTime;
  }
}

// =========================================================
// ESCAPE HTML
// =========================================================

function escapeHtml(value) {
  const div = document.createElement("div");

  div.textContent = value || "";

  return div.innerHTML;
}

// =========================================================
// CLEAR ORDERS
// =========================================================

function clearKitchenOrders() {
  document.getElementById("pendingOrders").innerHTML = "";

  document.getElementById("preparingOrders").innerHTML = "";

  document.getElementById("readyOrders").innerHTML = "";
}

// =========================================================
// AUTO REFRESH
// =========================================================

setInterval(loadKitchenOrders, 5000);

// =========================================================
// INITIAL LOAD
// =========================================================

loadKitchenOrders();
