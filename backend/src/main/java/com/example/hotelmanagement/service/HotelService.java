package com.example.hotelmanagement.service;

import com.example.hotelmanagement.model.MenuItem;
import com.example.hotelmanagement.model.OrderItem;
import com.example.hotelmanagement.model.TableOrder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class HotelService {
    private final Map<Long, MenuItem> menu = new ConcurrentHashMap<>();
    private final Map<Long, TableOrder> orders = new ConcurrentHashMap<>();
    private final AtomicLong menuId = new AtomicLong(1);
    private final AtomicLong tableOrderId = new AtomicLong(1);

    public List<MenuItem> getMenu() {
        return menu.values().stream().sorted(Comparator.comparing(MenuItem::getId)).toList();
    }

    public MenuItem addMenuItem(String name, BigDecimal price) {
        Long id = menuId.getAndIncrement();
        MenuItem item = new MenuItem(id, name, price);
        menu.put(id, item);
        return item;
    }

    public TableOrder startTable(int tableNumber) {
        boolean isActive = orders.values().stream()
                .anyMatch(order -> order.getTableNumber() == tableNumber && !order.isServed());
        if (isActive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table is already active");
        }

        Long id = tableOrderId.getAndIncrement();
        TableOrder order = new TableOrder(id, tableNumber);
        orders.put(id, order);
        return order;
    }

    public List<TableOrder> getActiveTables() {
        return orders.values().stream()
                .filter(order -> !order.isServed())
                .sorted(Comparator.comparingInt(TableOrder::getTableNumber))
                .toList();
    }

    public TableOrder addOrderItem(Long tableOrderId, Long menuItemId, int quantity) {
        TableOrder order = getTableOrderOrThrow(tableOrderId);
        if (order.isServed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add item. Table already served");
        }

        MenuItem menuItem = menu.get(menuItemId);
        if (menuItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found");
        }

        OrderItem existing = order.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            order.getItems().add(new OrderItem(menuItemId, menuItem.getName(), menuItem.getPrice(), quantity));
        } else {
            existing.setQuantity(existing.getQuantity() + quantity);
        }

        return order;
    }

    public TableOrder serveTable(Long tableOrderId) {
        TableOrder order = getTableOrderOrThrow(tableOrderId);
        order.setServed(true);
        return order;
    }

    public TableOrder getBill(Long tableOrderId) {
        return getTableOrderOrThrow(tableOrderId);
    }

    public List<TableOrder> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    private TableOrder getTableOrderOrThrow(Long tableOrderId) {
        TableOrder order = orders.get(tableOrderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Table order not found");
        }
        return order;
    }
}
