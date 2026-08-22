// =========================================================
// SCAN2SERVE - CART
// =========================================================

// =========================================================
// BACKEND URL
// =========================================================

const BACKEND_URL = "http://localhost:8080";

// =========================================================
// GET TABLE NUMBER FROM URL
// =========================================================

const params = new URLSearchParams(window.location.search);

const tableNumber = params.get("table");

// =========================================================
// HEADER ELEMENTS
// =========================================================

// New header table element
const headerTableInfo = document.getElementById("headerTableInfo");

// Keep compatibility with older HTML
const tableInfo = document.getElementById("tableInfo");

const menuButton = document.getElementById("menuButton");

const headerOrderButton = document.getElementById("headerOrderButton");

// =========================================================
// CART CONTAINER
// =========================================================

const cartContainer = document.getElementById("cartContainer");

// =========================================================
// CURRENT ORDER STORAGE KEY
// =========================================================

function getCurrentOrderStorageKey() {
  return "scan2serve_current_order_" + tableNumber;
}

// =========================================================
// GET CURRENT ORDER ID
// =========================================================

function getCurrentOrderId() {
  return localStorage.getItem(getCurrentOrderStorageKey());
}

// =========================================================
// INITIALIZE PAGE
// =========================================================

if (!tableNumber) {
  // New header
  if (headerTableInfo) {
    headerTableInfo.textContent = "Table number not found";
  }

  // Old header compatibility
  if (tableInfo) {
    tableInfo.textContent = "Table number not found";
  }

  if (cartContainer) {
    cartContainer.innerHTML = `
      <p>
        Invalid table number.
      </p>
    `;
  }
} else {
  // =====================================================
  // DISPLAY TABLE NUMBER
  // =====================================================

  const tableText = "Table " + tableNumber;

  // New header
  if (headerTableInfo) {
    headerTableInfo.textContent = tableText;
  }

  // Old header compatibility
  if (tableInfo) {
    tableInfo.textContent = tableText;
  }

  // =====================================================
  // VIEW MENU BUTTON
  // =====================================================

  if (menuButton) {
    menuButton.href = "menu.html?table=" + encodeURIComponent(tableNumber);
  }

  // =====================================================
  // VIEW CURRENT ORDER BUTTON
  // =====================================================

  setupHeaderOrderButton();

  // =====================================================
  // LOAD CART
  // =====================================================

  loadCart();
}

// =========================================================
// SETUP HEADER CURRENT ORDER BUTTON
// =========================================================

function setupHeaderOrderButton() {
  if (!headerOrderButton) {
    return;
  }

  const currentOrderId = getCurrentOrderId();

  if (currentOrderId) {
    headerOrderButton.href =
      "bill.html?orderId=" +
      encodeURIComponent(currentOrderId) +
      "&table=" +
      encodeURIComponent(tableNumber);

    headerOrderButton.style.display = "inline-flex";
  } else {
    headerOrderButton.style.display = "none";
  }
}

// =========================================================
// LOAD CART
// =========================================================

async function loadCart() {
  try {
    const response = await fetch(
      BACKEND_URL + "/customer/cart/" + encodeURIComponent(tableNumber),
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load cart");
    }

    displayCart(result.data);
  } catch (error) {
    console.error("Cart loading error:", error);

    if (cartContainer) {
      cartContainer.innerHTML = `

        <p>
          Unable to load cart.
        </p>

        <p>
          ${escapeHtml(error.message)}
        </p>

      `;
    }
  }
}

// =========================================================
// DISPLAY CART
// =========================================================

function displayCart(cartItems) {
  if (!cartContainer) {
    return;
  }

  // =====================================================
  // CLEAR CART BODY
  // =====================================================

  cartContainer.innerHTML = "";

  // =====================================================
  // EMPTY CART
  // =====================================================

  if (!cartItems || cartItems.length === 0) {
    cartContainer.innerHTML = `

      <div class="empty-cart">

        <p>
          Your cart is empty.
        </p>

      </div>

    `;

    return;
  }

  // =====================================================
  // SUBTOTAL
  // =====================================================

  let subtotal = 0;

  // =====================================================
  // DISPLAY CART ITEMS
  // =====================================================

  cartItems.forEach((item) => {
    const price = Number(item.menu.price);

    const quantity = Number(item.quantity);

    // =================================================
    // ITEM TOTAL
    // =================================================

    const itemTotal = price * quantity;

    // =================================================
    // ADD ITEM TOTAL TO SUBTOTAL
    // =================================================

    subtotal += itemTotal;

    // =============================================
    // CREATE CART CARD
    // =============================================

    const card = document.createElement("div");

    card.className = "cart-card";

    card.innerHTML = `

        <h3>
          ${escapeHtml(item.menu.name)}
        </h3>


        <p>
          ${escapeHtml(item.menu.description)}
        </p>


        <p class="price">

          Price:
          ₹${formatMoney(price)}

        </p>


        <!-- =====================================
             QUANTITY CONTROL
        ====================================== -->

        <div
          class="quantity-control"
        >

          <button
            type="button"
            class="quantity-button"
            onclick="
              decreaseQuantity(
                ${item.id},
                ${quantity}
              )
            "
          >
            −
          </button>


          <span
            class="quantity"
          >
            ${quantity}
          </span>


          <button
            type="button"
            class="quantity-button"
            onclick="
              increaseQuantity(
                ${item.id},
                ${quantity}
              )
            "
          >
            +
          </button>

        </div>


        <!-- =====================================
             ITEM TOTAL
        ====================================== -->

        <p class="item-total">

          Item Total:

          <strong>
            ₹${formatMoney(itemTotal)}
          </strong>

        </p>


        <!-- =====================================
             REMOVE BUTTON
        ====================================== -->

        <button
          type="button"
          class="remove-button"
          onclick="
            removeFromCart(
              ${item.id}
            )
          "
        >
          Remove
        </button>

      `;

    cartContainer.appendChild(card);
  });

  // =====================================================
  // GST CALCULATION
  // =====================================================

  const gst = subtotal * 0.05;

  // =====================================================
  // GRAND TOTAL
  // =====================================================

  const grandTotal = subtotal + gst;

  // =====================================================
  // TOTALS CARD
  // =====================================================

  const totalElement = document.createElement("div");

  totalElement.className = "cart-total";

  totalElement.innerHTML = `

    <div class="total-row">

      <span>
        Subtotal
      </span>

      <strong>
        ₹${formatMoney(subtotal)}
      </strong>

    </div>


    <div class="total-row">

      <span>
        GST (5%)
      </span>

      <strong>
        ₹${formatMoney(gst)}
      </strong>

    </div>


    <div class="total-row grand-total">

      <span>
        Grand Total
      </span>

      <strong>
        ₹${formatMoney(grandTotal)}
      </strong>

    </div>

  `;

  cartContainer.appendChild(totalElement);

  // =====================================================
  // PLACE ORDER BUTTON
  // =====================================================

  const orderButton = document.createElement("button");

  orderButton.type = "button";

  orderButton.className = "place-order-button";

  orderButton.textContent = "Place Order";

  orderButton.onclick = placeOrder;

  cartContainer.appendChild(orderButton);
}

// =========================================================
// INCREASE QUANTITY
// =========================================================

async function increaseQuantity(cartId, currentQuantity) {
  const newQuantity = currentQuantity + 1;

  await updateQuantity(cartId, newQuantity);
}

// =========================================================
// DECREASE QUANTITY
// =========================================================

async function decreaseQuantity(cartId, currentQuantity) {
  if (currentQuantity <= 1) {
    alert("Quantity cannot be less than 1");

    return;
  }

  const newQuantity = currentQuantity - 1;

  await updateQuantity(cartId, newQuantity);
}

// =========================================================
// UPDATE QUANTITY
// =========================================================

async function updateQuantity(cartId, quantity) {
  try {
    const response = await fetch(
      BACKEND_URL +
        "/customer/cart/" +
        encodeURIComponent(cartId) +
        "?quantity=" +
        encodeURIComponent(quantity),

      {
        method: "PUT",
      },
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to update quantity");
    }

    loadCart();
  } catch (error) {
    console.error("Quantity update error:", error);

    alert(error.message || "Unable to update quantity");
  }
}

// =========================================================
// REMOVE CART ITEM
// =========================================================

async function removeFromCart(cartId) {
  const confirmation = confirm("Are you sure you want to remove this item?");

  if (!confirmation) {
    return;
  }

  try {
    const response = await fetch(
      BACKEND_URL + "/customer/cart/" + encodeURIComponent(cartId),

      {
        method: "DELETE",
      },
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to remove item");
    }

    loadCart();
  } catch (error) {
    console.error("Remove cart error:", error);

    alert(error.message || "Unable to remove item");
  }
}

// =========================================================
// PLACE ORDER
// =========================================================

async function placeOrder() {
  const confirmation = confirm("Are you sure you want to place this order?");

  if (!confirmation) {
    return;
  }

  try {
    const response = await fetch(BACKEND_URL + "/customer/order", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        tableNumber: Number(tableNumber),
      }),
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to place order");
    }

    console.log("Order Response:", result);

    // =================================================
    // GET ORDER ID
    // =================================================

    const orderId = result.data?.id;

    if (!orderId) {
      throw new Error("Order was created but Order ID was not returned.");
    }

    // =================================================
    // SAVE CURRENT ORDER
    // =================================================

    localStorage.setItem(getCurrentOrderStorageKey(), orderId);

    // =================================================
    // OPEN BILL
    // =================================================

    window.location.href =
      "bill.html?orderId=" +
      encodeURIComponent(orderId) +
      "&table=" +
      encodeURIComponent(tableNumber) +
      "&orderPlaced=true";
  } catch (error) {
    console.error("Place order error:", error);

    alert(error.message || "Unable to place order");
  }
}

// =========================================================
// PAGE SHOW
// =========================================================

window.addEventListener("pageshow", function () {
  if (tableNumber) {
    setupHeaderOrderButton();

    loadCart();
  }
});

// =========================================================
// FORMAT MONEY
// =========================================================

function formatMoney(value) {
  return Number(value || 0).toFixed(2);
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
