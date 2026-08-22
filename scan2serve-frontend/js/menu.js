// =========================================================
// SCAN2SERVE - CUSTOMER MENU
// =========================================================

const BACKEND_URL = "http://localhost:8080";

// =========================================================
// GET TABLE NUMBER FROM URL
// =========================================================

const params = new URLSearchParams(window.location.search);

const tableNumber = params.get("table");

// =========================================================
// TABLE INFORMATION
// =========================================================

const tableInfo = document.getElementById("tableInfo");

const cartButton = document.getElementById("cartButton");

const viewOrderButton = document.getElementById("viewOrderButton");

// =========================================================
// INITIALIZATION
// =========================================================

if (!tableNumber) {
  tableInfo.textContent = "Table number not found";
} else {
  tableInfo.textContent = "Table " + tableNumber;

  // =======================================================
  // VIEW CART BUTTON
  // =======================================================

  if (cartButton) {
    cartButton.href = "cart.html?table=" + encodeURIComponent(tableNumber);
  }

  // =======================================================
  // CHECK CURRENT ORDER
  // =======================================================

  checkCurrentOrder();

  // =======================================================
  // LOAD MENU
  // =======================================================

  loadMenu();
}

// =========================================================
// CHECK CURRENT ORDER
// =========================================================

async function checkCurrentOrder() {
  try {
    const response = await fetch(
      BACKEND_URL +
        "/customer/order/current/" +
        encodeURIComponent(tableNumber),
    );

    const result = await response.json();

    console.log("Current Order:", result);

    if (response.ok && result.success && result.data) {
      const order = result.data;

      // =================================================
      // SHOW CURRENT ORDER BUTTON
      // =================================================

      viewOrderButton.style.display = "inline-flex";

      // =================================================
      // CURRENT ORDER / BILL PAGE
      // =================================================

      viewOrderButton.href =
        "bill.html?orderId=" +
        encodeURIComponent(order.id) +
        "&table=" +
        encodeURIComponent(tableNumber);
    } else {
      viewOrderButton.style.display = "none";
    }
  } catch (error) {
    console.error("Current order error:", error);

    viewOrderButton.style.display = "none";
  }
}

// =========================================================
// LOAD CUSTOMER MENU
// =========================================================

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
      <p id="error">
        Unable to load menu
      </p>
    `;
  }
}

// =========================================================
// DISPLAY MENU
// =========================================================

function displayMenu(menuData) {
  const container = document.getElementById("menuContainer");

  container.innerHTML = "";

  // =======================================================
  // EMPTY MENU
  // =======================================================

  if (!menuData || menuData.length === 0) {
    container.innerHTML = `
      <p id="error">
        No menu items available.
      </p>
    `;

    return;
  }

  // =======================================================
  // CATEGORIES
  // =======================================================

  menuData.forEach((category) => {
    const categorySection = document.createElement("section");

    categorySection.className = "menu-category";

    // ===================================================
    // CATEGORY TITLE
    // ===================================================

    const heading = document.createElement("h2");

    heading.textContent = category.category;

    // ===================================================
    // ITEMS CONTAINER
    // ===================================================

    const itemsContainer = document.createElement("div");

    itemsContainer.className = "menu-items";

    // ===================================================
    // MENU ITEMS
    // ===================================================

    category.items.forEach((menu) => {
      const card = document.createElement("div");

      card.className = "menu-card";

      // ===============================================
      // IMAGE URL
      // ===============================================

      const imageUrl = menu.imageUrl || menu.image || menu.foodImage || "";

      // ===============================================
      // IMAGE HTML
      // ===============================================

      let imageHtml = "";

      if (imageUrl) {
        imageHtml = `
              <div class="menu-food-image">

                <img
                  src="${escapeHtml(imageUrl)}"
                  alt="${escapeHtml(menu.name)}"
                  onerror="
                    this.style.display='none';
                    this.parentElement.classList.add(
                      'image-placeholder'
                    );
                  "
                />

                <div class="menu-image-placeholder">
                  🍽️
                </div>

              </div>
            `;
      } else {
        imageHtml = `
              <div class="menu-food-image image-placeholder">

                <div class="menu-image-placeholder">
                  🍽️
                </div>

              </div>
            `;
      }

      // ===============================================
      // CARD
      // ===============================================

      card.innerHTML = `
            ${imageHtml}

            <div class="menu-card-content">

              <h3>
                ${escapeHtml(menu.name)}
              </h3>

              <p class="menu-description">
                ${escapeHtml(menu.description)}
              </p>

              <p class="price">
                ₹${menu.price}
              </p>

              <div class="menu-card-actions">

                <div
                  class="menu-quantity-control"
                >

                  <button
                    type="button"
                    class="menu-quantity-button"
                    onclick="
                      decreaseMenuQuantity(
                        ${menu.id}
                      )
                    "
                    aria-label="Decrease quantity"
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
                    type="button"
                    class="menu-quantity-button"
                    onclick="
                      increaseMenuQuantity(
                        ${menu.id}
                      )
                    "
                    aria-label="Increase quantity"
                  >
                    +
                  </button>

                </div>

                <button
                  type="button"
                  class="add-button"
                  onclick="
                    addToCart(
                      ${menu.id}
                    )
                  "
                >
                  🛒 Add to Cart
                </button>

              </div>

            </div>
          `;

      itemsContainer.appendChild(card);
    });

    categorySection.appendChild(heading);

    categorySection.appendChild(itemsContainer);

    container.appendChild(categorySection);
  });
}

// =========================================================
// GET MENU QUANTITY
// =========================================================

function getMenuQuantity(menuId) {
  const quantityElement = document.getElementById("quantity-" + menuId);

  if (!quantityElement) {
    return 1;
  }

  return Number(quantityElement.textContent);
}

// =========================================================
// INCREASE MENU QUANTITY
// =========================================================

function increaseMenuQuantity(menuId) {
  const quantityElement = document.getElementById("quantity-" + menuId);

  if (!quantityElement) {
    return;
  }

  let quantity = Number(quantityElement.textContent);

  quantity++;

  quantityElement.textContent = quantity;
}

// =========================================================
// DECREASE MENU QUANTITY
// =========================================================

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

// =========================================================
// ADD TO CART
// =========================================================

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

    // ===================================================
    // CHECK RESPONSE
    // ===================================================

    if (!response.ok || !result.success) {
      throw new Error(result.message || "Unable to add item to cart");
    }

    // ===================================================
    // SUCCESS
    // ===================================================

    alert(quantity + " item(s) added to cart successfully");

    console.log("Cart Response:", result);

    // ===================================================
    // RESET QUANTITY
    // ===================================================

    const quantityElement = document.getElementById("quantity-" + menuId);

    if (quantityElement) {
      quantityElement.textContent = "1";
    }

    // ===================================================
    // CHECK CURRENT ORDER AGAIN
    // ===================================================

    checkCurrentOrder();
  } catch (error) {
    console.error("Add to cart error:", error);

    alert(error.message);
  }
}

// =========================================================
// ESCAPE HTML
// =========================================================

function escapeHtml(value) {
  const div = document.createElement("div");

  div.textContent = value ?? "";

  return div.innerHTML;
}
