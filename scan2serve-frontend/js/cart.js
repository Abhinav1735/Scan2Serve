const BACKEND_URL = "http://localhost:8080";

// ============================
// Get Table Number From URL
// ============================

const params = new URLSearchParams(window.location.search);

const tableNumber = params.get("table");

// ============================
// Validate Table Number
// ============================

if (!tableNumber) {
  document.getElementById("tableInfo").textContent = "Table number not found";

  document.getElementById("cartContainer").innerHTML = `
        <p>Invalid table number.</p>
    `;
} else {
  document.getElementById("tableInfo").textContent = "Table " + tableNumber;

  loadCart();
}

// ============================
// Load Cart
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

            <a href="menu.html?table=${tableNumber}">
                Go to Menu
            </a>

        `;
  }
}

// ============================
// Display Cart
// ============================

function displayCart(cartItems) {
  const container = document.getElementById("cartContainer");

  container.innerHTML = "";

  // ============================
  // Empty Cart
  // ============================

  if (!cartItems || cartItems.length === 0) {
    container.innerHTML = `

            <p>
                Your cart is empty.
            </p>

            <br>

            <a href="menu.html?table=${tableNumber}">
                Go to Menu
            </a>

        `;

    return;
  }

  let grandTotal = 0;

  // ============================
  // Display Cart Items
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
                Price: ₹${price.toFixed(2)}
            </p>


            <div class="quantity-control">

                <button
                    class="quantity-button"
                    onclick="decreaseQuantity(
                        ${item.id},
                        ${quantity}
                    )">

                    −

                </button>


                <span class="quantity">
                    ${quantity}
                </span>


                <button
                    class="quantity-button"
                    onclick="increaseQuantity(
                        ${item.id},
                        ${quantity}
                    )">

                    +

                </button>

            </div>


            <p class="price">
                Item Total: ₹${itemTotal.toFixed(2)}
            </p>


            <button
                class="remove-button"
                onclick="removeFromCart(${item.id})">

                Remove

            </button>

        `;

    container.appendChild(card);
  });

  // ============================
  // Grand Total
  // ============================

  const totalElement = document.createElement("h2");

  totalElement.className = "cart-total";

  totalElement.textContent = "Grand Total: ₹" + grandTotal.toFixed(2);

  container.appendChild(totalElement);

  // ============================
  // Continue Ordering
  // ============================

  const menuLink = document.createElement("a");

  menuLink.className = "continue-button";

  menuLink.href = "menu.html?table=" + tableNumber;

  menuLink.textContent = "Continue Ordering";

  container.appendChild(menuLink);

  // ============================
  // Place Order
  // ============================

  const orderButton = document.createElement("button");

  orderButton.className = "place-order-button";

  orderButton.textContent = "Place Order";

  orderButton.onclick = placeOrder;

  container.appendChild(orderButton);
}

// ============================
// Increase Quantity
// ============================

async function increaseQuantity(cartId, currentQuantity) {
  const newQuantity = currentQuantity + 1;

  await updateQuantity(cartId, newQuantity);
}

// ============================
// Decrease Quantity
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
// Update Quantity
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
// Remove Single Cart Item
// ============================

async function removeFromCart(cartId) {
  try {
    const response = await fetch(BACKEND_URL + "/customer/cart/" + cartId, {
      method: "DELETE",
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to remove item");
    }

    loadCart();
  } catch (error) {
    console.error("Remove cart error:", error);

    alert(error.message);
  }
}

// ============================
// Place Order
// ============================

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

    // ============================
    // SAME TABLE + ORDER SESSION
    // ============================

    const orderId = result.data.id;

    window.location.href =
      "order-success.html" + "?orderId=" + orderId + "&table=" + tableNumber;
  } catch (error) {
    console.error("Place order error:", error);

    alert(error.message);
  }
}

// ============================
// Reload Cart When Page Is Shown
// ============================

window.addEventListener("pageshow", function () {
  if (tableNumber) {
    loadCart();
  }
});
