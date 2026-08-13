// =========================================================
// SCAN2SERVE - KITCHEN DASHBOARD
// =========================================================

// =========================================================
// BACKEND URL
// =========================================================

const API_BASE_URL = "http://localhost:8080";

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

    // =================================================
    // DISPLAY ORDERS
    // =================================================

    displayKitchenOrders(orders);

    // =================================================
    // UPDATE HEADER
    // =================================================

    statusElement.textContent = `${orders.length} active order(s)`;

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
    // PLACE CARD ACCORDING TO ORDER STATUS
    // =================================================

    if (order.orderStatus === "PENDING") {
      pendingOrders.appendChild(orderCard);

      pendingCount++;
    } else if (order.orderStatus === "PREPARING") {
      preparingOrders.appendChild(orderCard);

      preparingCount++;
    } else if (order.orderStatus === "READY") {
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

        <span class="kitchen-order-status">
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

        <span class="kitchen-item-status">
            ${formatStatus(item.status)}
        </span>

    `;

  itemCard.appendChild(information);

  // =====================================================
  // BUTTONS
  // =====================================================

  const buttons = document.createElement("div");

  buttons.className = "kitchen-item-buttons";

  // =====================================================
  // ORDER_PLACED
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
    //
    // We reload from the backend rather than manually
    // changing the UI because the backend also
    // recalculates the parent Order status.
    //

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
//
// Refresh every 5 seconds so new customer orders appear
// without manually refreshing the browser.
//

setInterval(loadKitchenOrders, 5000);

// =========================================================
// INITIAL LOAD
// =========================================================

loadKitchenOrders();
