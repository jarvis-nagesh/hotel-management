package com.example.hotelmanagement.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TableOrder {
    private Long id;
    private int tableNumber;
    private boolean served;
    private List<OrderItem> items = new ArrayList<>();

    public TableOrder() {
    }

    public TableOrder(Long id, int tableNumber) {
        this.id = id;
        this.tableNumber = tableNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public boolean isServed() { return served; }
    public void setServed(boolean served) { this.served = served; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
