# Home Depot Order Fulfillment System

A comprehensive, production-ready order fulfillment system built with Spring Boot and Java 17+. This system manages products, inventory, orders, warehouses, and the complete order fulfillment workflow from order placement through delivery.

## 🚀 Features

### Core Functionality
- **Product Management**: Complete CRUD operations for products with SKU tracking
- **Inventory Management**: Real-time inventory tracking across multiple warehouses
- **Order Processing**: Full order lifecycle from placement to delivery
- **Multi-Warehouse Support**: Intelligent warehouse assignment based on inventory availability
- **Fulfillment Workflow**: Pick, pack, and ship operations with tracking
- **Low Stock Alerts**: Automated monitoring of inventory levels
- **Inventory Transfers**: Move inventory between warehouses

### Technical Features
- RESTful API design with comprehensive endpoints
- Layered architecture (Controller → Service → Repository → Entity)
- JPA/Hibernate ORM with MySQL/PostgreSQL support
- Comprehensive error handling and validation
- Transaction management with @Transactional
- Swagger/OpenAPI documentation
- Extensive unit tests
- Production-ready logging with SLF4J

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8+ or PostgreSQL 13+ (optional - H2 included for development)
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
cd order-fulfillment-system
```

### 2. Configure Database

The application is configured to use H2 in-memory database by default for easy setup.

**For Production with MySQL:**
Edit `src/main/resources/application.properties`:

```properties
# Comment out H2 configuration
# Uncomment MySQL configuration
spring.datasource.url=jdbc:mysql://localhost:3306/fulfillment_db?createDatabaseIfNotExist=true
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**For Production with PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fulfillment_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/order-fulfillment-system-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### H2 Console (Development)
Access H2 database console at:
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:fulfillment_db
Username: sa
Password: (leave blank)
```

## 🔌 API Endpoints

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?name={name}` - Search products by name
- `GET /api/products/{id}/availability` - Check product availability across warehouses
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get customer's orders
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/status?status={STATUS}` - Update order status
- `DELETE /api/orders/{id}` - Cancel order

### Inventory
- `GET /api/inventory/warehouse/{warehouseId}` - Get inventory by warehouse
- `GET /api/inventory/product/{productId}` - Get inventory levels for product
- `PUT /api/inventory/restock` - Restock inventory
- `GET /api/inventory/low-stock` - Get low stock alerts
- `POST /api/inventory/transfer` - Transfer inventory between warehouses

### Fulfillment
- `GET /api/fulfillment/pick-list/{warehouseId}` - Generate warehouse pick list
- `PUT /api/fulfillment/pack/{orderId}` - Mark order as packed
- `PUT /api/fulfillment/ship/{orderId}` - Ship order with tracking
- `PUT /api/fulfillment/deliver/{orderId}` - Confirm delivery

### Warehouses
- `GET /api/warehouses` - Get all warehouses
- `GET /api/warehouses/{id}` - Get warehouse by ID

### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer

## 📝 Sample Requests

### Create a Customer
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@email.com",
    "phone": "5551234567",
    "addressLine1": "456 Oak Street",
    "city": "Atlanta",
    "state": "GA",
    "zipCode": "30301"
  }'
```

### Create an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "shippingMethod": "Express",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 5,
        "quantity": 1
      }
    ]
  }'
```

### Restock Inventory
```bash
curl -X PUT http://localhost:8080/api/inventory/restock \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "warehouseId": 1,
    "quantity": 100
  }'
```

### Generate Pick List
```bash
curl http://localhost:8080/api/fulfillment/pick-list/1
```

### Ship Order
```bash
curl -X PUT http://localhost:8080/api/fulfillment/ship/1 \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "carrier": "UPS",
    "estimatedDelivery": "2024-12-31T17:00:00"
  }'
```

## 🏗️ Project Structure

```
src/main/java/com/homedepot/fulfillment/
├── controller/           # REST Controllers
│   ├── ProductController.java
│   ├── OrderController.java
│   ├── InventoryController.java
│   ├── FulfillmentController.java
│   ├── WarehouseController.java
│   └── CustomerController.java
├── service/             # Business Logic Layer
│   ├── ProductService.java
│   ├── OrderService.java
│   ├── InventoryService.java
│   └── FulfillmentService.java
├── repository/          # Data Access Layer
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   ├── InventoryRepository.java
│   ├── WarehouseRepository.java
│   ├── CustomerRepository.java
│   ├── OrderItemRepository.java
│   └── ShipmentRepository.java
├── entity/              # JPA Entities
│   ├── Product.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── Customer.java
│   ├── Inventory.java
│   ├── Warehouse.java
│   ├── Shipment.java
│   └── ShipmentStatus.java
├── dto/                 # Data Transfer Objects
│   ├── OrderRequest.java
│   ├── OrderResponse.java
│   ├── InventoryTransferRequest.java
│   ├── RestockRequest.java
│   ├── ProductAvailabilityResponse.java
│   ├── PickListResponse.java
│   ├── ShipmentRequest.java
│   └── LowStockAlertResponse.java
├── exception/           # Custom Exceptions
│   ├── InsufficientInventoryException.java
│   ├── OrderNotFoundException.java
│   ├── ProductNotFoundException.java
│   ├── CustomerNotFoundException.java
│   ├── WarehouseNotFoundException.java
│   ├── InvalidOrderStatusException.java
│   └── GlobalExceptionHandler.java
└── FulfillmentApplication.java  # Main Application
```

## 🔄 Order Workflow

```
PENDING → PROCESSING → PACKED → SHIPPED → DELIVERED
    ↓
CANCELLED (only from PENDING or PROCESSING)
```

## 💼 Business Rules

1. **Order Creation**
   - Orders can only be placed if sufficient inventory exists
   - Orders are automatically assigned to the nearest warehouse with stock
   - Inventory is immediately reserved upon order creation

2. **Pricing**
   - Tax calculated at 7% of subtotal
   - Free shipping for orders $50+
   - Standard shipping $5 for orders under $50

3. **Order Cancellation**
   - Can only cancel orders in PENDING or PROCESSING status
   - Cancelled orders automatically restore inventory

4. **Tracking Numbers**
   - Format: `HD-{warehouseId}-{orderId}-{timestamp}`

5. **Low Stock Alerts**
   - Triggered when quantity < minimumStockLevel
   - Default minimum: 10 units

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ProductServiceTest
```

### Test Coverage
The project includes comprehensive unit tests for:
- ProductService
- OrderService
- InventoryService

## 🔧 Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Business Rules
fulfillment.tax.rate=0.07
fulfillment.shipping.free-threshold=50.00
fulfillment.shipping.standard-cost=5.00

# Logging
logging.level.com.homedepot.fulfillment=DEBUG
```

## 📊 Sample Data

The application includes sample data for testing:
- 5 Warehouses (Atlanta, Chicago, Dallas, Los Angeles, New York)
- 20 Products across various categories
- 3 Sample Customers
- Pre-populated Inventory across warehouses

## 🚀 Future Enhancements

The system is designed to be expandable for:
- Payment processing integration
- Returns management
- Customer notifications (email/SMS)
- Analytics dashboards
- Multi-tenant support
- Real-time inventory sync
- Barcode scanning integration
- Route optimization for shipments
- Customer loyalty programs
- Advanced reporting

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed for educational and demonstration purposes.

## 👥 Contact

Home Depot Engineering
- Email: support@homedepot.com

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- Hibernate Team for ORM support
- OpenAPI/Swagger for API documentation tools

---

**Built with ❤️ using Spring Boot, Java 17, and modern software engineering practices.**
