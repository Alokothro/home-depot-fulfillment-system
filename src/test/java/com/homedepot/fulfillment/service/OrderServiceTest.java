package com.homedepot.fulfillment.service;

import com.homedepot.fulfillment.dto.OrderRequest;
import com.homedepot.fulfillment.dto.OrderResponse;
import com.homedepot.fulfillment.entity.*;
import com.homedepot.fulfillment.exception.CustomerNotFoundException;
import com.homedepot.fulfillment.exception.InvalidOrderStatusException;
import com.homedepot.fulfillment.exception.OrderNotFoundException;
import com.homedepot.fulfillment.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Product testProduct;
    private Warehouse testWarehouse;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(orderService, "taxRate", 0.07);
        ReflectionTestUtils.setField(orderService, "shippingFreeThreshold", 50.00);
        ReflectionTestUtils.setField(orderService, "shippingStandardCost", 5.00);

        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john@example.com");

        testProduct = new Product();
        testProduct.setProductId(1L);
        testProduct.setSku("TEST-001");
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("29.99"));

        testWarehouse = new Warehouse();
        testWarehouse.setWarehouseId(1L);
        testWarehouse.setName("Test Warehouse");
        testWarehouse.setCapacity(10000);
        testWarehouse.setCurrentUtilization(1000);

        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setWarehouse(testWarehouse);
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100.00"));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
    }

    @Test
    void getOrderById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnCustomerOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerCustomerIdOrderByOrderDateDesc(1L)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrdersByCustomerId(1L);

        // Assert
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByCustomerCustomerIdOrderByOrderDateDesc(1L);
    }

    @Test
    void updateOrderStatus_WhenValid_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenPending_ShouldCancelAndRestoreInventory() {
        // Arrange
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);
        testOrder.addOrderItem(orderItem);
        testOrder.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(inventoryService).increaseInventory(any(), any(), anyInt());

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(inventoryService, times(1)).increaseInventory(any(), any(), anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void cancelOrder_WhenShipped_ShouldThrowException() {
        // Arrange
        testOrder.setOrderStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(InvalidOrderStatusException.class, () -> {
            orderService.cancelOrder(1L);
        });
    }
}
