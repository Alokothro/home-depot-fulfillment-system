// State Management
const state = {
    products: [],
    filteredProducts: [],
    cart: [],
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
    await loadProducts();
    setupEventListeners();
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

    // Checkout button
    document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);

    // Close modal on background click
    document.getElementById('cartModal').addEventListener('click', (e) => {
        if (e.target.id === 'cartModal') {
            closeCart();
        }
    });
}

// Search Handler with debounce
function handleSearch(e) {
    const query = e.target.value.toLowerCase().trim();

    // Clear previous timeout
    if (state.searchTimeout) {
        clearTimeout(state.searchTimeout);
    }

    // Show loading immediately if there's a query
    if (query) {
        showLoading();
    }

    // Debounce search - wait 300ms after user stops typing
    state.searchTimeout = setTimeout(() => {
        state.searchQuery = query;
        applyFilters();
    }, 300);
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

// Apply Filters with loading delay
function applyFilters() {
    // Show loading state
    showLoading();

    // Simulate loading delay for better UX (500ms)
    setTimeout(() => {
        let filtered = [...state.products];

        // Department filter
        if (state.activeDepartment !== 'all') {
            filtered = filtered.filter(p => p.department === state.activeDepartment);
        }

        // Search filter
        if (state.searchQuery) {
            filtered = filtered.filter(p =>
                p.name.toLowerCase().includes(state.searchQuery) ||
                p.description.toLowerCase().includes(state.searchQuery) ||
                p.category.toLowerCase().includes(state.searchQuery) ||
                p.sku.toLowerCase().includes(state.searchQuery)
            );
        }

        state.filteredProducts = filtered;
        state.isLoading = false;
        renderProducts();
        updateActiveFilters();
    }, 500);
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
                ${getProductIcon(product.department)}
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
    document.getElementById('cartModal').classList.add('active');
    renderCartItems();
}

function closeCart() {
    document.getElementById('cartModal').classList.remove('active');
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

async function handleCheckout() {
    if (state.cart.length === 0) return;

    try {
        // For now, just show an alert. In a real app, this would create an order
        const total = state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const itemCount = state.cart.reduce((sum, item) => sum + item.quantity, 0);

        alert(`Order placed successfully!\n\nItems: ${itemCount}\nTotal: $${total.toFixed(2)}\n\nOrder will be processed by the warehouse team.`);

        // Clear cart
        state.cart = [];
        updateCartUI();
        closeCart();

    } catch (error) {
        console.error('Checkout error:', error);
        alert('Failed to place order. Please try again.');
    }
}

// Make functions globally accessible for onclick handlers
window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.updateQuantity = updateQuantity;
