// ============================
// BACKEND URL
// ============================

const BACKEND_URL = "http://localhost:8080";

// ============================
// GET TABLE NUMBER
// ============================

const params = new URLSearchParams(window.location.search);

const tableNumber = params.get("table");

// ============================
// CURRENT ORDER STORAGE KEY
// ============================

function getCurrentOrderStorageKey() {
  return "scan2serve_current_order_" + tableNumber;
}

// ============================
// GET CURRENT ORDER ID
// ============================

function getCurrentOrderId() {
  return localStorage.getItem(getCurrentOrderStorageKey());
}

// ============================
// DISPLAY TABLE NUMBER
// ============================

if (!tableNumber) {
  document.getElementById("tableInfo").textContent = "Table number not found";

  document.getElementById("cartContainer").innerHTML = `

            <p>
                Invalid table number.
            </p>

        `;
} else {
  document.getElementById("tableInfo").textContent = "Table " + tableNumber;

  loadCart();
}

// ============================
// LOAD CART
// ============================

async function loadCart() {
  try {
    const response = await fetch(BACKEND_URL + "/customer/cart/" + tableNumber);

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to load cart");
    }

    displayCart(result.data);
  } catch (error) {
    console.error("Cart loading error:", error);

    document.getElementById("cartContainer").innerHTML = `

                <p>
                    Unable to load cart.
                </p>

                <p>
                    ${error.message}
                </p>

                <br>

                <a
                    href="menu.html?table=${tableNumber}"
                >
                    Go to Menu
                </a>

            `;
  }
}

// ============================
// DISPLAY CART
// ============================

function displayCart(cartItems) {
  const container = document.getElementById("cartContainer");

  container.innerHTML = "";

  // ============================
  // VIEW CURRENT ORDER BUTTON
  // ============================

  const currentOrderId = getCurrentOrderId();

  if (currentOrderId) {
    const orderLink = document.createElement("a");

    orderLink.className = "continue-button";

    orderLink.href =
      "bill.html?orderId=" +
      encodeURIComponent(currentOrderId) +
      "&table=" +
      encodeURIComponent(tableNumber);

    orderLink.textContent = "📋 View Current Order";

    container.appendChild(orderLink);
  }

  // ============================
  // EMPTY CART
  // ============================

  if (!cartItems || cartItems.length === 0) {
    const emptyMessage = document.createElement("p");

    emptyMessage.textContent = "Your cart is empty.";

    container.appendChild(emptyMessage);

    // ============================
    // CONTINUE ORDERING
    // ============================

    const menuLink = document.createElement("a");

    menuLink.className = "continue-button";

    menuLink.href = "menu.html?table=" + tableNumber;

    menuLink.textContent = "Continue Ordering";

    container.appendChild(menuLink);

    return;
  }

  let grandTotal = 0;

  // ============================
  // DISPLAY CART ITEMS
  // ============================

  cartItems.forEach((item) => {
    const price = Number(item.menu.price);

    const quantity = Number(item.quantity);

    const itemTotal = price * quantity;

    grandTotal += itemTotal;

    const card = document.createElement("div");

    card.className = "cart-card";

    card.innerHTML = `

                <h3>
                    ${item.menu.name}
                </h3>


                <p>
                    ${item.menu.description}
                </p>


                <p>
                    Price: ₹${price}
                </p>


                <div
                    class="quantity-control"
                >

                    <button
                        class="quantity-button"
                        onclick="decreaseQuantity(
                            ${item.id},
                            ${quantity}
                        )"
                    >
                        −
                    </button>


                    <span
                        class="quantity"
                    >
                        ${quantity}
                    </span>


                    <button
                        class="quantity-button"
                        onclick="increaseQuantity(
                            ${item.id},
                            ${quantity}
                        )"
                    >
                        +
                    </button>

                </div>


                <p class="price">

                    Item Total:
                    ₹${itemTotal}

                </p>


                <button
                    class="remove-button"
                    onclick="removeFromCart(
                        ${item.id}
                    )"
                >

                    Remove

                </button>

            `;

    container.appendChild(card);
  });

  // ============================
  // GRAND TOTAL
  // ============================

  const totalElement = document.createElement("h2");

  totalElement.className = "cart-total";

  totalElement.textContent = "Grand Total: ₹" + grandTotal;

  container.appendChild(totalElement);

  // ============================
  // CONTINUE ORDERING
  // ============================

  const menuLink = document.createElement("a");

  menuLink.className = "continue-button";

  menuLink.href = "menu.html?table=" + tableNumber;

  menuLink.textContent = "Continue Ordering";

  container.appendChild(menuLink);

  // ============================
  // PLACE ORDER BUTTON
  // ============================

  const orderButton = document.createElement("button");

  orderButton.className = "place-order-button";

  orderButton.textContent = "Place Order";

  orderButton.onclick = placeOrder;

  container.appendChild(orderButton);
}

// ============================
// INCREASE QUANTITY
// ============================

async function increaseQuantity(cartId, currentQuantity) {
  const newQuantity = currentQuantity + 1;

  await updateQuantity(cartId, newQuantity);
}

// ============================
// DECREASE QUANTITY
// ============================

async function decreaseQuantity(cartId, currentQuantity) {
  if (currentQuantity <= 1) {
    alert("Quantity cannot be less than 1");

    return;
  }

  const newQuantity = currentQuantity - 1;

  await updateQuantity(cartId, newQuantity);
}

// ============================
// UPDATE QUANTITY
// ============================

async function updateQuantity(cartId, quantity) {
  try {
    const response = await fetch(
      BACKEND_URL + "/customer/cart/" + cartId + "?quantity=" + quantity,

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

    alert(error.message);
  }
}

// ============================
// REMOVE CART ITEM
// ============================

async function removeFromCart(cartId) {
  try {
    const response = await fetch(
      BACKEND_URL + "/customer/cart/" + cartId,

      {
        method: "DELETE",
      },
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to remove item");
    }

    alert("Item removed from cart successfully");

    loadCart();
  } catch (error) {
    console.error("Remove cart error:", error);

    alert(error.message);
  }
}

// ============================
// PLACE ORDER
// ============================

async function placeOrder() {
  const confirmation = confirm("Are you sure you want to place this order?");

  if (!confirmation) {
    return;
  }

  try {
    const response = await fetch(
      BACKEND_URL + "/customer/order",

      {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          tableNumber: Number(tableNumber),
        }),
      },
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to place order");
    }

    console.log("Order Response:", result);

    // ============================
    // SAVE CURRENT ORDER ID
    // ============================

    localStorage.setItem(
      getCurrentOrderStorageKey(),

      result.data.id,
    );

    // ============================
    // DIRECTLY OPEN BILL
    // ============================

    window.location.href =
      "bill.html?orderId=" +
      result.data.id +
      "&table=" +
      tableNumber +
      "&orderPlaced=true";
  } catch (error) {
    console.error("Place order error:", error);

    alert(error.message);
  }
}

// ============================
// RELOAD CART WHEN PAGE SHOWN
// ============================

window.addEventListener("pageshow", function () {
  if (tableNumber) {
    loadCart();
  }
});
