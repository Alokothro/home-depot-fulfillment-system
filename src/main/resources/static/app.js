// State Management
const state = {
    products: [],
    filteredProducts: [],
    cart: [],
    customer: null,
    activeDepartment: 'all',
    searchQuery: '',
    searchTimeout: null,
    isLoading: false
};

// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

async function initializeApp() {
    setupEventListeners();
    setupAuthListeners();
    document.body.classList.add('modal-open'); // login gate shows first
    await loadProducts();
    renderProducts();
}

// Load Products from API
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE}/products`);
        if (!response.ok) throw new Error('Failed to load products');

        state.products = await response.json();
        state.filteredProducts = [...state.products];

        console.log(`Loaded ${state.products.length} products`);
    } catch (error) {
        console.error('Error loading products:', error);
        document.getElementById('resultsCount').textContent = 'Error loading products. Please refresh the page.';
    }
}

// Setup Event Listeners
function setupEventListeners() {
    // Search
    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', handleSearch);

    // Department buttons
    const deptButtons = document.querySelectorAll('.dept-btn');
    deptButtons.forEach(btn => {
        btn.addEventListener('click', handleDepartmentClick);
    });

    // Cart button
    document.getElementById('cartBtn').addEventListener('click', openCart);
    document.getElementById('closeCart').addEventListener('click', closeCart);

    // Checkout button → opens delivery selection
    document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);

    // Delivery modal
    document.getElementById('closeDelivery').addEventListener('click', closeDeliveryModal);
    document.getElementById('placeOrderBtn').addEventListener('click', handlePlaceOrder);
    document.getElementById('optBopis').addEventListener('click', () => selectDelivery(document.getElementById('optBopis')));

    // Order confirmation close
    document.getElementById('confirmCloseBtn').addEventListener('click', () => {
        document.getElementById('confirmModal').classList.remove('active');
        document.body.classList.remove('modal-open');
    });

    // Close modal on background click
    document.getElementById('cartModal').addEventListener('click', (e) => {
        if (e.target.id === 'cartModal') {
            closeCart();
        }
    });
}

// Search Handler with a light debounce (filtering itself is instant)
function handleSearch(e) {
    const query = e.target.value.toLowerCase().trim();

    if (state.searchTimeout) {
        clearTimeout(state.searchTimeout);
    }

    // Short debounce just to coalesce fast typing; no artificial delay
    state.searchTimeout = setTimeout(() => {
        state.searchQuery = query;
        applyFilters();
    }, 120);
}

// Department Filter Handler
function handleDepartmentClick(e) {
    const dept = e.target.dataset.dept;

    // Update active button
    document.querySelectorAll('.dept-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    e.target.classList.add('active');

    state.activeDepartment = dept;
    applyFilters();
}

// Apply Filters synchronously (no artificial delay)
function applyFilters() {
    let filtered = state.products;

    // Department filter
    if (state.activeDepartment !== 'all') {
        filtered = filtered.filter(p => p.department === state.activeDepartment);
    }

    // Search filter
    if (state.searchQuery) {
        const q = state.searchQuery;
        filtered = filtered.filter(p =>
            p.name.toLowerCase().includes(q) ||
            p.description.toLowerCase().includes(q) ||
            p.category.toLowerCase().includes(q) ||
            p.sku.toLowerCase().includes(q)
        );
    }

    state.filteredProducts = filtered;
    state.isLoading = false;
    renderProducts();
    updateActiveFilters();
}

// Show Loading State
function showLoading() {
    state.isLoading = true;
    const grid = document.getElementById('productGrid');

    grid.innerHTML = `
        <div class="loading" style="grid-column: 1 / -1; text-align: center; padding: 60px 20px;">
            <div style="display: inline-block; width: 50px; height: 50px; border: 5px solid #f3f3f3; border-top: 5px solid var(--hd-orange); border-radius: 50%; animation: spin 1s linear infinite;"></div>
            <h3 style="margin-top: 20px; color: var(--hd-gray-dark);">Loading products...</h3>
        </div>
    `;

    document.getElementById('resultsCount').textContent = 'Searching...';
}

// Update Active Filters Display
function updateActiveFilters() {
    const filtersContainer = document.getElementById('activeFilters');
    filtersContainer.innerHTML = '';

    if (state.activeDepartment !== 'all') {
        const tag = createFilterTag(state.activeDepartment, () => {
            document.querySelector('.dept-btn[data-dept="all"]').click();
        });
        filtersContainer.appendChild(tag);
    }

    if (state.searchQuery) {
        const tag = createFilterTag(`Search: "${state.searchQuery}"`, () => {
            document.getElementById('searchInput').value = '';
            state.searchQuery = '';
            applyFilters();
        });
        filtersContainer.appendChild(tag);
    }
}

function createFilterTag(text, onRemove) {
    const tag = document.createElement('div');
    tag.className = 'filter-tag';
    tag.innerHTML = `
        <span>${text}</span>
        <button>&times;</button>
    `;
    tag.querySelector('button').addEventListener('click', onRemove);
    return tag;
}

// Render Products
function renderProducts() {
    const grid = document.getElementById('productGrid');
    const count = state.filteredProducts.length;

    // Update count
    document.getElementById('resultsCount').textContent =
        `${count} Product${count !== 1 ? 's' : ''}`;

    if (count === 0) {
        grid.innerHTML = `
            <div class="loading">
                <h3>No products found</h3>
                <p>Try adjusting your filters or search term</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = state.filteredProducts.map(product => `
        <div class="product-card">
            <div class="product-image">
                <img class="product-photo" src="${getImageUrl(product)}" alt="${product.name}"
                     loading="lazy" onload="this.classList.add('loaded')"
                     onerror="this.style.display='none';this.previousElementSibling.style.display='block'">
                <span class="product-image-fallback" style="display:none">${getProductIcon(product.department)}</span>
            </div>
            <div class="product-info">
                <div class="product-department">${product.department}</div>
                <div class="product-name">${product.name}</div>
                <div class="product-description">${product.description}</div>
                <div class="product-sku">SKU: ${product.sku}</div>
                <div class="product-footer">
                    <div class="product-price">$${product.price.toFixed(2)}</div>
                    <button
                        class="add-to-cart-btn"
                        onclick="addToCart(${product.productId})"
                    >
                        Add to Cart
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// Get product icon emoji based on department
function getProductIcon(department) {
    const icons = {
        'Garden Center': '🌱',
        'Tools & Hardware': '🔧',
        'Paint': '🎨',
        'Electrical': '💡',
        'Plumbing, Kitchen & Bath': '🚿',
        'Flooring': '🏠',
        'Lumber & Building Materials': '🪵',
        'Doors & Windows': '🚪'
    };
    return icons[department] || '🏪';
}

// Real product photo, bundled locally so it loads instantly & works offline.
// Images are pre-downloaded once by download-product-images.mjs (inventory is fixed).
function getImageUrl(product) {
    return `images/products/${product.productId}.jpg`;
}

// Cart Management
function addToCart(productId) {
    const product = state.products.find(p => p.productId === productId);
    if (!product) return;

    const existingItem = state.cart.find(item => item.productId === productId);

    if (existingItem) {
        existingItem.quantity++;
    } else {
        state.cart.push({
            ...product,
            quantity: 1
        });
    }

    updateCartUI();
    showCartNotification();
}

function removeFromCart(productId) {
    state.cart = state.cart.filter(item => item.productId !== productId);
    updateCartUI();
    renderCartItems();
}

function updateQuantity(productId, delta) {
    const item = state.cart.find(i => i.productId === productId);
    if (!item) return;

    item.quantity += delta;

    if (item.quantity <= 0) {
        removeFromCart(productId);
    } else {
        updateCartUI();
        renderCartItems();
    }
}

function updateCartUI() {
    const count = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    document.getElementById('cartCount').textContent = count;

    const headerCount = document.getElementById('cartHeaderCount');
    if (headerCount) headerCount.textContent = count > 0 ? `(${count})` : '';

    const total = state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    document.getElementById('cartTotal').textContent = `$${total.toFixed(2)}`;
}

function showCartNotification() {
    // Simple visual feedback - could enhance with toast notification
    const cartBtn = document.getElementById('cartBtn');
    cartBtn.style.transform = 'scale(1.1)';
    setTimeout(() => {
        cartBtn.style.transform = 'scale(1)';
    }, 200);
}

function openCart() {
    hideCartError();
    document.getElementById('cartModal').classList.add('active');
    document.body.classList.add('modal-open');
    renderCartItems();
}

function closeCart() {
    document.getElementById('cartModal').classList.remove('active');
    document.body.classList.remove('modal-open');
}

function renderCartItems() {
    const container = document.getElementById('cartItems');

    if (state.cart.length === 0) {
        container.innerHTML = `
            <div class="cart-empty">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="#ccc">
                    <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0020 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
                </svg>
                <h3>Your cart is empty</h3>
                <p>Add some products to get started</p>
            </div>
        `;
        document.getElementById('checkoutBtn').disabled = true;
        return;
    }

    document.getElementById('checkoutBtn').disabled = false;

    container.innerHTML = state.cart.map(item => `
        <div class="cart-item">
            <div class="cart-item-info">
                <div class="cart-item-name">${item.name}</div>
                <div class="cart-item-sku">SKU: ${item.sku}</div>
                <div class="cart-item-controls">
                    <button class="qty-btn" onclick="updateQuantity(${item.productId}, -1)">-</button>
                    <span class="cart-item-qty">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateQuantity(${item.productId}, 1)">+</button>
                    <span class="cart-item-price">$${(item.price * item.quantity).toFixed(2)}</span>
                </div>
                <button class="remove-item-btn" onclick="removeFromCart(${item.productId})">Remove</button>
            </div>
        </div>
    `).join('');
}

// ---- Auth: guest / login gate ----
function setupAuthListeners() {
    document.getElementById('chooseGuestBtn').addEventListener('click', showGuestForm);
    document.getElementById('chooseLoginBtn').addEventListener('click', showLoginForm);
    document.getElementById('guestSubmitBtn').addEventListener('click', handleGuestSubmit);
    document.getElementById('loginSubmitBtn').addEventListener('click', handleLoginSubmit);
    document.getElementById('signoutBtn').addEventListener('click', signOut);

    document.querySelectorAll('[data-auth-back]').forEach(btn =>
        btn.addEventListener('click', showAuthChoice));

    // Enter key submits the guest form
    ['guestFirstName', 'guestLastName'].forEach(id =>
        document.getElementById(id).addEventListener('keydown', e => {
            if (e.key === 'Enter') handleGuestSubmit();
        }));
}

function showAuthScreen(id) {
    ['loginChoice', 'guestForm', 'loginForm'].forEach(screen => {
        document.getElementById(screen).style.display = screen === id ? 'block' : 'none';
    });
}

function showAuthChoice() {
    showAuthScreen('loginChoice');
}

function showGuestForm() {
    document.getElementById('guestError').textContent = '';
    showAuthScreen('guestForm');
    document.getElementById('guestFirstName').focus();
}

async function showLoginForm() {
    showAuthScreen('loginForm');
    const select = document.getElementById('loginCustomerSelect');
    const errorEl = document.getElementById('loginError');
    errorEl.textContent = '';
    select.innerHTML = '<option>Loading accounts…</option>';

    try {
        const response = await fetch(`${API_BASE}/customers`);
        if (!response.ok) throw new Error('Failed to load accounts');

        const customers = await response.json();
        if (!customers.length) {
            select.innerHTML = '<option value="">No accounts found</option>';
            return;
        }

        select.innerHTML = customers.map(c =>
            `<option value="${c.customerId}" data-name="${c.firstName} ${c.lastName}">${c.firstName} ${c.lastName} — ${c.email}</option>`
        ).join('');
    } catch (error) {
        select.innerHTML = '';
        errorEl.textContent = error.message;
    }
}

// Title-case a name so it looks clean on the associate screen no matter how it was typed.
// Handles spaces, hyphens and apostrophes: "mary-JANE o'BRIEN" -> "Mary-Jane O'Brien".
function normalizeName(raw) {
    return (raw || '')
        .trim()
        .replace(/\s+/g, ' ')
        .toLowerCase()
        .replace(/(^|[\s'-])([a-zÀ-ɏ])/g, (m, sep, ch) => sep + ch.toUpperCase());
}

async function handleGuestSubmit() {
    const firstName = normalizeName(document.getElementById('guestFirstName').value);
    const lastName = normalizeName(document.getElementById('guestLastName').value);
    const errorEl = document.getElementById('guestError');

    if (!firstName || !lastName) {
        errorEl.textContent = 'Please enter your first and last name.';
        return;
    }
    errorEl.textContent = '';

    // Reflect the cleaned-up names back into the fields so the guest sees the fix too
    document.getElementById('guestFirstName').value = firstName;
    document.getElementById('guestLastName').value = lastName;

    const btn = document.getElementById('guestSubmitBtn');
    btn.disabled = true;
    btn.textContent = 'Setting up…';

    try {
        // Backend requires full contact details; fill valid placeholders for guests
        const guest = {
            firstName,
            lastName,
            email: `guest.${Date.now()}@guest.homedepot.local`,
            addressLine1: 'In-Store',
            city: 'Atlanta',
            state: 'GA',
            zipCode: '30301'
        };

        const response = await fetch(`${API_BASE}/customers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(guest)
        });
        if (!response.ok) throw new Error('Could not create guest profile. Please try again.');

        const created = await response.json();
        identifyCustomer(created);
    } catch (error) {
        errorEl.textContent = error.message;
    } finally {
        btn.disabled = false;
        btn.textContent = 'Start Shopping';
    }
}

function handleLoginSubmit() {
    const select = document.getElementById('loginCustomerSelect');
    const option = select.options[select.selectedIndex];

    if (!option || !option.value) {
        document.getElementById('loginError').textContent = 'Please select an account.';
        return;
    }

    const fullName = option.dataset.name || '';
    const [firstName, ...rest] = fullName.split(' ');
    identifyCustomer({
        customerId: Number(option.value),
        firstName: firstName || 'Customer',
        lastName: rest.join(' ')
    });
}

function identifyCustomer(customer) {
    state.customer = customer;
    document.getElementById('loginModal').classList.remove('active');
    document.body.classList.remove('modal-open');

    const greeting = document.getElementById('userGreeting');
    document.getElementById('userGreetingName').textContent = `Hi, ${customer.firstName}`;
    greeting.style.display = 'flex';
}

function signOut() {
    state.customer = null;
    state.cart = [];
    updateCartUI();
    closeCart();
    document.getElementById('userGreeting').style.display = 'none';
    showAuthChoice();
    document.getElementById('loginModal').classList.add('active');
    document.body.classList.add('modal-open');
}

// ---- Big-item detection (drives Van Delivery eligibility) ----
// Van is required for heavy items (≥50 lbs), all lumber/building materials
// (too bulky for a car regardless of weight), and large door/window units (≥35 lbs).
function isBigItem(product) {
    const weight = product.weight || 0;
    const dept   = product.department || '';
    const cat    = product.category || '';
    if (weight >= 50) return true;
    if (dept === 'Lumber & Building Materials') return true;
    if (dept === 'Doors & Windows' && weight >= 35) return true;
    if (['Lumber', 'Building Materials'].includes(cat)) return true;
    return false;
}

function cartHasBigItems()  { return state.cart.some(item  => isBigItem(item)); }
function cartHasOnlyBigItems() { return state.cart.length > 0 && state.cart.every(item => isBigItem(item)); }

// ---- Delivery modal ----
let selectedDeliveryMethod = null;

function handleCheckout() {
    if (state.cart.length === 0) return;
    if (!state.customer) { signOut(); return; }
    hideCartError();
    openDeliveryModal();
}

function openDeliveryModal() {
    const hasBig   = cartHasBigItems();
    const onlyBig  = cartHasOnlyBigItems();

    const vanOpt  = document.getElementById('optVan');
    const carOpt  = document.getElementById('optCar');
    const vanDesc = document.getElementById('vanDesc');

    // Van: only unlocked when cart has at least one big item
    if (hasBig) {
        vanOpt.classList.remove('delivery-option-locked');
        vanOpt.onclick = () => selectDelivery(vanOpt);
        vanDesc.textContent = onlyBig
            ? 'Required for large & heavy items · 2–5 business days · Scheduled delivery window'
            : 'Required for the large & heavy items in your cart · 2–5 business days';
    } else {
        vanOpt.classList.add('delivery-option-locked');
        vanOpt.onclick = null;
        vanDesc.textContent = 'Only available for large & heavy items (lumber, doors, heavy appliances)';
    }

    // Car: locked when cart is ALL big items (no standard items to deliver by car)
    if (onlyBig) {
        carOpt.classList.add('delivery-option-locked');
        carOpt.onclick = null;
        carOpt.querySelector('.delivery-desc').textContent =
            'Not available — your cart contains only large items requiring van delivery';
    } else {
        carOpt.classList.remove('delivery-option-locked');
        carOpt.onclick = () => selectDelivery(carOpt);
        carOpt.querySelector('.delivery-desc').textContent = 'Standard items · 1–3 business days';
    }

    // Reset selection, then pre-select sensible default
    selectedDeliveryMethod = null;
    document.querySelectorAll('.delivery-option').forEach(o => o.classList.remove('selected'));
    document.getElementById('deliveryError').textContent = '';
    selectDelivery(onlyBig ? vanOpt : document.getElementById('optBopis'));

    closeCart();
    document.getElementById('deliveryModal').classList.add('active');
    document.body.classList.add('modal-open');
}

function selectDelivery(el) {
    if (!el || el.classList.contains('delivery-option-locked')) return;
    document.querySelectorAll('.delivery-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    selectedDeliveryMethod = el.dataset.method;
    document.getElementById('deliveryError').textContent = '';
}

function closeDeliveryModal() {
    document.getElementById('deliveryModal').classList.remove('active');
    document.body.classList.remove('modal-open');
}

async function handlePlaceOrder() {
    if (!selectedDeliveryMethod) {
        document.getElementById('deliveryError').textContent = 'Please select a delivery method.';
        return;
    }

    const btn = document.getElementById('placeOrderBtn');
    btn.disabled = true;
    btn.textContent = 'Placing order…';
    document.getElementById('deliveryError').textContent = '';

    try {
        const response = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: state.customer.customerId,
                shippingMethod: selectedDeliveryMethod,
                items: state.cart.map(i => ({ productId: i.productId, quantity: i.quantity }))
            })
        });

        if (!response.ok) {
            let message = 'Failed to place order. Please try again.';
            try { const e = await response.json(); message = e.message || e.error || message; } catch (_) {}
            throw new Error(message);
        }

        const order = await response.json();
        state.cart = [];
        updateCartUI();
        closeDeliveryModal();
        showOrderConfirmation(order);

    } catch (error) {
        document.getElementById('deliveryError').textContent = error.message || 'Failed to place order. Please try again.';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirm & Place Order';
    }
}

function showCartError(message) {
    const el = document.getElementById('cartError');
    el.textContent = message;
    el.style.display = 'block';
}

function hideCartError() {
    const el = document.getElementById('cartError');
    el.textContent = '';
    el.style.display = 'none';
}

function showOrderConfirmation(order) {
    const total = order.totalAmount != null ? `$${Number(order.totalAmount).toFixed(2)}` : '';
    document.getElementById('confirmDetails').innerHTML =
        `Order <strong>#${order.orderId}</strong> for <strong>${state.customer.firstName} ${state.customer.lastName}</strong>` +
        (total ? `<br>Total: <strong>${total}</strong>` : '');

    const deliveryLabels = {
        'BOPIS':        '🏪 Ready for pick-up in 2 hours',
        'Car Delivery': '🚗 Car delivery · 1–3 business days',
        'Van Delivery': '🚐 Van delivery · 2–5 business days · We\'ll call to schedule'
    };
    document.getElementById('confirmDelivery').textContent =
        deliveryLabels[selectedDeliveryMethod] || selectedDeliveryMethod || '';

    document.getElementById('confirmModal').classList.add('active');
    document.body.classList.add('modal-open');
}

// Make functions globally accessible for onclick handlers
window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.updateQuantity = updateQuantity;
