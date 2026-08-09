const BACKEND_URL = "http://localhost:8080";

// ============================
// Get Table Number From URL
// ============================

const params = new URLSearchParams(window.location.search);

const tableNumber = params.get("table");

// ============================
// Table Information
// ============================

const tableInfo = document.getElementById("tableInfo");

const cartButton = document.getElementById("cartButton");

const viewOrderButton = document.getElementById("viewOrderButton");

if (!tableNumber) {
  tableInfo.textContent = "Table number not found";
} else {
  tableInfo.textContent = "Table " + tableNumber;

  // ============================
  // View Cart Button
  // ============================

  if (cartButton) {
    cartButton.href = "cart.html?table=" + tableNumber;
  }

  // ============================
  // Check Current Order
  // ============================

  checkCurrentOrder();

  // ============================
  // Load Menu
  // ============================

  loadMenu();
}

// ============================
// Check Current Order
// ============================

async function checkCurrentOrder() {
  try {
    const response = await fetch(
      BACKEND_URL + "/customer/order/current/" + tableNumber,
    );

    const result = await response.json();

    console.log("Current Order:", result);

    if (response.ok && result.success && result.data) {
      const order = result.data;

      // Show View Current Order

      viewOrderButton.style.display = "inline-block";

      viewOrderButton.href =
        "bill.html?orderId=" + order.id + "&table=" + tableNumber;
    } else {
      viewOrderButton.style.display = "none";
    }
  } catch (error) {
    console.error("Current order error:", error);

    viewOrderButton.style.display = "none";
  }
}

// ============================
// Load Customer Menu
// ============================

async function loadMenu() {
  try {
    const response = await fetch(BACKEND_URL + "/customer/menu");

    if (!response.ok) {
      throw new Error("Failed to fetch menu");
    }

    const menuData = await response.json();

    displayMenu(menuData);
  } catch (error) {
    console.error("Menu loading error:", error);

    document.getElementById("menuContainer").innerHTML = `

                <p>
                    Unable to load menu
                </p>

            `;
  }
}

// ============================
// Display Menu
// ============================

function displayMenu(menuData) {
  const container = document.getElementById("menuContainer");

  container.innerHTML = "";

  if (!menuData || menuData.length === 0) {
    container.innerHTML = `

            <p>
                No menu items available.
            </p>

        `;

    return;
  }

  menuData.forEach((category) => {
    const categorySection = document.createElement("section");

    categorySection.className = "menu-category";

    const heading = document.createElement("h2");

    heading.textContent = category.category;

    const itemsContainer = document.createElement("div");

    itemsContainer.className = "menu-items";

    category.items.forEach((menu) => {
      const card = document.createElement("div");

      card.className = "menu-card";

      card.innerHTML = `

                        <h3>
                            ${menu.name}
                        </h3>

                        <p>
                            ${menu.description}
                        </p>

                        <p class="price">
                            ₹${menu.price}
                        </p>


                        <div
                            class="menu-quantity-control"
                        >

                            <button
                                class="menu-quantity-button"
                                onclick="
                                    decreaseMenuQuantity(
                                        ${menu.id}
                                    )
                                "
                            >
                                −
                            </button>


                            <span
                                id="quantity-${menu.id}"
                                class="menu-quantity"
                            >
                                1
                            </span>


                            <button
                                class="menu-quantity-button"
                                onclick="
                                    increaseMenuQuantity(
                                        ${menu.id}
                                    )
                                "
                            >
                                +
                            </button>

                        </div>


                        <button
                            class="add-button"
                            onclick="
                                addToCart(
                                    ${menu.id}
                                )
                            "
                        >
                            Add to Cart
                        </button>

                    `;

      itemsContainer.appendChild(card);
    });

    categorySection.appendChild(heading);

    categorySection.appendChild(itemsContainer);

    container.appendChild(categorySection);
  });
}

// ============================
// Get Menu Quantity
// ============================

function getMenuQuantity(menuId) {
  const quantityElement = document.getElementById("quantity-" + menuId);

  if (!quantityElement) {
    return 1;
  }

  return Number(quantityElement.textContent);
}

// ============================
// Increase Menu Quantity
// ============================

function increaseMenuQuantity(menuId) {
  const quantityElement = document.getElementById("quantity-" + menuId);

  if (!quantityElement) {
    return;
  }

  let quantity = Number(quantityElement.textContent);

  quantity++;

  quantityElement.textContent = quantity;
}

// ============================
// Decrease Menu Quantity
// ============================

function decreaseMenuQuantity(menuId) {
  const quantityElement = document.getElementById("quantity-" + menuId);

  if (!quantityElement) {
    return;
  }

  let quantity = Number(quantityElement.textContent);

  if (quantity <= 1) {
    return;
  }

  quantity--;

  quantityElement.textContent = quantity;
}

// ============================
// Add To Cart
// ============================

async function addToCart(menuId) {
  try {
    const quantity = getMenuQuantity(menuId);

    const response = await fetch(BACKEND_URL + "/customer/cart", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        tableNumber: Number(tableNumber),

        menuId: menuId,

        quantity: quantity,
      }),
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to add item to cart");
    }

    alert(quantity + " item(s) added to cart successfully");

    console.log("Cart Response:", result);

    const quantityElement = document.getElementById("quantity-" + menuId);

    if (quantityElement) {
      quantityElement.textContent = "1";
    }
  } catch (error) {
    console.error("Add to cart error:", error);

    alert(error.message);
  }
}
