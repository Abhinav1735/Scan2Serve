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

if (!tableNumber) {
  tableInfo.textContent = "Table number not found";
} else {
  tableInfo.textContent = "Table " + tableNumber;

  // View Cart Button

  if (cartButton) {
    cartButton.href = "cart.html?table=" + tableNumber;
  }

  loadMenu();
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

  // ============================
  // Empty Menu
  // ============================

  if (!menuData || menuData.length === 0) {
    container.innerHTML = `

            <p>
                No menu items available.
            </p>

        `;

    return;
  }

  // ============================
  // Categories
  // ============================

  menuData.forEach((category) => {
    const categorySection = document.createElement("section");

    categorySection.className = "menu-category";

    // Category Heading

    const heading = document.createElement("h2");

    heading.textContent = category.category;

    // Items Container

    const itemsContainer = document.createElement("div");

    itemsContainer.className = "menu-items";

    // ============================
    // Menu Items
    // ============================

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


                <!-- Quantity Control -->

                <div class="menu-quantity-control">

                    <button
                        class="menu-quantity-button"
                        onclick="decreaseMenuQuantity(${menu.id})">

                        −

                    </button>


                    <span
                        id="quantity-${menu.id}"
                        class="menu-quantity">

                        1

                    </span>


                    <button
                        class="menu-quantity-button"
                        onclick="increaseMenuQuantity(${menu.id})">

                        +

                    </button>

                </div>


                <!-- Add To Cart -->

                <button
                    class="add-button"
                    onclick="addToCart(
                        ${menu.id}
                    )">

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

    const response = await fetch(
      BACKEND_URL + "/customer/cart",

      {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify({
          tableNumber: Number(tableNumber),

          menuId: menuId,

          quantity: quantity,
        }),
      },
    );

    const result = await response.json();

    // ============================
    // Check Response
    // ============================

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to add item to cart");
    }

    // ============================
    // Success
    // ============================

    alert(quantity + " item(s) added to cart successfully");

    console.log("Cart Response:", result);

    // Reset quantity to 1

    const quantityElement = document.getElementById("quantity-" + menuId);

    if (quantityElement) {
      quantityElement.textContent = "1";
    }
  } catch (error) {
    console.error("Add to cart error:", error);

    alert(error.message);
  }
}
